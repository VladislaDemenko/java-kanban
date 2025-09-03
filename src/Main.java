public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создаем задачи
        Task task1 = new Task(0, "Помыть посуду", "Помыть всю посуду вечером", "NEW");
        manager.createTask(task1);

        Epic epic1 = new Epic(0, "Переезд", "Организовать переезд в другой город");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Собрать коробки", "Купить и собрать коробки для переезда",
                "NEW", epic1.getId());
        manager.createSubtask(subtask1);

        // Просматриваем задачи несколько раз
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        manager.getTask(task1.getId()); // Повторный просмотр

        // Печатаем все задачи и историю
        printAllTasks(manager);
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