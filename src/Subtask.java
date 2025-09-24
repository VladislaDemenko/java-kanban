import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int id, String title, String description, String status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String dedescription, String status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(id, title, dedescription, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", duration=" + (duration != null ? duration.toMinutes() + "min" : "null") +
                ", startTime=" + startTime +
                ", epicId=" + epicId +
                '}';
    }
}