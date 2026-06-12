package se.devsecops.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import se.devsecops.backend.model.Task;
import se.devsecops.backend.model.UpdateTaskRequest;
import se.devsecops.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskController taskController;

    @Test
    void getTasksReturnsOkWithTasksForExistingUser() {
        List<Task> expectedTasks = List.of(new Task(1, "Plan the next sprint", "High", false));
        when(userService.getUserLookupError("test@example.com")).thenReturn(null);
        when(userService.getTasks("test@example.com")).thenReturn(expectedTasks);

        ResponseEntity<?> response = taskController.getTasks("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedTasks, response.getBody());
    }

    @Test
    void getTasksReturnsNotFoundWhenUserIsMissing() {
        when(userService.getUserLookupError("missing@example.com"))
            .thenReturn("User not found");

        ResponseEntity<?> response = taskController.getTasks("missing@example.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Map.of("message", "User not found"), response.getBody());
    }

    @Test
    void getTasksReturnsBadRequestWhenEmailIsInvalid() {
        when(userService.getUserLookupError("not-an-email"))
            .thenReturn("Email address is invalid");

        ResponseEntity<?> response = taskController.getTasks("not-an-email");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("message", "Email address is invalid"), response.getBody());
    }

    @Test
    void updateTaskReturnsOkWithUpdatedTask() {
        UpdateTaskRequest request = new UpdateTaskRequest("New title", "Low", true);
        Task updatedTask = new Task(1, "New title", "Low", true);
        when(userService.getUserLookupError("test@example.com")).thenReturn(null);
        when(userService.updateTask("test@example.com", 1, request)).thenReturn(updatedTask);

        ResponseEntity<?> response =
            taskController.updateTask("test@example.com", 1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updatedTask, response.getBody());
    }

    @Test
    void updateTaskReturnsBadRequestWhenTitleIsBlank() {
        UpdateTaskRequest request = new UpdateTaskRequest("   ", "High", false);
        when(userService.getUserLookupError("test@example.com")).thenReturn(null);

        ResponseEntity<?> response =
            taskController.updateTask("test@example.com", 1, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("message", "Title must not be empty"), response.getBody());
        verify(userService, never()).updateTask(anyString(), anyLong(), any());
    }

    @Test
    void updateTaskReturnsNotFoundWhenTaskIsMissing() {
        UpdateTaskRequest request = new UpdateTaskRequest("New title", "Medium", false);
        when(userService.getUserLookupError("test@example.com")).thenReturn(null);
        when(userService.updateTask("test@example.com", 99, request)).thenReturn(null);

        ResponseEntity<?> response =
            taskController.updateTask("test@example.com", 99, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Map.of("message", "Task not found"), response.getBody());
    }

    @Test
    void updateTaskReturnsNotFoundWhenUserIsMissing() {
        UpdateTaskRequest request = new UpdateTaskRequest("New title", "Medium", false);
        when(userService.getUserLookupError("missing@example.com"))
            .thenReturn("User not found");

        ResponseEntity<?> response =
            taskController.updateTask("missing@example.com", 1, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Map.of("message", "User not found"), response.getBody());
        verify(userService, never()).updateTask(anyString(), anyLong(), any());
    }
}
