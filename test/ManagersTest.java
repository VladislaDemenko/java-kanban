import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void returnInitialTaskMg() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Менеджер задач не инициализирован");
        assertTrue(taskManager instanceof InMemoryTaskManager, "Должен возвращаться InMemoryTaskManager");
    }

    @Test
    void returnInitialHistoryMg() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории не инициализирован");
        assertTrue(historyManager instanceof InMemoryHistoryManager, "Должен возвращаться InMemoryHistoryManager");
    }
}