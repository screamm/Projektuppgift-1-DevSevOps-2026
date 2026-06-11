package se.devsecops.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import se.devsecops.backend.model.ApiResponse;
import se.devsecops.backend.model.ChangePasswordRequest;
import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.LoginRequest;
import se.devsecops.backend.model.LoginResponse;
import se.devsecops.backend.model.ProfileResponse;
import se.devsecops.backend.model.Task;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.model.UpdateTaskRequest;
import se.devsecops.backend.model.User;

@Service
public class UserService {

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, List<Task>> tasks = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CreateUserResponse createUser(CreateUserRequest request) {
        String email = request.getEmail().toLowerCase();

        if (users.containsKey(email)) {
            return new CreateUserResponse(true, "User already exists");
        }

        User user = new User(
            request.getUsername().trim(),
            email,
            passwordEncoder.encode(request.getPassword())
        );

        users.put(email, user);
        tasks.put(email, createDefaultTasks());

        return new CreateUserResponse(false, "User created");
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();

        if (!users.containsKey(email)) {
            return new LoginResponse(false, "Invalid email or password");
        }

        User user = users.get(email);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new LoginResponse(false, "Invalid email or password");
        }

        return new LoginResponse(true, "Login successful");
    }

    public ProfileResponse getProfile(String email) {
        User user = users.get(normalizeEmail(email));
        if (user == null) {
            return new ProfileResponse(false, "User not found", null, null);
        }

        return new ProfileResponse(true, "Profile loaded", user.getUsername(), user.getEmail());
    }

    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = users.get(normalizeEmail(email));
        if (user == null) {
            return new ProfileResponse(false, "User not found", null, null);
        }

        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isEmpty()) {
            return new ProfileResponse(false, "Username is required", user.getUsername(), user.getEmail());
        }

        user.setUsername(username);
        return new ProfileResponse(true, "Username updated", user.getUsername(), user.getEmail());
    }

    public ApiResponse changePassword(String email, ChangePasswordRequest request) {
        User user = users.get(normalizeEmail(email));
        if (user == null) {
            return new ApiResponse(false, "User not found");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return new ApiResponse(false, "Current password is incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            return new ApiResponse(false, "New password must be at least 8 characters");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return new ApiResponse(true, "Password updated");
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

    private String normalizeEmail(String email) {
        return email == null ? "" : email.toLowerCase();
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
