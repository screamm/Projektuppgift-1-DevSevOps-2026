package se.devsecops.backend.tasks;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskRequest {

    @NotBlank(message = "Title must not be empty")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @NotNull(message = "Priority must not be null")
    private Priority priority;

    private LocalDate dueDate;

    private Boolean completed;

    public TaskRequest() {
    }

    public TaskRequest(String title, Priority priority, LocalDate dueDate, Boolean completed) {
        this.title = title;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
