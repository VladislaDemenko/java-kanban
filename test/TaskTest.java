import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksSameIdEqual() {
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        Task task2 = new Task(1, "Task 2", "Another description", "DONE");
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

    @Test
    void subtasksSameIdEqual() {
        Subtask subtask1 = new Subtask(1, "Subtask 1", "Description", "NEW", 1);
        Subtask subtask2 = new Subtask(1, "Subtask 2", "Another description", "DONE", 2);
        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }

    @Test
    void taskNotEqualNull() {
        Task task = new Task(1, "Task", "Description", "NEW");
        assertNotEquals(null, task, "Задача не должна быть равна null");
    }

    @Test
    void epicContainSubtaskIds() {
        Epic epic = new Epic(1, "Epic", "Description");
        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        assertEquals(2, epic.getSubtaskIds().size(), "Эпик должен содержать 2 ID подзадач");
        assertTrue(epic.getSubtaskIds().contains(2), "Эпик должен содержать ID подзадачи 2");
        assertTrue(epic.getSubtaskIds().contains(3), "Эпик должен содержать ID подзадачи 3");
    }

    @Test
    void subtaskHaveEpicId() {
        Subtask subtask = new Subtask(1, "Subtask", "Description", "NEW", 5);
        assertEquals(5, subtask.getEpicId(), "Подзадача должна иметь правильный epicId");
    }

    @Test
    void testTaskToString() {
        Task task = new Task(1, "Test Task", "Test Description", "NEW");
        String expected = "Task{id=1, title='Test Task', description='Test Description', status='NEW'}";
        assertEquals(expected, task.toString(), "toString не совпадает");
    }

    @Test
    void testSubtaskToString() {
        Subtask subtask = new Subtask(1, "Test Subtask", "Test Description", "NEW", 5);
        String expected = "Subtask{id=1, title='Test Subtask', description='Test Description', status='NEW', epicId=5}";
        assertEquals(expected, subtask.toString(), "toString не совпадает");
    }

    @Test
    void testEpicToString() {
        Epic epic = new Epic(1, "Test Epic", "Test Description");
        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        String expected = "Epic{id=1, title='Test Epic', description='Test Description', status='NEW', subtaskIds=[2, 3]}";
        assertEquals(expected, epic.toString(), "toString не совпадает");
    }
}