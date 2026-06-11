package se.devsecops.backend.service;

import java.util.ArrayList;
<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893
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
<<<<<<< HEAD
import se.devsecops.backend.model.UpdatePasswordRequest;
import se.devsecops.backend.model.UpdatePasswordResponse;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.model.UpdateProfileResponse;
=======
import se.devsecops.backend.model.ProfileResponse;
import se.devsecops.backend.model.Task;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.model.UpdateTaskRequest;
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893
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
        String username = request.getUsername() == null
            ? ""
            : request.getUsername().trim();

        if (username.isEmpty()) {
            return new CreateUserResponse(false, "Name cannot be empty");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return new CreateUserResponse(false, "Email is required");
        }

        String email = request.getEmail().toLowerCase().trim();

        if (!isValidEmail(email)) {
            return new CreateUserResponse(false, "Email address is invalid");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            return new CreateUserResponse(false, "Password must be at least 8 characters");
        }

        if (users.containsKey(email)) {
            return new CreateUserResponse(true, "User already exists");
        }

<<<<<<< HEAD
        User user = new User(username, email, request.getPassword());
=======
        User user = new User(
            request.getUsername().trim(),
            email,
            passwordEncoder.encode(request.getPassword())
        );

>>>>>>> ee89d5791ff178cc678277138af071e0a049a893
        users.put(email, user);
        tasks.put(email, createDefaultTasks());

        return new CreateUserResponse(false, "User created");
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return new LoginResponse(false, "Email is required");
        }

        String email = request.getEmail().toLowerCase().trim();

        if (!isValidEmail(email)) {
            return new LoginResponse(false, "Email address is invalid");
        }

        if (!users.containsKey(email)) {
            return new LoginResponse(false, "Invalid email or password");
        }

        User user = users.get(email);

<<<<<<< HEAD
        if (request.getPassword() == null || !user.getPassword().equals(request.getPassword())) {
=======
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893
            return new LoginResponse(false, "Invalid email or password");
        }

        return new LoginResponse(true, "Login successful", user.getEmail());
    }

<<<<<<< HEAD
    public GetProfileResponse getProfile(String email) {
        if (email == null || email.isBlank()) {
            return new GetProfileResponse("Email is required");
        }

        String normalizedEmail = email.toLowerCase().trim();

        if (!isValidEmail(normalizedEmail)) {
            return new GetProfileResponse("Email address is invalid");
        }

        User user = users.get(normalizedEmail);

        if (user == null) {
            return new GetProfileResponse("User not found");
        }

        return toProfileResponse(user);
    }

    public UpdateProfileResponse updateProfile(UpdateProfileRequest request) {
        if (request.getCurrentEmail() == null || request.getCurrentEmail().isBlank()) {
            return new UpdateProfileResponse(false, "Current email is required");
        }

        String currentEmail = request.getCurrentEmail().toLowerCase().trim();

        if (!users.containsKey(currentEmail)) {
            return new UpdateProfileResponse(false, "User not found");
        }

        String username = request.getUsername() == null
            ? ""
            : request.getUsername().trim();

        if (username.isEmpty()) {
            return new UpdateProfileResponse(false, "Name cannot be empty");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return new UpdateProfileResponse(false, "Email is required");
        }

        String newEmail = request.getEmail().toLowerCase().trim();

        if (!isValidEmail(newEmail)) {
            return new UpdateProfileResponse(false, "Email address is invalid");
        }

        if (!currentEmail.equals(newEmail) && users.containsKey(newEmail)) {
            return new UpdateProfileResponse(false, "Email is already in use");
        }

        User existingUser = users.get(currentEmail);
        User updatedUser = new User(username, newEmail, existingUser.getPassword());

        users.remove(currentEmail);
        users.put(newEmail, updatedUser);

        return new UpdateProfileResponse(true, "Profile updated", newEmail);
    }

    public ListUsersResponse listUsers() {
        List<UserResponse> userResponses = new ArrayList<>();

        for (User user : users.values()) {
            userResponses.add(toUserResponse(user));
        }

        return new ListUsersResponse(userResponses);
    }

    public UserResponse getUser(String email) {
        if (getUserLookupError(email) != null) {
            return null;
        }

        User user = users.get(email.toLowerCase().trim());
        return toUserResponse(user);
    }

    public String getUserLookupError(String email) {
        if (email == null || email.isBlank()) {
            return "Email is required";
        }

        String normalizedEmail = email.toLowerCase().trim();

        if (!isValidEmail(normalizedEmail)) {
            return "Email address is invalid";
        }

        if (!users.containsKey(normalizedEmail)) {
            return "User not found";
        }

        return null;
    }

    public DeleteUserResponse deleteUser(String email) {
        if (email == null || email.isBlank()) {
            return new DeleteUserResponse(false, "Email is required");
        }

        String normalizedEmail = email.toLowerCase().trim();

        if (!isValidEmail(normalizedEmail)) {
            return new DeleteUserResponse(false, "Email address is invalid");
        }

        if (!users.containsKey(normalizedEmail)) {
            return new DeleteUserResponse(false, "User not found");
        }

        users.remove(normalizedEmail);
        return new DeleteUserResponse(true, "User deleted");
    }

    public UpdatePasswordResponse updatePassword(String email, UpdatePasswordRequest request) {
        if (email == null || email.isBlank()) {
            return new UpdatePasswordResponse(false, "Email is required");
        }

        String normalizedEmail = email.toLowerCase().trim();

        if (!isValidEmail(normalizedEmail)) {
            return new UpdatePasswordResponse(false, "Email address is invalid");
        }

        if (!users.containsKey(normalizedEmail)) {
            return new UpdatePasswordResponse(false, "User not found");
        }

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            return new UpdatePasswordResponse(false, "Current password is required");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            return new UpdatePasswordResponse(false, "New password must be at least 8 characters");
        }

        User user = users.get(normalizedEmail);

        if (!user.getPassword().equals(request.getCurrentPassword())) {
            return new UpdatePasswordResponse(false, "Current password is incorrect");
        }

        User updatedUser = new User(
            user.getUsername(),
            user.getEmail(),
            request.getNewPassword()
        );
        users.put(normalizedEmail, updatedUser);

        return new UpdatePasswordResponse(true, "Password updated");
    }

    public DashboardSummaryResponse getDashboardSummary(String email) {
        if (email == null || email.isBlank()) {
            return new DashboardSummaryResponse("Email is required");
        }

        String normalizedEmail = email.toLowerCase().trim();

        if (!isValidEmail(normalizedEmail)) {
            return new DashboardSummaryResponse("Email address is invalid");
        }

        User user = users.get(normalizedEmail);

        if (user == null) {
            return new DashboardSummaryResponse("User not found");
        }

        return new DashboardSummaryResponse(
            user.getUsername(),
            user.getEmail(),
            "active",
            users.size(),
            3,
            0,
            3
        );
    }

    private GetProfileResponse toProfileResponse(User user) {
        return new GetProfileResponse(user.getUsername(), user.getEmail());
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getUsername(), user.getEmail());
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
=======
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
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893
    }
}
