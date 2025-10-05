import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Task Manager HTTP Server");

        try {
            TaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());

            Task task = new Task(0, "Первая задача", "Описание первой задачи", "NEW");
            Task created = manager.createTask(task);
            System.out.println("Created task with ID: " + created.getId());

            Task task2 = new Task(0, "Вторая задача", "Еще одна тестовая задача", "IN_PROGRESS");
            Task created2 = manager.createTask(task2);
            System.out.println("Created task with ID: " + created2.getId());

            HttpTaskServer server = new HttpTaskServer(manager);
            server.start();

            System.out.println(" Server is running on port 8081.");
            System.out.println(" Open in browser: http://localhost:8081/tasks");

            Thread.currentThread().join();

        } catch (IOException e) {
            System.err.println(" Server error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Server interrupted");
        } catch (Exception e) {
            System.err.println(" Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}