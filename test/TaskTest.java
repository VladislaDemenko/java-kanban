import java.util.List;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
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
    void taskNotEqualDifferentClass() {
        Task task = new Task(1, "Task", "Description", "NEW");
        String notTask = "Not a task";
        assertNotEquals(task, notTask, "Задача не должна быть равна объекту другого класса");
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
    void testTaskEndTimeCalculation() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        Duration duration = Duration.ofHours(2);
        Task task = new Task(1, "Task", "Description", "NEW", duration, startTime);

        LocalDateTime expectedEndTime = LocalDateTime.of(2024, 1, 15, 12, 0);
        assertEquals(expectedEndTime, task.getEndTime(), "Время окончания рассчитано неверно");
    }

    @Test
    void testTaskEndTimeWithoutStartTime() {
        Task task = new Task(1, "Task", "Description", "NEW");
        assertNull(task.getEndTime(), "Время окончания должно быть null при отсутствии времени начала");
    }

    @Test
    void testTaskEndTimeWithoutDuration() {
        Task task = new Task(1, "Task", "Description", "NEW");
        task.setStartTime(LocalDateTime.now());
        assertNull(task.getEndTime(), "Время окончания должно быть null при отсутствии продолжительности");
    }

    @Test
    void testEpicTimeCalculation() {
        Epic epic = new Epic(1, "Epic", "Description");

        Subtask subtask1 = new Subtask(2, "Subtask 1", "Description", "NEW", 1,
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 15, 10, 0));
        Subtask subtask2 = new Subtask(3, "Subtask 2", "Description", "NEW", 1,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 13, 0));

        epic.calculateTimes(List.of(subtask1, subtask2));

        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), epic.getStartTime(),
                "Время начала эпика должно быть временем начала самой ранней подзадачи");
        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 0), epic.getEndTime(),
                "Время окончания эпика должно быть временем окончания самой поздней подзадачи");
        assertEquals(Duration.ofHours(3), epic.getDuration(),
                "Продолжительность эпика должна быть суммой продолжительностей подзадач");
    }

    @Test
    void testEpicTimeCalculationWithEmptySubtasks() {
        Epic epic = new Epic(1, "Epic", "Description");
        epic.calculateTimes(List.of());

        assertNull(epic.getStartTime(), "Время начала должно быть null при отсутствии подзадач");
        assertNull(epic.getEndTime(), "Время окончания должно быть null при отсутствии подзадач");
        assertEquals(Duration.ZERO, epic.getDuration(),
                "Продолжительность должна быть нулевой при отсутствии подзадач");
    }

    @Test
    void testHashCodeConsistency() {
        Task task1 = new Task(1, "Task", "Description", "NEW");
        Task task2 = new Task(1, "Task", "Description", "NEW");

        assertEquals(task1.hashCode(), task2.hashCode(),
                "Хэш-коды должны быть одинаковыми для задач с одинаковым id");
    }

    @Test
    void testToStringFormat() {
        Task task = new Task(1, "Test Task", "Test Description", "NEW");
        String toString = task.toString();

        assertTrue(toString.contains("id=1"), "toString должен содержать id");
        assertTrue(toString.contains("title='Test Task'"), "toString должен содержать title");
        assertTrue(toString.contains("status='NEW'"), "toString должен содержать status");
    }
}