import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        taskManager = new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile);
    }

    @Test
    void saveAndLoadEmptyFile() {
        // сохранения пустого менеджера
        taskManager.save();

        // загрузка из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getTasks().isEmpty(), "Задачи должны быть пустыми");
        assertTrue(loadedManager.getEpics().isEmpty(), "Эпики должны быть пустыми");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Подзадачи должны быть пустыми");
    }

    @Test
    void saveAndLoadTasks() {
        // Создаем задачи
        Task task = new Task(0, "Test Task", "Test Description", "NEW");
        taskManager.createTask(task);

        Epic epic = new Epic(0, "Test Epic", "Test Epic Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Test Subtask", "Test Subtask Description", "NEW", epic.getId());
        taskManager.createSubtask(subtask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем задачи
        assertEquals(1, loadedManager.getTasks().size(), "Неверное количество задач");
        assertEquals(1, loadedManager.getEpics().size(), "Неверное количество эпиков");
        assertEquals(1, loadedManager.getSubtasks().size(), "Неверное количество подзадач");

        Task loadedTask = loadedManager.getTask(task.getId());
        assertNotNull(loadedTask, "Задача не загрузилась");
        assertEquals(task.getTitle(), loadedTask.getTitle(), "Название задачи не совпадает");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Описание задачи не совпадает");

        Epic loadedEpic = loadedManager.getEpic(epic.getId());
        assertNotNull(loadedEpic, "Эпик не загрузился");
        assertEquals(epic.getTitle(), loadedEpic.getTitle(), "Название эпика не совпадает");

        Subtask loadedSubtask = loadedManager.getSubtask(subtask.getId());
        assertNotNull(loadedSubtask, "Подзадача не загрузилась");
        assertEquals(subtask.getTitle(), loadedSubtask.getTitle(), "Название подзадачи не совпадает");
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId(), "EpicId подзадачи не совпадает");
    }

    @Test
    void saveAndLoadWithHistory() {
        // создание задач
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        // добавление в историю
        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());

        // сохранения и загрузка
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем, что задачи загрузились
        assertNotNull(loadedManager.getTask(task.getId()), "Задача не загрузилась");
        assertNotNull(loadedManager.getEpic(epic.getId()), "Эпик не загрузился");
    }

    @Test
    void preserveTaskDataAfterSaveAndLoad() {
        Task task = new Task(0, "Original", "Original Desc", "NEW");
        taskManager.createTask(task);

        // Меняем оригинальную задачу (не должно повлиять на сохраненную)
        task.setTitle("Changed");
        task.setDescription("Changed Desc");
        task.setStatus("DONE");

        // Загружаем
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTask(task.getId());

        assertEquals("Original", loadedTask.getTitle(), "Название не сохранилось");
        assertEquals("Original Desc", loadedTask.getDescription(), "Описание не сохранилось");
        assertEquals("NEW", loadedTask.getStatus(), "Статус не сохранился");
    }

    @Test
    void handleEpicStatusAfterLoad() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "NEW", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "NEW", epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        // Меняем статус одной подзадачи
        subtask1.setStatus("DONE");
        taskManager.updateSubtask(subtask1);

        // Загружаем
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpic(epic.getId());

        assertEquals("IN_PROGRESS", loadedEpic.getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void saveAfterEachOperation() {
        // Проверяем, что файл создается после каждой операции
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);
        assertTrue(tempFile.exists(), "Файл должен существовать после создания задачи");

        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);
        assertTrue(tempFile.exists(), "Файл должен существовать после создания эпика");

        taskManager.deleteTask(task.getId());
        assertTrue(tempFile.exists(), "Файл должен существовать после удаления задачи");
    }

    @Test
    void loadFromNonExistentFile() {
        File nonExistentFile = new File("non_existent_file.csv");
        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(nonExistentFile);
        }, "Должно выбрасываться исключение при загрузке из несуществующего файла");
    }

    @Test
    void saveToNonWritableFile() {
        File readOnlyFile = new File("read_only.csv");
        readOnlyFile.setReadOnly();

        FileBackedTaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), readOnlyFile);
        assertThrows(ManagerSaveException.class, () -> {
            Task task = new Task(0, "Task", "Description", "NEW");
            manager.createTask(task);
        }, "Должно выбрасываться исключение при сохранении в недоступный файл");
    }
}