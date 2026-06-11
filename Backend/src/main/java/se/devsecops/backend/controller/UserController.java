package se.devsecops.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import se.devsecops.backend.model.ApiResponse;
import se.devsecops.backend.model.ChangePasswordRequest;
import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.ProfileResponse;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.service.UserService;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/users")
    public CreateUserResponse createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/api/users/{email}")
    public ProfileResponse getProfile(@PathVariable String email) {
        return userService.getProfile(email);
    }

    @PatchMapping("/api/users/{email}")
    public ProfileResponse updateProfile(
        @PathVariable String email,
        @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateProfile(email, request);
    }

    @PatchMapping("/api/users/{email}/password")
    public ApiResponse changePassword(
        @PathVariable String email,
        @RequestBody ChangePasswordRequest request
    ) {
        return userService.changePassword(email, request);
    }
}
