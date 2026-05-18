package se.devsecops.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import se.devsecops.backend.model.LoginRequest;
import se.devsecops.backend.model.LoginResponse;
import se.devsecops.backend.service.UserService;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/auth/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}