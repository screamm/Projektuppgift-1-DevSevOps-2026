package se.devsecops.backend.model;

public class UpdateProfileRequest {

<<<<<<< HEAD
    private String currentEmail;
    private String username;
    private String email;
=======
    private String username;
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893

    public UpdateProfileRequest() {
    }

<<<<<<< HEAD
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
=======
    public UpdateProfileRequest(String username) {
        this.username = username;
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
<<<<<<< HEAD

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
=======
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893
}
