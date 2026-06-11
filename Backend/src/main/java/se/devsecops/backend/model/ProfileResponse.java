package se.devsecops.backend.model;

public class ProfileResponse extends ApiResponse {

    private String username;
    private String email;

    public ProfileResponse() {
    }

    public ProfileResponse(boolean success, String message, String username, String email) {
        super(success, message);
        this.username = username;
        this.email = email;
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
