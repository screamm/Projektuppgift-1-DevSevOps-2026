package se.devsecops.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.LoginRequest;
import se.devsecops.backend.model.LoginResponse;
import se.devsecops.backend.model.User;

@Service
public class UserService {

    private final Map<String, User> users = new HashMap<>();

    public CreateUserResponse createUser(CreateUserRequest request) {
        String email = request.getEmail().toLowerCase();

        if (users.containsKey(email)) {
            return new CreateUserResponse(true, "User already exists");
        }

        User user = new User(
            request.getUsername(),
            email,
            request.getPassword()
        );

        users.put(email, user);

        return new CreateUserResponse(false, "User created");
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();

        if (!users.containsKey(email)) {
            return new LoginResponse(false, "Invalid email or password");
        }

        User user = users.get(email);

        if (!user.getPassword().equals(request.getPassword())) {
            return new LoginResponse(false, "Invalid email or password");
        }

        return new LoginResponse(true, "Login successful");
    }
}