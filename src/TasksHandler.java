import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = GsonFactory.createGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        System.out.println("Handling " + method + " " + path);

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendBadRequest(exchange, "Method " + method + " not allowed");
            }
        } catch (Exception e) {
            System.err.println("Error in TasksHandler: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange, e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {

            var tasks = taskManager.getTasks();
            System.out.println("Found " + tasks.size() + " tasks");
            String response = gson.toJson(tasks);
            System.out.println("Sending response: " + response);
            sendSuccess(exchange, response);

        } else if (path.matches("/tasks/\\d+")) {
            // Получить задачу по ID
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Task task = taskManager.getTask(id);
            if (task != null) {
                String response = gson.toJson(task);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readText(exchange);
            System.out.println("POST body: " + body);

            if (body == null || body.trim().isEmpty()) {
                sendBadRequest(exchange, "Empty request body");
                return;
            }

            Task task = gson.fromJson(body, Task.class);

            if (task.getId() == 0) {
                // новая задача
                Task createdTask = taskManager.createTask(task);
                String response = gson.toJson(createdTask);
                sendCreated(exchange, response);
            } else {
                // Обновление существующей задачи
                taskManager.updateTask(task);
                String response = gson.toJson(task);
                sendCreated(exchange, response);
            }

        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("пересекается")) {
                sendHasInteractions(exchange);
            } else {
                sendBadRequest(exchange, e.getMessage());
            }
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            // Удалить все задачи
            taskManager.deleteTasks();
            sendSuccess(exchange, "{\"message\": \"All tasks deleted\"}");

        } else if (path.matches("/tasks/\\d+")) {
            // удаление
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Task task = taskManager.getTask(id);
            if (task != null) {
                taskManager.deleteTask(id);
                sendSuccess(exchange, "{\"message\": \"Task " + id + " deleted\"}");
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}