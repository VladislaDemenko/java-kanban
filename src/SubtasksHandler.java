import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = GsonFactory.createGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET":
                    if (path.equals("/subtasks")) {
                        String response = gson.toJson(taskManager.getSubtasks());
                        sendSuccess(exchange, response);
                    } else if (path.matches("/subtasks/\\d+")) {
                        String[] parts = path.split("/");
                        int id = Integer.parseInt(parts[2]);
                        Subtask subtask = taskManager.getSubtask(id);
                        if (subtask != null) {
                            sendSuccess(exchange, gson.toJson(subtask));
                        } else {
                            sendNotFound(exchange);
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                default:
                    sendBadRequest(exchange, "Method not allowed");
            }
        } catch (Exception e) {
            sendInternalError(exchange, e.getMessage());
        }
    }
}