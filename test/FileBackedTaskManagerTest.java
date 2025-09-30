import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    File tempDir;
    private File testFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            testFile = File.createTempFile("test", ".csv", tempDir);
            return new FileBackedTaskManager(Managers.getDefaultHistory(), testFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }

    @Test
    void saveLoadEmptyManager() {
        taskManager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertTrue(loadedManager.getTasks().isEmpty(), "Задачи должны быть пустыми");
        assertTrue(loadedManager.getEpics().isEmpty(), "Эпики должны быть пустыми");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Подзадачи должны быть пустыми");
    }

    @Test
    void saveLoadTasksWithData() {
        Task task = new Task(0, "Test Task", "Test Description", "NEW");
        taskManager.createTask(task);

        Epic epic = new Epic(0, "Test Epic", "Test Epic Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Test Subtask", "Test Subtask Description", "NEW", epic.getId());
        taskManager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertEquals(1, loadedManager.getTasks().size(), "Неверное количество задач");
        assertEquals(1, loadedManager.getEpics().size(), "Неверное количество эпиков");
        assertEquals(1, loadedManager.getSubtasks().size(), "Неверное количество подзадач");

        Task loadedTask = loadedManager.getTask(task.getId());
        assertNotNull(loadedTask, "Задача не загрузилась");
        assertEquals(task.getTitle(), loadedTask.getTitle(), "Название задачи не совпадает");

        Epic loadedEpic = loadedManager.getEpic(epic.getId());
        assertNotNull(loadedEpic, "Эпик не загрузился");

        Subtask loadedSubtask = loadedManager.getSubtask(subtask.getId());
        assertNotNull(loadedSubtask, "Подзадача не загрузилась");
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId(), "EpicId подзадачи не совпадает");
    }

    @Test
    void saveLoadTasksWithTimeData() {
        Task task = new Task(0, "Task", "Description", "NEW",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0));
        taskManager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        Task loadedTask = loadedManager.getTask(task.getId());

        assertNotNull(loadedTask.getDuration(), "Продолжительность должна сохраниться");
        assertNotNull(loadedTask.getStartTime(), "Время начала должно сохраниться");
        assertEquals(task.getDuration(), loadedTask.getDuration(), "Продолжительность не совпадает");
        assertEquals(task.getStartTime(), loadedTask.getStartTime(), "Время начала не совпадает");
    }

    @Test
    void taskDataPersistance() {
        Task task = new Task(0, "Original", "Original Desc", "NEW");
        taskManager.createTask(task);

        task.setTitle("Changed");
        task.setDescription("Changed Desc");
        task.setStatus("DONE");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        Task loadedTask = loadedManager.getTask(task.getId());

        assertEquals("Original", loadedTask.getTitle(), "Название не сохранилось");
        assertEquals("Original Desc", loadedTask.getDescription(), "Описание не сохранилось");
        assertEquals("NEW", loadedTask.getStatus(), "Статус не сохранился");
    }

    @Test
    void epicStatusCalculationAfterLoad() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "NEW", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "NEW", epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        subtask1.setStatus("DONE");
        taskManager.updateSubtask(subtask1);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        Epic loadedEpic = loadedManager.getEpic(epic.getId());

        assertEquals("IN_PROGRESS", loadedEpic.getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void fileExistsAfterOperations() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);
        assertTrue(testFile.exists(), "Файл должен существовать после создания задачи");

        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);
        assertTrue(testFile.exists(), "Файл должен существовать после создания эпика");

        taskManager.deleteTask(task.getId());
        assertTrue(testFile.exists(), "Файл должен существовать после удаления задачи");
    }

    @Test
    void loadFromNonExistentFile() {
        File nonExistentFile = new File("non_existent_file.csv");
        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(nonExistentFile);
        }, "Должно выбрасываться исключение при загрузке из несуществующего файла");
    }

    @Test
    void saveToReadOnlyFile() {
        File readOnlyFile = new File(tempDir, "read_only.csv");
        try {
            Files.write(readOnlyFile.toPath(), new byte[0], StandardOpenOption.CREATE);
            readOnlyFile.setReadOnly();

            FileBackedTaskManager manager = new FileBackedTaskManager(
                    Managers.getDefaultHistory(), readOnlyFile);

            assertThrows(ManagerSaveException.class, () -> {
                Task task = new Task(0, "Task", "Description", "NEW");
                manager.createTask(task);
            }, "Должно выбрасываться исключение при сохранении в недоступный файл");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            readOnlyFile.setWritable(true);
        }
    }

    @Test
    void loadCorruptedFile() {
        try {
            Files.write(testFile.toPath(), "corrupted,data\n".getBytes());

            assertThrows(ManagerSaveException.class, () -> {
                FileBackedTaskManager.loadFromFile(testFile);
            }, "Должно выбрасываться исключение при загрузке поврежденного файла");
        } catch (IOException e) {
            fail("Не удалось создать поврежденный файл для теста");
        }
    }

    @Test
    void saveLoadWithEmptyFields() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        Task loadedTask = loadedManager.getTask(task.getId());

        assertNotNull(loadedTask, "Задача должна загрузиться даже с пустыми полями времени");
    }
}