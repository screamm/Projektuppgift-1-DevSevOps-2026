package se.devsecops.backend.model;

public class UpdateTaskRequest {

    private String title;
    private String priority;
    private boolean completed;

    public UpdateTaskRequest() {
    }

    public UpdateTaskRequest(String title, String priority, boolean completed) {
        this.title = title;
        this.priority = priority;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
