import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList history = new CustomLinkedList();

    @Override
    public void add(Task task) {
        if (task != null) {
            history.linkLast(task);
        }
    }

    @Override
    public void remove(int id) {
        history.removeNode(id);
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    private static class CustomLinkedList {
        private Node<Task> head;
        private Node<Task> tail;
        private final Map<Integer, Node<Task>> nodeMap = new HashMap<>();
        private int size = 0;

        public void linkLast(Task task) {
            removeNode(task.getId());

            Node<Task> newNode = new Node<>(tail, task, null);
            if (tail == null) {
                head = newNode;
            } else {
                tail.next = newNode;
            }
            tail = newNode;
            nodeMap.put(task.getId(), newNode);
            size++;
        }

        public List<Task> getTasks() {
            List<Task> result = new ArrayList<>();
            Node<Task> current = head;
            while (current != null) {
                result.add(current.data);
                current = current.next;
            }
            return result;
        }

        public void removeNode(int taskId) {
            Node<Task> node = nodeMap.remove(taskId);
            if (node == null) return;

            if (node.prev != null) {
                node.prev.next = node.next;
            } else {
                head = node.next;
            }

            if (node.next != null) {
                node.next.prev = node.prev;
            } else {
                tail = node.prev;
            }

            size--;
        }

        private static class Node<T> {
            T data;
            Node<T> next;
            Node<T> prev;

            Node(Node<T> prev, T data, Node<T> next) {
                this.data = data;
                this.next = next;
                this.prev = prev;
            }
        }
    }
}