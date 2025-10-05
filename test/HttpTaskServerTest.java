import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private HttpTaskServer taskServer;
    private TaskManager taskManager;
    private final Gson gson = GsonFactory.createGson();

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager(new InMemoryHistoryManager());
        taskServer = new HttpTaskServer(taskManager);
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    void testGetTasks() throws IOException {
        // Добавляем тестовую задачу
        Task task = new Task(0, "Test Task", "Test Description", "NEW");
        taskManager.createTask(task);

        // Отправляем GET запрос
        URL url = new URL("http://localhost:8081/tasks");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode(), "Должен вернуться статус 200");

        // Читаем ответ
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.readLine();
        reader.close();

        assertNotNull(response, "Ответ не должен быть null");
        assertTrue(response.contains("Test Task"), "Ответ должен содержать название задачи");
    }

    @Test
    void testCreateTask() throws IOException {
        // задача для отправки
        Task task = new Task(0, "New Task", "New Description", "NEW");
        String taskJson = gson.toJson(task);

        // POST запрос
        URL url = new URL("http://localhost:8081/tasks");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        OutputStream os = connection.getOutputStream();
        os.write(taskJson.getBytes(StandardCharsets.UTF_8));
        os.close();

        assertEquals(201, connection.getResponseCode(), "Должен вернуться статус 201");

        // задача добавилась?
        assertEquals(1, taskManager.getTasks().size(), "Задача должна быть добавлена");
    }

    @Test
    void testGetTaskById() throws IOException {
        // Добавляем задачу
        Task task = new Task(0, "Test Task", "Test Description", "NEW");
        Task created = taskManager.createTask(task);

        // задача по ID
        URL url = new URL("http://localhost:8081/tasks/" + created.getId());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode(), "Должен вернуться статус 200");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.readLine();
        reader.close();

        assertTrue(response.contains("Test Task"), "Ответ должен содержать задачу");
    }

    @Test
    void testGetNonExistentTask() throws IOException {
        URL url = new URL("http://localhost:8081/tasks/999");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        assertEquals(404, connection.getResponseCode(), "Должен вернуться статус 404 для несуществующей задачи");
    }

    @Test
    void testDeleteTask() throws IOException {
        // + задачу
        Task task = new Task(0, "Test Task", "Test Description", "NEW");
        Task created = taskManager.createTask(task);

        // - задачу
        URL url = new URL("http://localhost:8081/tasks/" + created.getId());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");

        assertEquals(200, connection.getResponseCode(), "Должен вернуться статус 200");

        assertNull(taskManager.getTask(created.getId()), "Задача должна быть удалена");
    }
}