package se.devsecops.backend.tasks;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import se.devsecops.backend.exception.TaskNotFoundException;

@Service
public class TaskService {

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    public TaskService() {
        seedTask("Plan the next sprint", Priority.HIGH, LocalDate.of(2026, 6, 15), false);
        seedTask("Review security checklist", Priority.MEDIUM, LocalDate.of(2026, 6, 18), false);
        seedTask("Update project documentation", Priority.LOW, null, false);
    }

    private void seedTask(String title, Priority priority, LocalDate dueDate, boolean completed) {
        long id = idSequence.incrementAndGet();
        tasks.put(id, new Task(id, title, priority, dueDate, completed));
    }

    public List<Task> getAllTasks() {
        return tasks.values().stream()
            .sorted(Comparator.comparing(Task::getId))
            .toList();
    }

    public Task getTaskById(Long id) {
        Task task = tasks.get(id);

        if (task == null) {
            throw new TaskNotFoundException(id);
        }

        return task;
    }

    public Task createTask(TaskRequest request) {
        long id = idSequence.incrementAndGet();
        Task task = new Task(
            id,
            request.getTitle(),
            request.getPriority(),
            request.getDueDate(),
            Boolean.TRUE.equals(request.getCompleted())
        );

        tasks.put(id, task);

        return task;
    }

    public Task updateTask(Long id, TaskRequest request) {
        // Utelämnat completed-fält behåller taskens nuvarande värde i stället
        // för att tyst nollställa en avbockad task.
        Task updated = tasks.computeIfPresent(id, (key, existing) -> new Task(
            key,
            request.getTitle(),
            request.getPriority(),
            request.getDueDate(),
            request.getCompleted() != null ? request.getCompleted() : existing.isCompleted()
        ));

        if (updated == null) {
            throw new TaskNotFoundException(id);
        }

        return updated;
    }

    public void deleteTask(Long id) {
        if (tasks.remove(id) == null) {
            throw new TaskNotFoundException(id);
        }
    }
}
