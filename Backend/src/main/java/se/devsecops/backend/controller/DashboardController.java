package se.devsecops.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.devsecops.backend.model.DashboardSummaryResponse;
import se.devsecops.backend.service.UserService;

@RestController
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@RequestParam String email) {
        DashboardSummaryResponse response = userService.getDashboardSummary(email);

        if (response.getUsername() != null) {
            return ResponseEntity.ok(response);
        }

        if ("User not found".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.badRequest().body(response);
    }
}
