package se.devsecops.backend.model;

public class UpdateProfileRequest {

    private String currentEmail;
    private String username;
    private String email;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String currentEmail, String username, String email) {
        this.currentEmail = currentEmail;
        this.username = username;
        this.email = email;
    }

    public String getCurrentEmail() {
        return currentEmail;
    }

    public void setCurrentEmail(String currentEmail) {
        this.currentEmail = currentEmail;
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
}
