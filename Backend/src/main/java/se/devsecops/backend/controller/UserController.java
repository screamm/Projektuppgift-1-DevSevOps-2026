package se.devsecops.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.devsecops.backend.model.ApiResponse;
import se.devsecops.backend.model.ChangePasswordRequest;
import se.devsecops.backend.model.CreateUserRequest;
import se.devsecops.backend.model.CreateUserResponse;
import se.devsecops.backend.model.DeleteUserResponse;
import se.devsecops.backend.model.ListUsersResponse;
import se.devsecops.backend.model.ProfileResponse;
import se.devsecops.backend.model.UpdatePasswordRequest;
import se.devsecops.backend.model.UpdatePasswordResponse;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.model.UserResponse;
import se.devsecops.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserRequest request) {
        CreateUserResponse response = userService.createUser(request);

        if (response.isExists()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        if ("User created".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping
    public ResponseEntity<ListUsersResponse> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getUser(@PathVariable String email) {
        UserResponse response = userService.getUser(email);
        if (response != null) {
            return ResponseEntity.ok(response);
        }

        String error = userService.getUserLookupError(email);
        if ("User not found".equals(error)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", error));
        }
        return ResponseEntity.badRequest().body(Map.of("message", error));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<DeleteUserResponse> deleteUser(@PathVariable String email) {
        DeleteUserResponse response = userService.deleteUser(email);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        if ("User not found".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/{email}/password")
    public ResponseEntity<UpdatePasswordResponse> updatePassword(
        @PathVariable String email,
        @RequestBody UpdatePasswordRequest request
    ) {
        UpdatePasswordResponse response = userService.updatePassword(email, request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        if ("User not found".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/{email}/settings")
    public ResponseEntity<ProfileResponse> getSettingsProfile(@PathVariable String email) {
        ProfileResponse response = userService.getSettingsProfile(email);
        return ResponseEntity.status(statusFor(response)).body(response);
    }

    @PatchMapping("/{email}")
    public ResponseEntity<ProfileResponse> updateSettingsProfile(
        @PathVariable String email,
        @RequestBody UpdateProfileRequest request
    ) {
        ProfileResponse response = userService.updateProfile(email, request);
        return ResponseEntity.status(statusFor(response)).body(response);
    }

    @PatchMapping("/{email}/password")
    public ResponseEntity<ApiResponse> changePassword(
        @PathVariable String email,
        @RequestBody ChangePasswordRequest request
    ) {
        ApiResponse response = userService.changePassword(email, request);
        return ResponseEntity.status(statusFor(response)).body(response);
    }

    private HttpStatus statusFor(ApiResponse response) {
        if (response.isSuccess()) {
            return HttpStatus.OK;
        }
        if ("User not found".equals(response.getMessage())) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
