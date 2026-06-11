package se.devsecops.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import se.devsecops.backend.model.ApiResponse;
import se.devsecops.backend.model.ChangePasswordRequest;
import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.DashboardSummaryResponse;
import se.devsecops.backend.model.DeleteUserResponse;
import se.devsecops.backend.model.GetProfileResponse;
import se.devsecops.backend.model.ListUsersResponse;
import se.devsecops.backend.model.LoginRequest;
import se.devsecops.backend.model.LoginResponse;
import se.devsecops.backend.model.ProfileResponse;
import se.devsecops.backend.model.Task;
import se.devsecops.backend.model.UpdatePasswordRequest;
import se.devsecops.backend.model.UpdatePasswordResponse;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.model.UpdateProfileResponse;
import se.devsecops.backend.model.UpdateTaskRequest;
import se.devsecops.backend.model.User;
import se.devsecops.backend.model.UserResponse;

@Service
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, List<Task>> tasks = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CreateUserResponse createUser(CreateUserRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isEmpty()) {
            return new CreateUserResponse(false, "Name cannot be empty");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return new CreateUserResponse(false, "Email is required");
        }

        String email = normalizeEmail(request.getEmail());
        if (!isValidEmail(email)) {
            return new CreateUserResponse(false, "Email address is invalid");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            return new CreateUserResponse(false, "Password must be at least 8 characters");
        }
        if (users.containsKey(email)) {
            return new CreateUserResponse(true, "User already exists");
        }

        users.put(email, new User(username, email, passwordEncoder.encode(request.getPassword())));
        tasks.put(email, createDefaultTasks());
        return new CreateUserResponse(false, "User created");
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return new LoginResponse(false, "Email is required");
        }

        String email = normalizeEmail(request.getEmail());
        if (!isValidEmail(email)) {
            return new LoginResponse(false, "Email address is invalid");
        }

        User user = users.get(email);
        if (user == null || request.getPassword() == null
            || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new LoginResponse(false, "Invalid email or password");
        }
        return new LoginResponse(true, "Login successful", user.getEmail());
    }

    public GetProfileResponse getProfile(String email) {
        String error = getUserLookupError(email);
        if (error != null) {
            return new GetProfileResponse(error);
        }

        User user = users.get(normalizeEmail(email));
        return new GetProfileResponse(user.getUsername(), user.getEmail());
    }

    public ProfileResponse getSettingsProfile(String email) {
        GetProfileResponse profile = getProfile(email);
        if (profile.getUsername() == null) {
            return new ProfileResponse(false, profile.getMessage(), null, null);
        }
        return new ProfileResponse(
            true,
            "Profile loaded",
            profile.getUsername(),
            profile.getEmail()
        );
    }

    public UpdateProfileResponse updateProfile(UpdateProfileRequest request) {
        if (request.getCurrentEmail() == null || request.getCurrentEmail().isBlank()) {
            return new UpdateProfileResponse(false, "Current email is required");
        }

        String currentEmail = normalizeEmail(request.getCurrentEmail());
        User existingUser = users.get(currentEmail);
        if (existingUser == null) {
            return new UpdateProfileResponse(false, "User not found");
        }

        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isEmpty()) {
            return new UpdateProfileResponse(false, "Name cannot be empty");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return new UpdateProfileResponse(false, "Email is required");
        }

        String newEmail = normalizeEmail(request.getEmail());
        if (!isValidEmail(newEmail)) {
            return new UpdateProfileResponse(false, "Email address is invalid");
        }
        if (!currentEmail.equals(newEmail) && users.containsKey(newEmail)) {
            return new UpdateProfileResponse(false, "Email is already in use");
        }

        users.remove(currentEmail);
        users.put(newEmail, new User(username, newEmail, existingUser.getPassword()));
        List<Task> userTasks = tasks.remove(currentEmail);
        if (userTasks != null) {
            tasks.put(newEmail, userTasks);
        }
        return new UpdateProfileResponse(true, "Profile updated", newEmail);
    }

    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = users.get(normalizeEmail(email));
        if (user == null) {
            return new ProfileResponse(false, "User not found", null, null);
        }

        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isEmpty()) {
            return new ProfileResponse(
                false,
                "Username is required",
                user.getUsername(),
                user.getEmail()
            );
        }

        user.setUsername(username);
        return new ProfileResponse(true, "Username updated", username, user.getEmail());
    }

    public ListUsersResponse listUsers() {
        List<UserResponse> responses = users.values().stream()
            .map(this::toUserResponse)
            .toList();
        return new ListUsersResponse(responses);
    }

    public UserResponse getUser(String email) {
        if (getUserLookupError(email) != null) {
            return null;
        }
        return toUserResponse(users.get(normalizeEmail(email)));
    }

    public String getUserLookupError(String email) {
        if (email == null || email.isBlank()) {
            return "Email is required";
        }

        String normalizedEmail = normalizeEmail(email);
        if (!isValidEmail(normalizedEmail)) {
            return "Email address is invalid";
        }
        if (!users.containsKey(normalizedEmail)) {
            return "User not found";
        }
        return null;
    }

    public DeleteUserResponse deleteUser(String email) {
        String error = getUserLookupError(email);
        if (error != null) {
            return new DeleteUserResponse(false, error);
        }

        String normalizedEmail = normalizeEmail(email);
        users.remove(normalizedEmail);
        tasks.remove(normalizedEmail);
        return new DeleteUserResponse(true, "User deleted");
    }

    public UpdatePasswordResponse updatePassword(
        String email,
        UpdatePasswordRequest request
    ) {
        ApiResponse response = changePassword(
            email,
            new ChangePasswordRequest(request.getCurrentPassword(), request.getNewPassword())
        );
        return new UpdatePasswordResponse(response.isSuccess(), response.getMessage());
    }

    public ApiResponse changePassword(String email, ChangePasswordRequest request) {
        String error = getUserLookupError(email);
        if (error != null) {
            return new ApiResponse(false, error);
        }

        User user = users.get(normalizeEmail(email));
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            return new ApiResponse(false, "Current password is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            return new ApiResponse(false, "New password must be at least 8 characters");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return new ApiResponse(false, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return new ApiResponse(true, "Password updated");
    }

    public DashboardSummaryResponse getDashboardSummary(String email) {
        String error = getUserLookupError(email);
        if (error != null) {
            return new DashboardSummaryResponse(error);
        }

        String normalizedEmail = normalizeEmail(email);
        User user = users.get(normalizedEmail);
        List<Task> userTasks = tasks.getOrDefault(normalizedEmail, List.of());
        int completed = (int) userTasks.stream().filter(Task::isCompleted).count();
        return new DashboardSummaryResponse(
            user.getUsername(),
            user.getEmail(),
            "active",
            users.size(),
            userTasks.size(),
            completed,
            userTasks.size() - completed
        );
    }

    public List<Task> getTasks(String email) {
        return new ArrayList<>(tasks.getOrDefault(normalizeEmail(email), List.of()));
    }

    public Task updateTask(String email, long taskId, UpdateTaskRequest request) {
        List<Task> userTasks = tasks.get(normalizeEmail(email));
        if (userTasks == null) {
            return null;
        }

        Task task = userTasks.stream()
            .filter(item -> item.getId() == taskId)
            .findFirst()
            .orElse(null);
        if (task == null || request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return null;
        }

        task.setTitle(request.getTitle().trim());
        task.setPriority(normalizePriority(request.getPriority()));
        task.setCompleted(request.isCompleted());
        return task;
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getUsername(), user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.toLowerCase().trim();
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private String normalizePriority(String priority) {
        if ("High".equalsIgnoreCase(priority)) {
            return "High";
        }
        if ("Low".equalsIgnoreCase(priority)) {
            return "Low";
        }
        return "Medium";
    }

    private List<Task> createDefaultTasks() {
        return new ArrayList<>(List.of(
            new Task(1, "Plan the next sprint", "High", false),
            new Task(2, "Review security checklist", "Medium", false),
            new Task(3, "Update project documentation", "Low", false)
        ));
    }
}
