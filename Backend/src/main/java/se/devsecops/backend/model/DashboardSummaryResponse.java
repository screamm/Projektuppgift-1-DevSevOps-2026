package se.devsecops.backend.model;

public class DashboardSummaryResponse {

    private String username;
    private String email;
    private String accountStatus;
    private int userCount;
    private int tasksToday;
    private int completed;
    private int remaining;
    private String message;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(
        String username,
        String email,
        String accountStatus,
        int userCount,
        int tasksToday,
        int completed,
        int remaining
    ) {
        this.username = username;
        this.email = email;
        this.accountStatus = accountStatus;
        this.userCount = userCount;
        this.tasksToday = tasksToday;
        this.completed = completed;
        this.remaining = remaining;
    }

    public DashboardSummaryResponse(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getTasksToday() {
        return tasksToday;
    }

    public void setTasksToday(int tasksToday) {
        this.tasksToday = tasksToday;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
