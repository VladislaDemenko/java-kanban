import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    @Test
    void shouldAddTasksToHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        Task task2 = new Task(2, "Task 2", "Description", "NEW");
        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "Неверное количество задач в истории");
        assertEquals(task1, history.get(0), "Первая задача в истории не совпадает");
        assertEquals(task2, history.get(1), "Вторая задача в истории не совпадает");
    }

    @Test
    void shouldRemoveDuplicatesFromHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task(1, "Task", "Description", "NEW");

        // Добавляем задачу несколько раз
        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "В истории должны быть только уникальные задачи");
        assertEquals(task, history.get(0), "Задача в истории не совпадает");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        Task task2 = new Task(2, "Task 2", "Description", "NEW");

        historyManager.add(task1);
        historyManager.add(task2);

        // Удаляем первую задачу
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "Неверное количество задач в истории после удаления");
        assertEquals(task2, history.get(0), "Оставшаяся задача в истории не совпадает");
    }

    @Test
    void shouldPreserveTaskDataInHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task(1, "Task", "Description", "NEW");

        historyManager.add(task);
        task.setStatus("DONE");

        Task historyTask = historyManager.getHistory().get(0);

        assertEquals("NEW", historyTask.getStatus(), "Статус задачи в истории изменился");
    }

    @Test
    void shouldHandleEmptyHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void shouldNotAddNullTask() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        historyManager.add(null);

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История не должна содержать null задачи");
    }
}