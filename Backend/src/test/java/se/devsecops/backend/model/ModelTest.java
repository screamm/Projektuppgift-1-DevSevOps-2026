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
}
