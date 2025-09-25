import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(Managers.getDefaultHistory());
    }

    @Test
    void testInMemorySpecificFunctionality() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        // задача действительно хранится в памяти???
        assertTrue(taskManager.getTasks().contains(task),
                "Задача должна храниться в памяти");
    }

    @Test
    void testHistoryManagerIntegration() {
        Task task = new Task(0, "Task", "Description", "NEW");
        taskManager.createTask(task);

        taskManager.getTask(task.getId());
        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size(), "Задача должна быть в истории");
        assertEquals(task, history.get(0), "История должна содержать задачу");
    }
}