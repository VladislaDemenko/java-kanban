import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }



    private static Task fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) {
            return null;
        }

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        String status = parts[3];
        String description = parts[4];

        switch (type) {
            case "TASK":
                return new Task(id, name, description, status);
            case "EPIC":
                return new Epic(id, name, description, status);
            case "SUBTASK":
                if (parts.length >= 6) {
                    int epicId = Integer.parseInt(parts[5]);
                    return new Subtask(id, name, description, status, epicId);
                }
                return null;
            default:
                return null;
        }
    }

}
