package se.devsecops.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.LoginRequest;
import se.devsecops.backend.model.LoginResponse;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void createUserCreatesNewUser() {
        CreateUserResponse response = userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "secret")
        );

        assertFalse(response.isExists());
        assertEquals("User created", response.getMessage());
    }

    @Test
    void createUserRejectsDuplicateEmailIgnoringCase() {
        userService.createUser(
            new CreateUserRequest("First User", "test@example.com", "secret")
        );

        CreateUserResponse response = userService.createUser(
            new CreateUserRequest("Second User", "TEST@EXAMPLE.COM", "other")
        );

        assertTrue(response.isExists());
        assertEquals("User already exists", response.getMessage());
    }

    @Test
    void loginSucceedsWithCorrectCredentialsIgnoringEmailCase() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "secret")
        );

        LoginResponse response = userService.login(
            new LoginRequest("TEST@EXAMPLE.COM", "secret")
        );

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void loginFailsForUnknownEmail() {
        LoginResponse response = userService.login(
            new LoginRequest("missing@example.com", "secret")
        );

        assertFalse(response.isSuccess());
        assertEquals("Invalid email or password", response.getMessage());
    }

    @Test
    void loginFailsForIncorrectPassword() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "secret")
        );

        LoginResponse response = userService.login(
            new LoginRequest("test@example.com", "wrong-password")
        );

        assertFalse(response.isSuccess());
        assertEquals("Invalid email or password", response.getMessage());
    }
}
