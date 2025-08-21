public class Subtask extends Task {
    private int epicId;

    public Subtask(int id, String title, String description, String status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", epicId=" + epicId +
                '}';
    }
}