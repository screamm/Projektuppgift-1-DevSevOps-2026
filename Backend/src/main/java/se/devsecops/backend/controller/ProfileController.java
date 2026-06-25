package se.devsecops.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.devsecops.backend.model.GetProfileResponse;
import se.devsecops.backend.model.UpdateProfileRequest;
import se.devsecops.backend.model.UpdateProfileResponse;
import se.devsecops.backend.service.UserService;

@RestController
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/profile")
    public ResponseEntity<GetProfileResponse> getProfile(@RequestParam String email) {
        GetProfileResponse response = userService.getProfile(email);

        if (response.getUsername() != null) {
            return ResponseEntity.ok(response);
        }

        if ("User not found".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/api/profile")
    public ResponseEntity<UpdateProfileResponse> updateProfile(
        @RequestBody UpdateProfileRequest request
    ) {
        UpdateProfileResponse response = userService.updateProfile(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        if ("User not found".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if ("Email is already in use".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        return ResponseEntity.badRequest().body(response);
    }
}
