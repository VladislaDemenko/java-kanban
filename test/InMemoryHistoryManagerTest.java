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
    void shouldNotExceedLimit() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        for (int i = 1; i <= 15; i++) {
            Task task = new Task(i, "Task " + i, "Description", "NEW");
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(10, history.size(), "История превысила лимит в 10 задач");
        assertEquals(6, history.get(0).getId(), "Первая задача в истории не совпадает");
        assertEquals(15, history.get(9).getId(), "Последняя задача в истории не совпадает");
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
}
