import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldAddAndFindTask() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Task savedTask = taskManager.getTask(task.getId());
        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task.getTitle(), savedTask.getTitle(), "Название задачи не совпадает");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описание задачи не совпадает");
    }

    @Test
    void shouldNotConflictWithGeneratedIds() {
        Task task1 = new Task(0, "Task 1", "Description", "NEW");
        taskManager.createTask(task1);

        Task task2 = new Task(0, "Task 2", "Description", "NEW");
        taskManager.createTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "ID задач не должны конфликтовать");
    }

    @Test
    void shouldPreserveTaskFieldsWhenAdded() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Task savedTask = taskManager.getTask(task.getId());
        assertEquals(task.getTitle(), savedTask.getTitle(), "Название задачи изменилось");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описание задачи изменилось");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статус задачи изменился");
    }

    @Test
    void shouldAddTasksToHistory() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Subtask", "Description", "NEW", epic.getId());
        taskManager.createSubtask(subtask);

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size(), "Неверное количество задач в истории");
        assertEquals(task, history.get(0), "Первая задача в истории не совпадает");
        assertEquals(epic, history.get(1), "Вторая задача в истории не совпадает");
        assertEquals(subtask, history.get(2), "Третья задача в истории не совпадает");
    }

    @Test
    void shouldRemoveTasksFromHistoryWhenDeleted() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Subtask", "Description", "NEW", epic.getId());
        taskManager.createSubtask(subtask);

        // Добавляем в историю
        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask.getId());

        // Удаляем задачу
        taskManager.deleteTask(task.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "Задача не удалилась из истории");
        assertFalse(history.contains(task), "Удаленная задача осталась в истории");
    }

    @Test
    void shouldUpdateEpicStatusAutomatically() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "NEW", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "NEW", epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals("NEW", epic.getStatus(), "Статус эпика должен быть NEW");

        // Меняем статус одной подзадачи
        subtask1.setStatus("DONE");
        taskManager.updateSubtask(subtask1);

        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals("IN_PROGRESS", updatedEpic.getStatus(), "Статус эпика должен быть IN_PROGRESS");

        // Меняем статус второй подзадачи
        subtask2.setStatus("DONE");
        taskManager.updateSubtask(subtask2);

        updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals("DONE", updatedEpic.getStatus(), "Статус эпика должен быть DONE");
    }

    @Test
    void shouldDeleteAllSubtasksWhenEpicDeleted() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "NEW", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "NEW", epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpic(epic.getId()), "Эпик не удалился");
        assertNull(taskManager.getSubtask(subtask1.getId()), "Подзадача 1 не удалилась");
        assertNull(taskManager.getSubtask(subtask2.getId()), "Подзадача 2 не удалилась");
    }

    @Test
    void shouldHandleEmptyTaskLists() {
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void shouldReturnEpicSubtasks() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "NEW", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "NEW", epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic.getId());
        assertEquals(2, epicSubtasks.size(), "Неверное количество подзадач эпика");
        assertTrue(epicSubtasks.contains(subtask1), "Подзадача 1 не найдена в списке эпика");
        assertTrue(epicSubtasks.contains(subtask2), "Подзадача 2 не найдена в списке эпика");
    }

    // Дополнительные тесты для полного покрытия
    @Test
    void testDeleteTasks() {
        Task task1 = new Task(0, "Task 1", "Description", "NEW");
        Task task2 = new Task(0, "Task 2", "Description", "NEW");
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.deleteTasks();
        assertTrue(taskManager.getTasks().isEmpty(), "Все задачи должны быть удалены");
    }

    @Test
    void testDeleteSubtasks() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "NEW", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "NEW", epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        taskManager.deleteSubtasks();
        assertTrue(taskManager.getSubtasks().isEmpty(), "Все подзадачи должны быть удалены");
        assertTrue(epic.getSubtaskIds().isEmpty(), "Список подзадач эпика должен быть пустым");
    }

    @Test
    void testUpdateTask() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Task updatedTask = new Task(task.getId(), "Updated Task", "Updated Description", "DONE");
        taskManager.updateTask(updatedTask);

        Task savedTask = taskManager.getTask(task.getId());
        assertEquals("Updated Task", savedTask.getTitle(), "Название задачи не обновилось");
        assertEquals("Updated Description", savedTask.getDescription(), "Описание задачи не обновилось");
        assertEquals("DONE", savedTask.getStatus(), "Статус задачи не обновился");
    }
}