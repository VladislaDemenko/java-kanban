import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        if (text == null) {
            text = "";
        }
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }

    protected void sendSuccess(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 200);
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 201);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Not Found\"}", 404);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Task has time interactions with existing tasks\"}", 406);
    }

    protected void sendInternalError(HttpExchange exchange, String message) throws IOException {
        String errorMessage = message != null ? message : "Internal Server Error";
        sendText(exchange, "{\"error\": \"" + escapeJson(errorMessage) + "\"}", 500);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        String errorMessage = message != null ? message : "Bad Request";
        sendText(exchange, "{\"error\": \"" + escapeJson(errorMessage) + "\"}", 400);
    }

    protected String readText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}