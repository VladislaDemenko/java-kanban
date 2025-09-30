import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager;
    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteTasks() {
        // Лямбда для удаления из истории
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        // Лямбда для удаления эпиков и подзадач
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(prioritizedTasks::remove);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        // Лямбда для удаления подзадач
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(prioritizedTasks::remove);
        subtasks.clear();

        // Обновление эпиков с помощью лямбда
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
            updateEpicTimes(epic.getId());
        });
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        Optional.ofNullable(task).ifPresent(historyManager::add);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        Optional.ofNullable(epic).ifPresent(historyManager::add);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        Optional.ofNullable(subtask).ifPresent(historyManager::add);
        return subtask;
    }

    @Override
    public Task createTask(Task task) {
        if (hasTimeOverlap(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующей задачей");
        }

        task.setId(nextId++);
        tasks.put(task.getId(), task);
        Optional.ofNullable(task.getStartTime())
                .ifPresent(st -> prioritizedTasks.add(task));
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            return null;
        }

        if (hasTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующей задачей");
        }

        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        Optional.ofNullable(subtask.getStartTime())
                .ifPresent(st -> prioritizedTasks.add(subtask));
        updateEpicStatus(subtask.getEpicId());
        updateEpicTimes(subtask.getEpicId());
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        Optional.ofNullable(tasks.get(task.getId()))
                .ifPresent(existingTask -> {
                    prioritizedTasks.remove(existingTask);

                    if (hasTimeOverlap(task)) {
                        prioritizedTasks.add(existingTask);
                        throw new IllegalArgumentException("Задача пересекается по времени с существующей задачей");
                    }

                    tasks.put(task.getId(), task);
                    Optional.ofNullable(task.getStartTime())
                            .ifPresent(st -> prioritizedTasks.add(task));
                });
    }

    @Override
    public void updateEpic(Epic epic) {
        Optional.ofNullable(epics.get(epic.getId()))
                .ifPresent(savedEpic -> {
                    savedEpic.setTitle(epic.getTitle());
                    savedEpic.setDescription(epic.getDescription());
                });
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Optional.ofNullable(subtasks.get(subtask.getId()))
                .ifPresent(savedSubtask -> {
                    prioritizedTasks.remove(savedSubtask);

                    if (hasTimeOverlap(subtask)) {
                        prioritizedTasks.add(savedSubtask);
                        throw new IllegalArgumentException("Подзадача пересекается по времени с существующей задачей");
                    }


                    handleEpicChange(savedSubtask, subtask);

                    subtasks.put(subtask.getId(), subtask);
                    Optional.ofNullable(subtask.getStartTime())
                            .ifPresent(st -> prioritizedTasks.add(subtask));
                    updateEpicStatus(subtask.getEpicId());
                    updateEpicTimes(subtask.getEpicId());
                });
    }

    private void handleEpicChange(Subtask savedSubtask, Subtask newSubtask) {
        if (savedSubtask.getEpicId() != newSubtask.getEpicId()) {
            Optional.ofNullable(epics.get(savedSubtask.getEpicId()))
                    .ifPresent(epic -> epic.removeSubtaskId(savedSubtask.getId()));

            Optional.ofNullable(epics.get(newSubtask.getEpicId()))
                    .ifPresent(epic -> epic.addSubtaskId(newSubtask.getId()));
        }
    }

    @Override
    public void deleteTask(int id) {
        Optional.ofNullable(tasks.remove(id))
                .ifPresent(task -> {
                    prioritizedTasks.remove(task);
                    historyManager.remove(id);
                });
    }

    @Override
    public void deleteEpic(int id) {
        Optional.ofNullable(epics.remove(id))
                .ifPresent(epic -> {
                    historyManager.remove(id);
                    // Лямбда для удаления всех подзадач эпика
                    epic.getSubtaskIds().forEach(subtaskId -> {
                        Optional.ofNullable(subtasks.remove(subtaskId))
                                .ifPresent(subtask -> {
                                    prioritizedTasks.remove(subtask);
                                    historyManager.remove(subtaskId);
                                });
                    });
                });
    }

    @Override
    public void deleteSubtask(int id) {
        Optional.ofNullable(subtasks.remove(id))
                .ifPresent(subtask -> {
                    prioritizedTasks.remove(subtask);
                    historyManager.remove(id);
                    Optional.ofNullable(epics.get(subtask.getEpicId()))
                            .ifPresent(epic -> {
                                epic.removeSubtaskId(id);
                                updateEpicStatus(epic.getId());
                                updateEpicTimes(epic.getId());
                            });
                });
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        return Optional.ofNullable(epics.get(epicId))
                .map(Epic::getSubtaskIds)
                .orElse(Collections.emptyList())
                .stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public boolean isTasksIntersect(Task task1, Task task2) {
        if (task1 == null || task2 == null || task1.equals(task2)) {
            return false;
        }

        return Optional.ofNullable(task1.getStartTime())
                .flatMap(start1 -> Optional.ofNullable(task1.getEndTime())
                        .flatMap(end1 -> Optional.ofNullable(task2.getStartTime())
                                .flatMap(start2 -> Optional.ofNullable(task2.getEndTime())
                                        .map(end2 -> start1.isBefore(end2) && start2.isBefore(end1)))))
                .orElse(false);
    }

    @Override
    public boolean hasTimeOverlap(Task task) {
        return prioritizedTasks.stream()
                .anyMatch(existingTask -> isTasksIntersect(task, existingTask));
    }

    protected void updateEpicStatus(int epicId) {
        Optional.ofNullable(epics.get(epicId))
                .ifPresent(epic -> {
                    List<Subtask> epicSubtasks = getEpicSubtasks(epicId);

                    if (epicSubtasks.isEmpty()) {
                        epic.setStatus("NEW");
                        return;
                    }

                    //проверки статусов
                    boolean allNew = epicSubtasks.stream()
                            .allMatch(subtask -> "NEW".equals(subtask.getStatus()));

                    boolean allDone = epicSubtasks.stream()
                            .allMatch(subtask -> "DONE".equals(subtask.getStatus()));

                    if (allNew) {
                        epic.setStatus("NEW");
                    } else if (allDone) {
                        epic.setStatus("DONE");
                    } else {
                        epic.setStatus("IN_PROGRESS");
                    }
                });
    }

    protected void updateEpicTimes(int epicId) {
        Optional.ofNullable(epics.get(epicId))
                .ifPresent(epic -> {
                    List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
                    epic.calculateTimes(epicSubtasks);
                });
    }

    public List<Task> findTasksByPredicate(java.util.function.Predicate<Task> predicate) {
        return getAllTasks().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public void forEachTask(java.util.function.Consumer<Task> action) {
        getAllTasks().forEach(action);
    }

    private List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(epics.values());
        allTasks.addAll(subtasks.values());
        return allTasks;
    }

    // группировки задач по статусу
    public Map<String, List<Task>> groupTasksByStatus() {
        return getAllTasks().stream()
                .collect(Collectors.groupingBy(
                        Task::getStatus,
                        Collectors.toList()
                ));
    }

    // фильтрации задач по времени
    public List<Task> getTasksInTimeRange(LocalDateTime start, LocalDateTime end) {
        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null && task.getEndTime() != null)
                .filter(task -> !task.getStartTime().isBefore(start) && !task.getEndTime().isAfter(end))
                .collect(Collectors.toList());
    }
}