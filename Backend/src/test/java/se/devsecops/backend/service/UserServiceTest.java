package se.devsecops.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.DashboardSummaryResponse;
import se.devsecops.backend.model.DeleteUserResponse;
import se.devsecops.backend.model.GetProfileResponse;
import se.devsecops.backend.model.ListUsersResponse;
import se.devsecops.backend.model.LoginRequest;
import se.devsecops.backend.model.LoginResponse;
<<<<<<< HEAD
import se.devsecops.backend.model.UpdatePasswordRequest;
import se.devsecops.backend.model.UpdatePasswordResponse;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.model.UpdateProfileResponse;
import se.devsecops.backend.model.UserResponse;
=======
import se.devsecops.backend.model.ChangePasswordRequest;
import se.devsecops.backend.model.ProfileResponse;
import se.devsecops.backend.model.Task;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.model.UpdateTaskRequest;
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void createUserCreatesNewUser() {
        CreateUserResponse response = userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        assertFalse(response.isExists());
        assertEquals("User created", response.getMessage());
    }

    @Test
    void createUserRejectsDuplicateEmailIgnoringCase() {
        userService.createUser(
            new CreateUserRequest("First User", "test@example.com", "password1")
        );

        CreateUserResponse response = userService.createUser(
            new CreateUserRequest("Second User", "TEST@EXAMPLE.COM", "password2")
        );

        assertTrue(response.isExists());
        assertEquals("User already exists", response.getMessage());
    }

    @Test
    void loginSucceedsWithCorrectCredentialsIgnoringEmailCase() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        LoginResponse response = userService.login(
            new LoginRequest("TEST@EXAMPLE.COM", "password1")
        );

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void loginFailsForUnknownEmail() {
        LoginResponse response = userService.login(
            new LoginRequest("missing@example.com", "password1")
        );

        assertFalse(response.isSuccess());
        assertEquals("Invalid email or password", response.getMessage());
    }

    @Test
    void loginFailsForIncorrectPassword() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        LoginResponse response = userService.login(
            new LoginRequest("test@example.com", "wrong-password")
        );

        assertFalse(response.isSuccess());
        assertEquals("Invalid email or password", response.getMessage());
    }

    @Test
