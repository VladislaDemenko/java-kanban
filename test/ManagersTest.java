import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void returnInitialTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Менеджер задач не инициализирован");
        assertTrue(taskManager instanceof InMemoryTaskManager, "Должен возвращаться InMemoryTaskManager");
    }

    @Test
    void returnInitialHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории не инициализирован");
        assertTrue(historyManager instanceof InMemoryHistoryManager, "Должен возвращаться InMemoryHistoryManager");
    }

    @Test
    void returnFileBackedTaskManager() {
        File tempFile = new File("test.csv");
        TaskManager taskManager = Managers.getFileBackedTaskManager(tempFile);
        assertNotNull(taskManager, "FileBackedTaskManager не инициализирован");
        assertTrue(taskManager instanceof FileBackedTaskManager, "Должен возвращаться FileBackedTaskManager");
    }
}