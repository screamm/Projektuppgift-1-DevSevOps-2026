package se.devsecops.backend.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ModelTest {

    @Test
    void createUserRequestSupportsConstructorAndSetters() {
        CreateUserRequest request =
            new CreateUserRequest("Initial User", "initial@example.com", "initial");

        assertAll(
            () -> assertEquals("Initial User", request.getUsername()),
            () -> assertEquals("initial@example.com", request.getEmail()),
            () -> assertEquals("initial", request.getPassword())
        );

        request.setUsername("Updated User");
        request.setEmail("updated@example.com");
        request.setPassword("updated");

        assertAll(
            () -> assertEquals("Updated User", request.getUsername()),
            () -> assertEquals("updated@example.com", request.getEmail()),
            () -> assertEquals("updated", request.getPassword())
        );
    }

    @Test
    void createUserResponseSupportsConstructorAndSetters() {
        CreateUserResponse response = new CreateUserResponse(true, "Initial");

        assertTrue(response.isExists());
        assertEquals("Initial", response.getMessage());

        response.setExists(false);
        response.setMessage("Updated");

        assertFalse(response.isExists());
        assertEquals("Updated", response.getMessage());
    }

    @Test
    void loginRequestSupportsConstructorAndSetters() {
        LoginRequest request = new LoginRequest("initial@example.com", "initial");

        assertEquals("initial@example.com", request.getEmail());
        assertEquals("initial", request.getPassword());

        request.setEmail("updated@example.com");
        request.setPassword("updated");

        assertEquals("updated@example.com", request.getEmail());
        assertEquals("updated", request.getPassword());
    }

    @Test
    void loginResponseSupportsConstructorAndSetters() {
        LoginResponse response = new LoginResponse(true, "Initial");

        assertTrue(response.isSuccess());
        assertEquals("Initial", response.getMessage());

        response.setSuccess(false);
        response.setMessage("Updated");

        assertFalse(response.isSuccess());
        assertEquals("Updated", response.getMessage());
    }

    @Test
    void userExposesConstructorValues() {
        User user = new User("Test User", "test@example.com", "secret");

        assertAll(
            () -> assertEquals("Test User", user.getUsername()),
            () -> assertEquals("test@example.com", user.getEmail()),
            () -> assertEquals("secret", user.getPassword())
        );
    }

    @Test
    void getProfileResponseSupportsConstructorAndSetters() {
        GetProfileResponse response =
            new GetProfileResponse("Test User", "test@example.com");

        assertEquals("Test User", response.getUsername());
        assertEquals("test@example.com", response.getEmail());

        response.setUsername("Updated User");
        response.setEmail("updated@example.com");
        response.setMessage("Updated");

        assertAll(
            () -> assertEquals("Updated User", response.getUsername()),
            () -> assertEquals("updated@example.com", response.getEmail()),
            () -> assertEquals("Updated", response.getMessage())
        );
    }

    @Test
    void updateProfileRequestSupportsConstructorAndSetters() {
        UpdateProfileRequest request =
            new UpdateProfileRequest("old@example.com", "Test User", "new@example.com");

        assertAll(
            () -> assertEquals("old@example.com", request.getCurrentEmail()),
            () -> assertEquals("Test User", request.getUsername()),
            () -> assertEquals("new@example.com", request.getEmail())
        );

        request.setCurrentEmail("current@example.com");
        request.setUsername("Updated User");
        request.setEmail("updated@example.com");

        assertAll(
            () -> assertEquals("current@example.com", request.getCurrentEmail()),
            () -> assertEquals("Updated User", request.getUsername()),
            () -> assertEquals("updated@example.com", request.getEmail())
        );
    }

    @Test
    void updateProfileResponseSupportsConstructorAndSetters() {
        UpdateProfileResponse response =
            new UpdateProfileResponse(true, "Profile updated", "test@example.com");

        assertTrue(response.isSuccess());
        assertEquals("Profile updated", response.getMessage());
        assertEquals("test@example.com", response.getEmail());

        response.setSuccess(false);
        response.setMessage("Updated");
        response.setEmail("updated@example.com");

        assertAll(
            () -> assertFalse(response.isSuccess()),
            () -> assertEquals("Updated", response.getMessage()),
            () -> assertEquals("updated@example.com", response.getEmail())
        );
    }
}
