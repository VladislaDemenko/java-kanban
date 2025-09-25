import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    void addAndFindTask() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Task savedTask = taskManager.getTask(task.getId());
        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task.getTitle(), savedTask.getTitle(), "Название не совпадает");
    }

    @Test
    void notConflictIds() {
        Task task1 = new Task(0, "Task 1", "Description", "NEW");
        taskManager.createTask(task1);

        Task task2 = new Task(0, "Task 2", "Description", "NEW");
        taskManager.createTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "ID не должны конфликтовать");
    }

    @Test
    void preserveTaskFields() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Task savedTask = taskManager.getTask(task.getId());
        assertEquals(task.getTitle(), savedTask.getTitle(), "Название изменилось");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описание изменилось");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статус изменился");
    }

    @Test
    void addToHistory() {
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
        assertEquals(3, history.size(), "Неверное количество в истории");
    } 

    @Test
    void removeFromHistory() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());

        taskManager.deleteTask(task.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "Задача не удалилась из истории");
        assertFalse(history.contains(task), "Удаленная задача осталась в истории");
    }

    @Test
    void updateEpicStatusAllNew() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "NEW", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "NEW", epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals("NEW", epic.getStatus(), "Статус должен быть NEW когда все подзадачи NEW");
    }

    @Test
    void updateEpicStatusAllDone() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "DONE", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "DONE", epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals("DONE", epic.getStatus(), "Статус должен быть DONE когда все подзадачи DONE");
    }

    @Test
    void updateEpicStatusMixedNewDone() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "NEW", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "DONE", epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals("IN_PROGRESS", epic.getStatus(),
                "Статус должен быть IN_PROGRESS когда подзадачи NEW и DONE");
    }

    @Test
    void updateEpicStatusInProgress() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "IN_PROGRESS", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "IN_PROGRESS", epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals("IN_PROGRESS", epic.getStatus(),
                "Статус должен быть IN_PROGRESS когда подзадачи IN_PROGRESS");
    }

    @Test
    void deleteSubtasksWithEpic() {
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
    void handleEmptyLists() {
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void returnEpicSubtasks() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "NEW", epic.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "NEW", epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic.getId());
        assertEquals(2, epicSubtasks.size(), "Неверное количество подзадач");
        assertTrue(epicSubtasks.contains(subtask1), "Подзадача 1 не найдена");
        assertTrue(epicSubtasks.contains(subtask2), "Подзадача 2 не найдена");
    }

    @Test
    void testDeleteAllTasks() {
        Task task1 = new Task(0, "Task 1", "Description", "NEW");
        Task task2 = new Task(0, "Task 2", "Description", "NEW");
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.deleteTasks();
        assertTrue(taskManager.getTasks().isEmpty(), "Все задачи должны быть удалены");
    }

    @Test
    void testDeleteAllSubtasks() {
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
        assertEquals("Updated Task", savedTask.getTitle(), "Название не обновилось");
        assertEquals("Updated Description", savedTask.getDescription(), "Описание не обновилось");
        assertEquals("DONE", savedTask.getStatus(), "Статус не обновился");
    }

    @Test
    void testSubtaskHasEpic() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Subtask", "Description", "NEW", epic.getId());
        taskManager.createSubtask(subtask);

        assertNotNull(taskManager.getEpic(epic.getId()), "Эпик должен существовать");
        assertEquals(epic.getId(), subtask.getEpicId(), "Подзадача должна ссылаться на эпик");
    }

    @Test
    void testTasksTimeIntersection() {
        Task task1 = new Task(0, "Task 1", "Description", "NEW",
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 15, 10, 0));
        Task task2 = new Task(0, "Task 2", "Description", "NEW",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 11, 0));

        taskManager.createTask(task1);

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createTask(task2);
        }, "Должно быть исключение при пересечении времени задач");
    }

    @Test
    void testNoTimeIntersection() {
        Task task1 = new Task(0, "Task 1", "Description", "NEW",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0));
        Task task2 = new Task(0, "Task 2", "Description", "NEW",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 12, 0));

        taskManager.createTask(task1);
        assertDoesNotThrow(() -> taskManager.createTask(task2),
                "Не должно быть исключения при непересекающемся времени");
    }

    @Test
    void testGetPrioritizedTasks() {
        Task task1 = new Task(0, "Task 1", "Description", "NEW",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 12, 0));
        Task task2 = new Task(0, "Task 2", "Description", "NEW",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0));

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size(), "Должно быть 2 задачи в приоритетном списке");
        assertEquals(task2.getId(), prioritized.get(0).getId(),
                "Первой должна быть задача с более ранним временем");
    }

    @Test
    void testTasksWithoutTimeNotInPrioritized() {
        Task taskWithTime = new Task(0, "Task with time", "Description", "NEW",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0));
        Task taskWithoutTime = new Task(0, "Task without time", "Description", "NEW");

        taskManager.createTask(taskWithTime);
        taskManager.createTask(taskWithoutTime);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(1, prioritized.size(), "Только задачи с временем должны быть в приоритетном списке");
        assertEquals(taskWithTime.getId(), prioritized.get(0).getId());
    }

    @Test
    void testEpicTimeCalculation() {
        Epic epic = new Epic(0, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description", "NEW", epic.getId(),
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 15, 10, 0));
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description", "NEW", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 13, 0));

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epic.getId());
        assertNotNull(savedEpic.getStartTime(), "У эпика должно быть время начала");
        assertNotNull(savedEpic.getEndTime(), "У эпика должно быть время окончания");
        assertEquals(Duration.ofHours(3), savedEpic.getDuration(),
                "Продолжительность эпика должна быть суммой продолжительностей подзадач");
    }
}