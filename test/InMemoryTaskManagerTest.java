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
        Task task = new Task(1, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Task savedTask = taskManager.getTask(task.getId());

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");
    }

    @Test
    void shouldNotConflictWithGeneratedAndGivenIds() {
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        taskManager.createTask(task1);

        Task task2 = new Task(0, "Task 2", "Description", "NEW");
        taskManager.createTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "ID задач не должны конфликтовать");
    }

    @Test
    void shouldNotAddSubtaskToItselfAsEpic() {
        Epic epic = new Epic(1, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(2, "Subtask", "Description", "NEW", epic.getId());
        taskManager.createSubtask(subtask);

        assertThrows(IllegalArgumentException.class, () -> {
            subtask.setEpicId(subtask.getId());
            taskManager.updateSubtask(subtask);
        }, "Подзадача не может быть своим же эпиком");
    }

    @Test
    void shouldPreserveTaskFieldsWhenAdded() {
        Task task = new Task(1, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Task savedTask = taskManager.getTask(task.getId());

        assertEquals(task.getTitle(), savedTask.getTitle(), "Название задачи изменилось");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описание задачи изменилось");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статус задачи изменился");
    }

    @Test
    void shouldAddTasksToHistory() {
        Task task = new Task(1, "Task", "Description", "NEW");
        taskManager.createTask(task);

        Epic epic = new Epic(2, "Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(3, "Subtask", "Description", "NEW", epic.getId());
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

    @org.junit.jupiter.api.Test
    void getTasks() {
    }

    @org.junit.jupiter.api.Test
    void getEpics() {
    }

    @org.junit.jupiter.api.Test
    void getSubtasks() {
    }

    @org.junit.jupiter.api.Test
    void deleteTasks() {
    }

    @org.junit.jupiter.api.Test
    void deleteEpics() {
    }

    @org.junit.jupiter.api.Test
    void deleteSubtasks() {
    }

    @org.junit.jupiter.api.Test
    void getTask() {
    }

    @org.junit.jupiter.api.Test
    void getEpic() {
    }

    @org.junit.jupiter.api.Test
    void getSubtask() {
    }

    @org.junit.jupiter.api.Test
    void createTask() {
    }

    @org.junit.jupiter.api.Test
    void createEpic() {
    }

    @org.junit.jupiter.api.Test
    void createSubtask() {
    }

    @org.junit.jupiter.api.Test
    void updateTask() {
    }

    @org.junit.jupiter.api.Test
    void updateEpic() {
    }

    @org.junit.jupiter.api.Test
    void updateSubtask() {
    }

    @org.junit.jupiter.api.Test
    void deleteTask() {
    }

    @org.junit.jupiter.api.Test
    void deleteEpic() {
    }

    @org.junit.jupiter.api.Test
    void deleteSubtask() {
    }

    @org.junit.jupiter.api.Test
    void getEpicSubtasks() {
    }

    @org.junit.jupiter.api.Test
    void getHistory() {
    }
}