import org.junit.jupiter.api.Test;
import java.util.List;
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
    void removeFromBeginning() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        Task task2 = new Task(2, "Task 2", "Description", "NEW");
        Task task3 = new Task(3, "Task 3", "Description", "NEW");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач после удаления из начала");
        assertEquals(task2, history.get(0), "Первая задача не совпадает");
        assertEquals(task3, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void removeFromMiddle() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        Task task2 = new Task(2, "Task 2", "Description", "NEW");
        Task task3 = new Task(3, "Task 3", "Description", "NEW");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач после удаления из середины");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task3, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void removeFromEnd() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        Task task2 = new Task(2, "Task 2", "Description", "NEW");
        Task task3 = new Task(3, "Task 3", "Description", "NEW");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач после удаления с конца");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task2, history.get(1), "Вторая задача не совпадает");
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

    @Test
    void removeNonExistentTask() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task(1, "Task", "Description", "NEW");
        historyManager.add(task);

        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна измениться при удалении несуществующей задачи");
    }

    @Test
    void historyOrderPreservation() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task1 = new Task(1, "Task 1", "Description", "NEW");
        Task task2 = new Task(2, "Task 2", "Description", "NEW");
        Task task3 = new Task(3, "Task 3", "Description", "NEW");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Должно быть 3 уникальные задачи");
        assertEquals(task2, history.get(0), "Первая задача не совпадает");
        assertEquals(task3, history.get(1), "Вторая задача не совпадает");
        assertEquals(task1, history.get(2), "Третья задача не совпадает");
    }
}