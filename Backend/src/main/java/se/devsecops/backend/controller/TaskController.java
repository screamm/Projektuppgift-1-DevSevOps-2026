package se.devsecops.backend.controller;

import java.util.List;

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
    public List<Task> getTasks(@PathVariable String email) {
        return userService.getTasks(email);
    }

    @PutMapping("/api/users/{email}/tasks/{taskId}")
    public Task updateTask(
        @PathVariable String email,
        @PathVariable long taskId,
        @RequestBody UpdateTaskRequest request
    ) {
        return userService.updateTask(email, taskId, request);
    }
}
