package se.devsecops.backend.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import se.devsecops.backend.exception.TaskNotFoundException;

@WebMvcTest(TasksController.class)
class TasksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void getAllTasksReturnsOkWithJsonList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of(
            new Task(1L, "Plan the next sprint", Priority.HIGH, LocalDate.of(2026, 6, 15), false),
            new Task(3L, "Update project documentation", Priority.LOW, null, false)
        ));

        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("Plan the next sprint"))
            .andExpect(jsonPath("$[0].priority").value("HIGH"))
            .andExpect(jsonPath("$[0].dueDate").value("2026-06-15"))
            .andExpect(jsonPath("$[0].completed").value(false))
            .andExpect(jsonPath("$[1].dueDate").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void getTaskByIdReturnsNotFoundForUnknownId() throws Exception {
        when(taskService.getTaskById(999L)).thenThrow(new TaskNotFoundException(999L));

        mockMvc.perform(get("/api/tasks/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Task with id 999 not found"));
    }

    @Test
    void postValidTaskReturnsCreatedWithTask() throws Exception {
        when(taskService.createTask(any())).thenReturn(
            new Task(4L, "Write report", Priority.MEDIUM, LocalDate.of(2026, 7, 1), false)
        );

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "Write report", "priority": "MEDIUM", "dueDate": "2026-07-01"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.title").value("Write report"))
            .andExpect(jsonPath("$.priority").value("MEDIUM"))
            .andExpect(jsonPath("$.dueDate").value("2026-07-01"))
            .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void postBlankTitleReturnsBadRequestWithErrors() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "", "priority": "LOW"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.title").value("Title must not be empty"));
    }

    @Test
    void postTitleAtExactly100CharactersReturnsCreated() throws Exception {
        String title = "x".repeat(100);
        when(taskService.createTask(any())).thenReturn(
            new Task(5L, title, Priority.LOW, null, false)
        );

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "%s", "priority": "LOW"}
                    """.formatted(title)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value(title));
    }

    @Test
    void postTitleOver100CharactersReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "%s", "priority": "LOW"}
                    """.formatted("x".repeat(101))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void postInvalidDueDateFormatReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "Some task", "priority": "LOW", "dueDate": "tomorrow"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    void postInvalidPriorityReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "Some task", "priority": "URGENT"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    void putUnknownIdReturnsNotFound() throws Exception {
        when(taskService.updateTask(eq(999L), any())).thenThrow(new TaskNotFoundException(999L));

        mockMvc.perform(put("/api/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "Updated", "priority": "LOW", "completed": true}
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Task with id 999 not found"));
    }

    @Test
    void putValidTaskReturnsOkWithUpdatedTask() throws Exception {
        when(taskService.updateTask(eq(2L), any())).thenReturn(
            new Task(2L, "Updated checklist", Priority.HIGH, null, true)
        );

        mockMvc.perform(put("/api/tasks/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "Updated checklist", "priority": "HIGH", "dueDate": null, "completed": true}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.title").value("Updated checklist"))
            .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void deleteTaskReturnsNoContentWithEmptyBody() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        verify(taskService).deleteTask(1L);
    }

    @Test
    void deleteUnknownIdReturnsNotFound() throws Exception {
        doThrow(new TaskNotFoundException(999L)).when(taskService).deleteTask(999L);

        mockMvc.perform(delete("/api/tasks/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Task with id 999 not found"));
    }
}
