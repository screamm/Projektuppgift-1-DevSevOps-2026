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

import se.devsecops.backend.model.ApiResponse;
import se.devsecops.backend.model.ChangePasswordRequest;
import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.DeleteUserResponse;
import se.devsecops.backend.model.ListUsersResponse;
import se.devsecops.backend.model.ProfileResponse;
import se.devsecops.backend.model.UpdatePasswordRequest;
import se.devsecops.backend.model.UpdatePasswordResponse;
import se.devsecops.backend.model.UpdateProfileRequest;
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

    @Test
    void getSettingsProfileReturnsNotFoundWhenUserIsMissing() {
        ProfileResponse expectedResponse =
            new ProfileResponse(false, "User not found", null, null);
        when(userService.getSettingsProfile("missing@example.com"))
            .thenReturn(expectedResponse);

        ResponseEntity<ProfileResponse> response =
            userController.getSettingsProfile("missing@example.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void getSettingsProfileReturnsOkWhenUserExists() {
        ProfileResponse expectedResponse =
            new ProfileResponse(true, "Profile loaded", "Test User", "test@example.com");
        when(userService.getSettingsProfile("test@example.com"))
            .thenReturn(expectedResponse);

        ResponseEntity<ProfileResponse> response =
            userController.getSettingsProfile("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void updateSettingsProfileReturnsBadRequestWhenUsernameIsMissing() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        ProfileResponse expectedResponse =
            new ProfileResponse(false, "Username is required", "Old Name", "test@example.com");
        when(userService.updateProfile("test@example.com", request))
            .thenReturn(expectedResponse);

        ResponseEntity<ProfileResponse> response =
            userController.updateSettingsProfile("test@example.com", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void updateSettingsProfileReturnsNotFoundWhenUserIsMissing() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        ProfileResponse expectedResponse =
            new ProfileResponse(false, "User not found", null, null);
        when(userService.updateProfile("missing@example.com", request))
            .thenReturn(expectedResponse);

        ResponseEntity<ProfileResponse> response =
            userController.updateSettingsProfile("missing@example.com", request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void changePasswordReturnsNotFoundWhenUserIsMissing() {
        ChangePasswordRequest request =
            new ChangePasswordRequest("password1", "newpassword");
        ApiResponse expectedResponse = new ApiResponse(false, "User not found");
        when(userService.changePassword("missing@example.com", request))
            .thenReturn(expectedResponse);

        ResponseEntity<ApiResponse> response =
            userController.changePassword("missing@example.com", request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void changePasswordReturnsBadRequestWhenCurrentPasswordIsWrong() {
        ChangePasswordRequest request =
            new ChangePasswordRequest("wrongpassword", "newpassword");
        ApiResponse expectedResponse =
            new ApiResponse(false, "Current password is incorrect");
        when(userService.changePassword("test@example.com", request))
            .thenReturn(expectedResponse);

        ResponseEntity<ApiResponse> response =
            userController.changePassword("test@example.com", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }
}
