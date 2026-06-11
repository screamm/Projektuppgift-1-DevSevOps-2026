package se.devsecops.backend.model;

public class GetProfileResponse {

    private String username;
    private String email;
    private String message;

    public GetProfileResponse() {
    }

    public GetProfileResponse(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public GetProfileResponse(String message) {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
