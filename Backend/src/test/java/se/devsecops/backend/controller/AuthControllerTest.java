package se.devsecops.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.LoginRequest;
import se.devsecops.backend.model.LoginResponse;
import se.devsecops.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerReturnsCreatedWhenUserIsCreated() {
        CreateUserRequest request =
            new CreateUserRequest("Test User", "test@example.com", "password1");
        CreateUserResponse expectedResponse =
            new CreateUserResponse(false, "User created");
        when(userService.createUser(request)).thenReturn(expectedResponse);

        ResponseEntity<CreateUserResponse> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(userService).createUser(request);
    }

    @Test
    void registerReturnsConflictWhenUserExists() {
        CreateUserRequest request =
            new CreateUserRequest("Test User", "test@example.com", "password1");
        CreateUserResponse expectedResponse =
            new CreateUserResponse(true, "User already exists");
        when(userService.createUser(request)).thenReturn(expectedResponse);

        ResponseEntity<CreateUserResponse> response = authController.register(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void loginReturnsOkWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("test@example.com", "password1");
        LoginResponse expectedResponse =
            new LoginResponse(true, "Login successful", "test@example.com");
        when(userService.login(request)).thenReturn(expectedResponse);

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(userService).login(request);
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong-password");
        LoginResponse expectedResponse =
            new LoginResponse(false, "Invalid email or password");
        when(userService.login(request)).thenReturn(expectedResponse);

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }
}
