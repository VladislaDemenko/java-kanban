import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    @Test
    void addTasksToHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        Task task2 = new Task(2, "Task 2", "Description", "NEW");
        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "Неверное количество задач в истории");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task2, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void removeDuplicates() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task(1, "Task", "Description", "NEW");

        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "Должны быть только уникальные задачи");
        assertEquals(task, history.get(0), "Задача не совпадает");
    }

    @Test
    void shouldRemoveTask() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        Task task2 = new Task(2, "Task 2", "Description", "NEW");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "Неверное количество задач после удаления");
        assertEquals(task2, history.get(0), "Оставшаяся задача не совпадает");
    }

    @Test
    void preserveTaskData() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task(1, "Task", "Description", "NEW");

        historyManager.add(task);
        task.setStatus("DONE");

        Task historyTask = historyManager.getHistory().get(0);

        assertEquals("NEW", historyTask.getStatus(), "Статус задачи в истории изменился");
    }

    @Test
    void handleEmptyHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void notAddNullTask() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        historyManager.add(null);

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История не должна содержать null задачи");
    }
}