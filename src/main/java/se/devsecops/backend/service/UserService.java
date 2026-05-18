package se.devsecops.backend.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;

@Service
public class UserService {

    private final Set<String> users = new HashSet<>();

    public CreateUserResponse createUser(CreateUserRequest request) {
        String email = request.getEmail().toLowerCase();

        if (users.contains(email)) {
            return new CreateUserResponse(true, "User already exists");
        }

        users.add(email);

        return new CreateUserResponse(false, "User created");
    }
}