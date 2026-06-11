package se.devsecops.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.DeleteUserResponse;
import se.devsecops.backend.model.ListUsersResponse;
import se.devsecops.backend.model.UpdatePasswordRequest;
import se.devsecops.backend.model.UpdatePasswordResponse;
import se.devsecops.backend.model.UserResponse;
import se.devsecops.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUserReturnsCreatedWhenUserIsCreated() {
        CreateUserRequest request =
            new CreateUserRequest("Test User", "test@example.com", "password1");
        CreateUserResponse expectedResponse =
            new CreateUserResponse(false, "User created");
        when(userService.createUser(request)).thenReturn(expectedResponse);

        ResponseEntity<CreateUserResponse> response = userController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(userService).createUser(request);
    }

    @Test
    void listUsersDelegatesToUserService() {
        ListUsersResponse expectedResponse =
            new ListUsersResponse(List.of(new UserResponse("Test User", "test@example.com")));
        when(userService.listUsers()).thenReturn(expectedResponse);

        ResponseEntity<ListUsersResponse> response = userController.listUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(userService).listUsers();
    }

    @Test
    void getUserReturnsOkWhenUserExists() {
        UserResponse expectedResponse = new UserResponse("Test User", "test@example.com");
        when(userService.getUser("test@example.com")).thenReturn(expectedResponse);

        ResponseEntity<?> response = userController.getUser("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void deleteUserReturnsOkWhenUserIsDeleted() {
        DeleteUserResponse expectedResponse = new DeleteUserResponse(true, "User deleted");
        when(userService.deleteUser("test@example.com")).thenReturn(expectedResponse);

        ResponseEntity<DeleteUserResponse> response =
            userController.deleteUser("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void updatePasswordDelegatesToUserService() {
        UpdatePasswordRequest request =
            new UpdatePasswordRequest("password1", "newpassword");
        UpdatePasswordResponse expectedResponse =
            new UpdatePasswordResponse(true, "Password updated");
        when(userService.updatePassword("test@example.com", request))
            .thenReturn(expectedResponse);

        ResponseEntity<UpdatePasswordResponse> response =
            userController.updatePassword("test@example.com", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(userService).updatePassword("test@example.com", request);
    }
}
