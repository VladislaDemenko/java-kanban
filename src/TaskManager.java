import java.util.List;

interface TaskManager {
    List<Task> getTasks();
    List<Epic> getEpics();
    List<Subtask> getSubtasks();
    void deleteTasks();
    void deleteEpics();
    void deleteSubtasks();
    Task getTask(int id);
    Epic getEpic(int id);
    Subtask getSubtask(int id);
    Task createTask(Task task);
    Epic createEpic(Epic epic);
    Subtask createSubtask(Subtask subtask);
    void updateTask(Task task);
    void updateEpic(Epic epic);
    void updateSubtask(Subtask subtask);
    void deleteTask(int id);
    void deleteEpic(int id);
    void deleteSubtask(int id);
    List<Subtask> getEpicSubtasks(int epicId);
    List<Task> getHistory();
}