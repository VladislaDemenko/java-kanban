import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            File tempFile = File.createTempFile("tasks", ".csv");
            TaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile);

            Task task1 = new Task(0, "Помыть посуду", "Помыть всю посуду вечером", "NEW");
            manager.createTask(task1);

            Epic epic1 = new Epic(0, "Переезд", "Организовать переезд в другой город");
            manager.createEpic(epic1);

            Subtask subtask1 = new Subtask(0, "Собрать коробки", "Купить и собрать коробки для переезда",
                    "NEW", epic1.getId());
            manager.createSubtask(subtask1);

            manager.getTask(task1.getId());
            manager.getEpic(epic1.getId());
            manager.getSubtask(subtask1.getId());
            manager.getTask(task1.getId()); // Повторный просмотр

            // печатаем все задачи и историю
            printAllTasks(manager);

            // загрузка из файла
            System.out.println("\n=== Загрузка из файла ===");
            TaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
            printAllTasks(loadedManager);

        } catch (IOException e) {
            System.out.println("Ошибка при создании временного файла: " + e.getMessage());
        }
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}