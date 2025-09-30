import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            reader.readLine(); // Пропускаем заголовок

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
                        if (subtask.getStartTime() != null) {
                            manager.prioritizedTasks.add(subtask);
                        }
                    } else {
                        manager.tasks.put(task.getId(), task);
                        if (task.getId() >= manager.nextId) {
                            manager.nextId = task.getId() + 1;
                        }
                        if (task.getStartTime() != null) {
                            manager.prioritizedTasks.add(task);
                        }
                    }
                }
            }


            for (Epic epic : manager.epics.values()) {
                manager.updateEpicTimes(epic.getId());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла", e);
        }
        return manager;
    }

    void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic,duration,startTime");
            writer.newLine();

            for (Task task : tasks.values()) {
                writer.write(toString(task));
                writer.newLine();
            }

            for (Epic epic : epics.values()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            for (Subtask subtask : subtasks.values()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    private String toString(Task task) {
        String durationStr = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().toString() : "";

        if (task instanceof Epic) {
            return String.format("%d,EPIC,%s,%s,%s,,%s,%s",
                    task.getId(), task.getTitle(), task.getStatus(), task.getDescription(),
                    durationStr, startTimeStr);
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return String.format("%d,SUBTASK,%s,%s,%s,%d,%s,%s",
                    subtask.getId(), subtask.getTitle(), subtask.getStatus(),
                    subtask.getDescription(), subtask.getEpicId(),
                    durationStr, startTimeStr);
        } else {
            return String.format("%d,TASK,%s,%s,%s,,%s,%s",
                    task.getId(), task.getTitle(), task.getStatus(), task.getDescription(),
                    durationStr, startTimeStr);
        }
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",", -1);
        if (parts.length < 5) {
            return null;
        }

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        String status = parts[3];
        String description = parts[4];

        Duration duration = null;
        LocalDateTime startTime = null;

        if (parts.length >= 7) {
            if (!parts[6].isEmpty()) {
                duration = Duration.ofMinutes(Long.parseLong(parts[6]));
            }
            if (!parts[7].isEmpty()) {
                startTime = LocalDateTime.parse(parts[7]);
            }
        }

        switch (type) {
            case "TASK":
                return new Task(id, name, description, status, duration, startTime);
            case "EPIC":
                Epic epic = new Epic(id, name, description, status);
                if (duration != null) epic.setDuration(duration);
                if (startTime != null) epic.setStartTime(startTime);
                return epic;
            case "SUBTASK":
                if (parts.length >= 6 && !parts[5].isEmpty()) {
                    int epicId = Integer.parseInt(parts[5]);
                    return new Subtask(id, name, description, status, epicId, duration, startTime);
                }
                return null;
            default:
                return null;
        }
    }

    @Override
    public Task createTask(Task task) {
        Task result = super.createTask(task);
        save();
        return result;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic result = super.createEpic(epic);
        save();
        return result;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask result = super.createSubtask(subtask);
        save();
        return result;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }
}