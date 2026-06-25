package se.devsecops.backend.tasks;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.devsecops.backend.exception.TaskNotFoundException;

class TaskServiceTest {

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
    }

    @Test
    void getAllTasksReturnsSeedData() {
        List<Task> tasks = taskService.getAllTasks();

        assertEquals(3, tasks.size());
        assertAll(
            () -> assertEquals("Plan the next sprint", tasks.get(0).getTitle()),
            () -> assertEquals(Priority.HIGH, tasks.get(0).getPriority()),
            () -> assertEquals(LocalDate.of(2026, 6, 15), tasks.get(0).getDueDate()),
            () -> assertFalse(tasks.get(0).isCompleted()),
            () -> assertEquals("Review security checklist", tasks.get(1).getTitle()),
            () -> assertEquals(Priority.MEDIUM, tasks.get(1).getPriority()),
            () -> assertEquals(LocalDate.of(2026, 6, 18), tasks.get(1).getDueDate()),
            () -> assertFalse(tasks.get(1).isCompleted()),
            () -> assertEquals("Update project documentation", tasks.get(2).getTitle()),
            () -> assertEquals(Priority.LOW, tasks.get(2).getPriority()),
            () -> assertNull(tasks.get(2).getDueDate()),
            () -> assertFalse(tasks.get(2).isCompleted())
        );
    }

    @Test
    void createTaskAssignsIdAndStoresTask() {
        Task created = taskService.createTask(
            new TaskRequest("Write report", Priority.HIGH, LocalDate.of(2026, 7, 1), true)
        );

        assertNotNull(created.getId());
        assertAll(
            () -> assertEquals("Write report", created.getTitle()),
            () -> assertEquals(Priority.HIGH, created.getPriority()),
            () -> assertEquals(LocalDate.of(2026, 7, 1), created.getDueDate()),
            () -> assertTrue(created.isCompleted())
        );
        assertEquals(4, taskService.getAllTasks().size());
        assertEquals("Write report", taskService.getTaskById(created.getId()).getTitle());
    }

    @Test
    void createTaskDefaultsCompletedToFalseWhenOmitted() {
        Task created = taskService.createTask(
            new TaskRequest("No completed flag", Priority.LOW, null, null)
        );

        assertFalse(created.isCompleted());
        assertNull(created.getDueDate());
    }

    @Test
    void getTaskByIdReturnsTask() {
        Task task = taskService.getTaskById(1L);

        assertEquals(1L, task.getId());
        assertEquals("Plan the next sprint", task.getTitle());
    }

    @Test
    void getTaskByIdThrowsForUnknownId() {
        TaskNotFoundException exception = assertThrows(
            TaskNotFoundException.class,
            () -> taskService.getTaskById(999L)
        );

        assertEquals("Task with id 999 not found", exception.getMessage());
    }

    @Test
    void updateTaskReplacesFields() {
        Task updated = taskService.updateTask(
            2L,
            new TaskRequest("Review updated checklist", Priority.HIGH, LocalDate.of(2026, 6, 20), true)
        );

        assertAll(
            () -> assertEquals(2L, updated.getId()),
            () -> assertEquals("Review updated checklist", updated.getTitle()),
            () -> assertEquals(Priority.HIGH, updated.getPriority()),
            () -> assertEquals(LocalDate.of(2026, 6, 20), updated.getDueDate()),
            () -> assertTrue(updated.isCompleted())
        );
        assertEquals("Review updated checklist", taskService.getTaskById(2L).getTitle());
        assertEquals(3, taskService.getAllTasks().size());
    }

    @Test
    void updateTaskPreservesCompletedWhenFieldIsOmitted() {
        taskService.updateTask(
            1L,
            new TaskRequest("Plan the next sprint", Priority.HIGH, LocalDate.of(2026, 6, 15), true)
        );

        Task updated = taskService.updateTask(
            1L,
            new TaskRequest("Plan the next sprint (revised)", Priority.HIGH, LocalDate.of(2026, 6, 15), null)
        );

        assertTrue(updated.isCompleted());
        assertEquals("Plan the next sprint (revised)", updated.getTitle());
    }

    @Test
    void updateTaskThrowsForUnknownId() {
        TaskRequest request = new TaskRequest("Does not exist", Priority.LOW, null, false);

        TaskNotFoundException exception = assertThrows(
            TaskNotFoundException.class,
            () -> taskService.updateTask(999L, request)
        );

        assertEquals("Task with id 999 not found", exception.getMessage());
    }

    @Test
    void deleteTaskRemovesTask() {
        taskService.deleteTask(1L);

        assertEquals(2, taskService.getAllTasks().size());
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    void deleteTaskThrowsForUnknownId() {
        TaskNotFoundException exception = assertThrows(
            TaskNotFoundException.class,
            () -> taskService.deleteTask(999L)
        );

        assertEquals("Task with id 999 not found", exception.getMessage());
    }
}
