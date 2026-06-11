package se.devsecops.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.devsecops.backend.model.ChangePasswordRequest;
import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.DashboardSummaryResponse;
import se.devsecops.backend.model.DeleteUserResponse;
import se.devsecops.backend.model.GetProfileResponse;
import se.devsecops.backend.model.ListUsersResponse;
import se.devsecops.backend.model.LoginRequest;
import se.devsecops.backend.model.LoginResult;
import se.devsecops.backend.model.ProfileResponse;
import se.devsecops.backend.model.Task;
import se.devsecops.backend.model.UpdatePasswordRequest;
import se.devsecops.backend.model.UpdatePasswordResponse;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.model.UpdateProfileResponse;
import se.devsecops.backend.model.UpdateTaskRequest;
import se.devsecops.backend.model.UserResponse;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void createUserCreatesNewUser() {
        CreateUserResponse response = createUser("Test User", "test@example.com");

        assertFalse(response.isExists());
        assertEquals("User created", response.getMessage());
    }

    @Test
    void createUserRejectsDuplicateEmailIgnoringCase() {
        createUser("First User", "test@example.com");

        CreateUserResponse response = createUser("Second User", "TEST@EXAMPLE.COM");

        assertTrue(response.isExists());
        assertEquals("User already exists", response.getMessage());
    }

    @Test
    void createUserRejectsShortPassword() {
        CreateUserResponse response = userService.createUser(
            new CreateUserRequest("Test User", "test@example.com", "short")
        );

        assertEquals("Password must be at least 8 characters", response.getMessage());
    }

    @Test
    void loginSucceedsAndReturnsNormalizedEmail() {
        createUser("Test User", "test@example.com");

        LoginResult response = userService.login(
            new LoginRequest("TEST@EXAMPLE.COM", "password1")
        );

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void loginFailsForUnknownEmailOrIncorrectPassword() {
        LoginResult missing = userService.login(
            new LoginRequest("missing@example.com", "password1")
        );
        createUser("Test User", "test@example.com");
        LoginResult incorrect = userService.login(
            new LoginRequest("test@example.com", "wrong-password")
        );

        assertFalse(missing.isSuccess());
        assertFalse(incorrect.isSuccess());
        assertEquals("Invalid email or password", incorrect.getMessage());
    }

    @Test
    void getProfileReturnsUserAndValidationErrors() {
        createUser("Test User", "test@example.com");

        GetProfileResponse profile = userService.getProfile("TEST@EXAMPLE.COM");

        assertEquals("Test User", profile.getUsername());
        assertEquals("test@example.com", profile.getEmail());
        assertEquals(
            "Email address is invalid",
            userService.getProfile("not-an-email").getMessage()
        );
        assertEquals(
            "User not found",
            userService.getProfile("missing@example.com").getMessage()
        );
    }

    @Test
    void updateProfileUpdatesUsernameAndEmail() {
        createUser("Test User", "test@example.com");

        UpdateProfileResponse response = userService.updateProfile(
            new UpdateProfileRequest(
                "test@example.com",
                "Updated User",
                "updated@example.com"
            )
        );

        assertTrue(response.isSuccess());
        assertEquals("updated@example.com", response.getEmail());
        assertEquals(
            "Updated User",
            userService.getProfile("updated@example.com").getUsername()
        );
    }

    @Test
    void updateProfileRejectsInvalidValuesAndDuplicateEmail() {
        createUser("First User", "first@example.com");
        createUser("Second User", "second@example.com");

        UpdateProfileResponse emptyName = userService.updateProfile(
            new UpdateProfileRequest("first@example.com", " ", "first@example.com")
        );
        UpdateProfileResponse invalidEmail = userService.updateProfile(
            new UpdateProfileRequest("first@example.com", "First User", "invalid")
        );
        UpdateProfileResponse duplicateEmail = userService.updateProfile(
            new UpdateProfileRequest(
                "first@example.com",
                "First User",
                "second@example.com"
            )
        );

        assertEquals("Name cannot be empty", emptyName.getMessage());
        assertEquals("Email address is invalid", invalidEmail.getMessage());
        assertEquals("Email is already in use", duplicateEmail.getMessage());
    }

    @Test
    void settingsProfileCanUpdateUsername() {
        createUser("Test User", "test@example.com");

        ProfileResponse response = userService.updateProfile(
            "TEST@EXAMPLE.COM",
            new UpdateProfileRequest("Updated User")
        );

        assertTrue(response.isSuccess());
        assertEquals("Updated User", response.getUsername());
        assertTrue(userService.getSettingsProfile("test@example.com").isSuccess());
    }

    @Test
    void listAndGetUsersDoNotExposePasswords() {
        createUser("Test User", "test@example.com");

        ListUsersResponse users = userService.listUsers();
        UserResponse user = userService.getUser("TEST@EXAMPLE.COM");

        assertEquals(1, users.getUsers().size());
        assertEquals("Test User", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void deleteUserRemovesUserAndTasks() {
        createUser("Test User", "test@example.com");

        DeleteUserResponse response = userService.deleteUser("test@example.com");

        assertTrue(response.isSuccess());
        assertEquals("User not found", userService.getProfile("test@example.com").getMessage());
        assertTrue(userService.getTasks("test@example.com").isEmpty());
    }

    @Test
    void bothPasswordApisChangePassword() {
        createUser("Test User", "test@example.com");

        UpdatePasswordResponse firstResponse = userService.updatePassword(
            "test@example.com",
            new UpdatePasswordRequest("password1", "newpassword")
        );
        boolean secondResponse = userService.changePassword(
            "test@example.com",
            new ChangePasswordRequest("newpassword", "final-password")
        ).isSuccess();

        assertTrue(firstResponse.isSuccess());
        assertTrue(secondResponse);
        assertTrue(userService.login(
            new LoginRequest("test@example.com", "final-password")
        ).isSuccess());
    }

    @Test
    void updatePasswordRejectsIncorrectCurrentPassword() {
        createUser("Test User", "test@example.com");

        UpdatePasswordResponse response = userService.updatePassword(
            "test@example.com",
            new UpdatePasswordRequest("wrong-password", "newpassword")
        );

        assertFalse(response.isSuccess());
        assertEquals("Current password is incorrect", response.getMessage());
    }

    @Test
    void dashboardSummaryUsesCurrentTaskCounts() {
        createUser("Test User", "test@example.com");

        DashboardSummaryResponse response =
            userService.getDashboardSummary("test@example.com");

        assertEquals("Test User", response.getUsername());
        assertEquals("active", response.getAccountStatus());
        assertEquals(1, response.getUserCount());
        assertEquals(3, response.getTasksToday());
        assertEquals(0, response.getCompleted());
        assertEquals(3, response.getRemaining());
    }

    @Test
    void taskCanBeUpdated() {
        createUser("Test User", "test@example.com");

        Task task = userService.updateTask(
            "test@example.com",
            1,
            new UpdateTaskRequest("Ship settings page", "High", true)
        );

        assertNotNull(task);
        assertEquals("Ship settings page", task.getTitle());
        assertEquals("High", task.getPriority());
        assertTrue(task.isCompleted());
    }

    private CreateUserResponse createUser(String username, String email) {
        return userService.createUser(
            new CreateUserRequest(username, email, "password1")
        );
    }
}
