import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        Task task2 = new Task(1, "Task 2", "Another description", "DONE");

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask(1, "Subtask 1", "Description", "NEW", 1);
        Subtask subtask2 = new Subtask(1, "Subtask 2", "Another description", "DONE", 2);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }

    @Test
    void taskShouldNotBeEqualToNull() {
        Task task = new Task(1, "Task", "Description", "NEW");

        assertNotEquals(null, task, "Задача не должна быть равна null");
    }
}