<<<<<<< HEAD
    void loginReturnsEmailOnSuccess() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        LoginResponse response = userService.login(
            new LoginRequest("test@example.com", "password1")
        );

        assertTrue(response.isSuccess());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void getProfileReturnsUserData() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        GetProfileResponse response = userService.getProfile("TEST@EXAMPLE.COM");

        assertEquals("Test User", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void getProfileRejectsInvalidEmail() {
        GetProfileResponse response = userService.getProfile("not-an-email");

        assertEquals("Email address is invalid", response.getMessage());
    }

    @Test
    void getProfileRejectsMissingUser() {
        GetProfileResponse response = userService.getProfile("missing@example.com");

        assertEquals("User not found", response.getMessage());
    }

    @Test
    void updateProfileUpdatesUsernameAndEmail() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        UpdateProfileResponse response = userService.updateProfile(
            new UpdateProfileRequest(
                "test@example.com",
                "Updated User",
                "updated@example.com"
            )
        );

        assertTrue(response.isSuccess());
        assertEquals("Profile updated", response.getMessage());
        assertEquals("updated@example.com", response.getEmail());

        GetProfileResponse profile = userService.getProfile("updated@example.com");
        assertEquals("Updated User", profile.getUsername());
    }

    @Test
    void updateProfileRejectsEmptyName() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        UpdateProfileResponse response = userService.updateProfile(
            new UpdateProfileRequest("test@example.com", "   ", "test@example.com")
        );

        assertFalse(response.isSuccess());
        assertEquals("Name cannot be empty", response.getMessage());
    }

    @Test
    void updateProfileRejectsInvalidEmail() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        UpdateProfileResponse response = userService.updateProfile(
            new UpdateProfileRequest("test@example.com", "Test User", "invalid-email")
        );

        assertFalse(response.isSuccess());
        assertEquals("Email address is invalid", response.getMessage());
    }

    @Test
    void updateProfileRejectsDuplicateEmail() {
        userService.createUser(
            new CreateUserRequest("First User", "first@example.com", "password1")
        );
        userService.createUser(
            new CreateUserRequest("Second User", "second@example.com", "password1")
        );

        UpdateProfileResponse response = userService.updateProfile(
            new UpdateProfileRequest(
                "first@example.com",
                "First User",
                "second@example.com"
            )
        );

        assertFalse(response.isSuccess());
        assertEquals("Email is already in use", response.getMessage());
    }

    @Test
    void createUserRejectsShortPassword() {
        CreateUserResponse response = userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "short")
        );

        assertFalse(response.isExists());
        assertEquals("Password must be at least 8 characters", response.getMessage());
    }

    @Test
    void listUsersReturnsAllUsersWithoutPasswords() {
        userService.createUser(
            new CreateUserRequest("First User", "first@example.com", "password1")
        );
        userService.createUser(
            new CreateUserRequest("Second User", "second@example.com", "password2")
        );

        ListUsersResponse response = userService.listUsers();

        assertEquals(2, response.getUsers().size());
        assertTrue(
            response.getUsers().stream()
                .anyMatch(user -> "first@example.com".equals(user.getEmail()))
        );
    }

    @Test
    void getUserReturnsUserWithoutPassword() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        UserResponse response = userService.getUser("TEST@EXAMPLE.COM");

        assertEquals("Test User", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void deleteUserRemovesUser() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        DeleteUserResponse response = userService.deleteUser("test@example.com");

        assertTrue(response.isSuccess());
        assertEquals("User not found", userService.getProfile("test@example.com").getMessage());
    }

    @Test
    void updatePasswordChangesPassword() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        UpdatePasswordResponse response = userService.updatePassword(
            "test@example.com",
            new UpdatePasswordRequest("password1", "newpassword")
        );

        assertTrue(response.isSuccess());

        LoginResponse loginResponse = userService.login(
            new LoginRequest("test@example.com", "newpassword")
        );
        assertTrue(loginResponse.isSuccess());
    }

    @Test
    void updatePasswordRejectsIncorrectCurrentPassword() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        UpdatePasswordResponse response = userService.updatePassword(
            "test@example.com",
            new UpdatePasswordRequest("wrong-password", "newpassword")
        );

        assertFalse(response.isSuccess());
        assertEquals("Current password is incorrect", response.getMessage());
    }

    @Test
    void getDashboardSummaryReturnsUserAndTaskOverview() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "password1")
        );

        DashboardSummaryResponse response =
            userService.getDashboardSummary("test@example.com");

        assertEquals("Test User", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("active", response.getAccountStatus());
        assertEquals(1, response.getUserCount());
        assertEquals(3, response.getTasksToday());
=======
    void profileAndPasswordCanBeUpdated() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "old-password")
        );

        ProfileResponse profile = userService.updateProfile(
            "TEST@EXAMPLE.COM",
            new UpdateProfileRequest("Updated User")
        );
        assertTrue(profile.isSuccess());
        assertEquals("Updated User", profile.getUsername());

        assertTrue(userService.changePassword(
            "test@example.com",
            new ChangePasswordRequest("old-password", "new-password")
        ).isSuccess());
        assertTrue(userService.login(
            new LoginRequest("test@example.com", "new-password")
        ).isSuccess());
    }

    @Test
    void taskCanBeUpdated() {
        userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "secret")
        );

        Task task = userService.updateTask(
            "test@example.com",
            1,
            new UpdateTaskRequest("Ship settings page", "High", true)
        );

        assertEquals("Ship settings page", task.getTitle());
        assertEquals("High", task.getPriority());
        assertTrue(task.isCompleted());
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893
    }
}
