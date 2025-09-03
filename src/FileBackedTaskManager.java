import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            // Пропускаем заголовок
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                Task task = fromString(line);
                if (task != null) {
                    if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                        if (task.getId() >= manager.nextId) {
                            manager.nextId = task.getId() + 1;
                        }
                    } else if (task instanceof Subtask) {
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(subtask.getId(), subtask);
                        if (subtask.getId() >= manager.nextId) {
                            manager.nextId = subtask.getId() + 1;
                        }
                        // Добавляем подзадачу в эпик
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtaskId(subtask.getId());
                        }
                    } else {
                        manager.tasks.put(task.getId(), task);
                        if (task.getId() >= manager.nextId) {
                            manager.nextId = task.getId() + 1;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла", e);
        }
        return manager;
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
