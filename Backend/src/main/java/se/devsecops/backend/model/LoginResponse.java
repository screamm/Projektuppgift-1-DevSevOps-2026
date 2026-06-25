package se.devsecops.backend.model;

/**
 * Backward-compatible login response name for existing controllers and IDE state.
 */
public class LoginResponse extends LoginResult {

    public LoginResponse() {
    }

    public LoginResponse(boolean success, String message) {
        super(success, message);
    }

    public LoginResponse(boolean success, String message, String email) {
        super(success, message, email);
    }
}
