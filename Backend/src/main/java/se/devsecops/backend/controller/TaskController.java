package se.devsecops.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import se.devsecops.backend.model.Task;
import se.devsecops.backend.model.UpdateTaskRequest;
import se.devsecops.backend.service.UserService;

@RestController
public class TaskController {

    private final UserService userService;

    public TaskController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users/{email}/tasks")
    public ResponseEntity<?> getTasks(@PathVariable String email) {
        ResponseEntity<?> lookupError = userLookupError(email);
        if (lookupError != null) {
            return lookupError;
        }
        return ResponseEntity.ok(userService.getTasks(email));
    }

    @PutMapping("/api/users/{email}/tasks/{taskId}")
    public ResponseEntity<?> updateTask(
        @PathVariable String email,
        @PathVariable long taskId,
        @RequestBody UpdateTaskRequest request
    ) {
        ResponseEntity<?> lookupError = userLookupError(email);
        if (lookupError != null) {
            return lookupError;
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Title must not be empty"));
        }

        Task task = userService.updateTask(email, taskId, request);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Task not found"));
        }
        return ResponseEntity.ok(task);
    }

    private ResponseEntity<?> userLookupError(String email) {
        String error = userService.getUserLookupError(email);
        if (error == null) {
            return null;
        }
        HttpStatus status = "User not found".equals(error)
            ? HttpStatus.NOT_FOUND
            : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("message", error));
    }
}
