package se.devsecops.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.LoginRequest;
import se.devsecops.backend.model.LoginResult;
import se.devsecops.backend.service.UserService;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<CreateUserResponse> register(@RequestBody CreateUserRequest request) {
        CreateUserResponse response = userService.createUser(request);

        if (response.isExists()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        if ("User created".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResult> login(@RequestBody LoginRequest request) {
        LoginResult response = userService.login(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        if ("Email is required".equals(response.getMessage())
            || "Email address is invalid".equals(response.getMessage())) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
