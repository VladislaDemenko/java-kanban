import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        try {
            File tempFile = File.createTempFile("tasks", ".csv");
            TaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile);

            //задачи с временными интервалами
            Task task1 = new Task(0, "Помыть посуду", "Помыть всю посуду вечером", "NEW",
                    Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 15, 18, 0));
            manager.createTask(task1);

            Epic epic1 = new Epic(0, "Переезд", "Организовать переезд в другой город");
            manager.createEpic(epic1);

            Subtask subtask1 = new Subtask(0, "Собрать коробки", "Купить и собрать коробки для переезда",
                    "NEW", epic1.getId(), Duration.ofHours(2),
                    LocalDateTime.of(2024, 1, 16, 10, 0));
            manager.createSubtask(subtask1);

            Subtask subtask2 = new Subtask(0, "Упаковать вещи", "Упаковать все вещи в коробки",
                    "IN_PROGRESS", epic1.getId(), Duration.ofHours(4),
                    LocalDateTime.of(2024, 1, 16, 14, 0));
            manager.createSubtask(subtask2);

            System.out.println("=== Приоритетный список задач ===");
            manager.getPrioritizedTasks().forEach(System.out::println);

            // Тестируем историю
            manager.getTask(task1.getId());
            manager.getEpic(epic1.getId());
            manager.getSubtask(subtask1.getId());

            System.out.println("\n=== История ===");
            manager.getHistory().forEach(System.out::println);

            // Тестируем загрузку из файла
            System.out.println("\n=== Загрузка из файла ===");
            TaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
            printAllTasks(loadedManager);

        } catch (IOException e) {
            System.out.println("Ошибка при создании временного файла: " + e.getMessage());
        }
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        manager.getTasks().forEach(System.out::println);

        System.out.println("\nЭпики:");
        manager.getEpics().forEach(epic -> {
            System.out.println(epic);
            manager.getEpicSubtasks(epic.getId()).forEach(subtask ->
                    System.out.println("--> " + subtask));
        });

        System.out.println("\nПодзадачи:");
        manager.getSubtasks().forEach(System.out::println);

        System.out.println("\nПриоритетный список:");
        manager.getPrioritizedTasks().forEach(System.out::println);
    }
}