"use client";

import Link from "next/link";
import {
  FormEvent,
  KeyboardEvent as ReactKeyboardEvent,
  useEffect,
  useState,
  useSyncExternalStore,
} from "react";

type TaskPriority = "LOW" | "MEDIUM" | "HIGH";

type Task = {
  id: number;
  title: string;
  priority: TaskPriority;
  dueDate: string | null;
  completed: boolean;
};

type ApiErrorResponse = {
  message?: string;
  errors?: Record<string, string>;
};

const priorityLabels: Record<TaskPriority, string> = {
  LOW: "Low",
  MEDIUM: "Medium",
  HIGH: "High",
};

const fieldClassName =
  "block w-full rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2.5 text-zinc-900 placeholder:text-zinc-400 transition-colors outline-none focus:border-zinc-400 focus:bg-white focus:ring-2 focus:ring-zinc-900/10 dark:border-zinc-700 dark:bg-zinc-800/50 dark:text-zinc-50 dark:placeholder:text-zinc-500 dark:focus:border-zinc-500 dark:focus:bg-zinc-800 dark:focus:ring-zinc-50/10";

const emptySubscribe = () => () => {};

function getTodayLabel() {
  return new Intl.DateTimeFormat("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  }).format(new Date());
}

function getServerTodayLabel() {
  return "";
}

async function readApiErrorMessage(response: Response, fallback: string) {
  try {
    const data = (await response.json()) as ApiErrorResponse;
    const fieldErrors = data.errors ? Object.values(data.errors).join(" ") : "";
    const message = [data.message, fieldErrors].filter(Boolean).join(" ");

    return message || fallback;
  } catch {
    return fallback;
  }
}

export default function DashboardPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasLoaded, setHasLoaded] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [busyTaskIds, setBusyTaskIds] = useState<Set<number>>(new Set());
  const [taskPendingDelete, setTaskPendingDelete] = useState<Task | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const today = useSyncExternalStore(
    emptySubscribe,
    getTodayLabel,
    getServerTodayLabel,
  );

  useEffect(() => {
    const controller = new AbortController();

    async function loadTasks() {
      try {
        const response = await fetch("/api/tasks", {
          signal: controller.signal,
        });

        if (!response.ok) {
          setErrorMessage(
            await readApiErrorMessage(
              response,
              "Could not load tasks. Please try again.",
            ),
          );
          return;
        }

        setTasks((await response.json()) as Task[]);
        setHasLoaded(true);
      } catch {
        if (!controller.signal.aborted) {
          setErrorMessage("Could not connect to the server. Please try again.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    loadTasks();

    return () => controller.abort();
  }, []);

  useEffect(() => {
    if (!taskPendingDelete) {
      return;
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape" && !isDeleting) {
        setTaskPendingDelete(null);
      }
    }

    window.addEventListener("keydown", handleKeyDown);

    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [taskPendingDelete, isDeleting]);

  async function handleCreateTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const formData = new FormData(form);
    const dueDate = formData.get("dueDate");

    setErrorMessage("");
    setIsSubmitting(true);

    try {
      const response = await fetch("/api/tasks", {
        method: "POST",
        headers: {
          "content-type": "application/json",
        },
        body: JSON.stringify({
          title: formData.get("title"),
          priority: formData.get("priority"),
          ...(dueDate ? { dueDate } : {}),
        }),
      });

      if (!response.ok) {
        setErrorMessage(
          await readApiErrorMessage(
            response,
            "Could not create the task. Please try again.",
          ),
        );
        return;
      }

      const createdTask = (await response.json()) as Task;
      setTasks((current) => [...current, createdTask]);
      form.reset();
      setIsFormOpen(false);
    } catch {
      setErrorMessage("Could not connect to the server. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleToggleCompleted(task: Task) {
    if (busyTaskIds.has(task.id)) {
      return;
    }

    setErrorMessage("");
    setBusyTaskIds((current) => new Set(current).add(task.id));

    try {
      const response = await fetch(`/api/tasks/${task.id}`, {
        method: "PUT",
        headers: {
          "content-type": "application/json",
        },
        body: JSON.stringify({
          title: task.title,
          priority: task.priority,
          dueDate: task.dueDate,
          completed: !task.completed,
        }),
      });

      if (!response.ok) {
        setErrorMessage(
          await readApiErrorMessage(
            response,
            "Could not update the task. Please try again.",
          ),
        );
        return;
      }

      const updatedTask = (await response.json()) as Task;
      setTasks((current) =>
        current.map((item) => (item.id === updatedTask.id ? updatedTask : item)),
      );
    } catch {
      setErrorMessage("Could not connect to the server. Please try again.");
    } finally {
      setBusyTaskIds((current) => {
        const next = new Set(current);
        next.delete(task.id);
        return next;
      });
    }
  }

  async function handleConfirmDelete() {
    if (!taskPendingDelete) {
      return;
    }

    const taskId = taskPendingDelete.id;
    setErrorMessage("");
    setIsDeleting(true);

    try {
      const response = await fetch(`/api/tasks/${taskId}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        setErrorMessage(
          await readApiErrorMessage(
            response,
            "Could not delete the task. Please try again.",
          ),
        );
        return;
      }

      setTasks((current) => current.filter((item) => item.id !== taskId));
    } catch {
      setErrorMessage("Could not connect to the server. Please try again.");
    } finally {
      setIsDeleting(false);
      setTaskPendingDelete(null);
    }
  }

  function handleDialogKeyDown(event: ReactKeyboardEvent<HTMLDivElement>) {
    if (event.key !== "Tab") {
      return;
    }

    const focusable = event.currentTarget.querySelectorAll<HTMLElement>(
      "button:not([disabled])",
    );

    if (focusable.length === 0) {
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  }

  const completedCount = tasks.filter((task) => task.completed).length;
  const overview = [
    { label: "Tasks today", value: tasks.length },
    { label: "Completed", value: completedCount },
    { label: "Remaining", value: tasks.length - completedCount },
  ];

  return (
    <main className="min-h-screen bg-zinc-50 font-sans text-zinc-900 dark:bg-zinc-950 dark:text-zinc-50">
      <header className="border-b border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <div>
            <p className="text-xs font-semibold tracking-widest text-zinc-500 uppercase dark:text-zinc-400">
              Todo DevSecOps
            </p>
            <p className="mt-1 font-semibold">Dashboard</p>
          </div>

          <nav className="flex items-center gap-2">
            <Link
              href="/settings"
              className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium transition-colors hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
            >
              Settings
            </Link>
            <Link
              href="/sign-in"
              onClick={() => localStorage.removeItem("userEmail")}
              className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium transition-colors hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
            >
              Sign out
            </Link>
          </nav>
        </div>
      </header>

      <div className="mx-auto max-w-6xl px-6 py-10">
        <section>
          <p className="text-sm text-zinc-500 dark:text-zinc-400">
            {today || " "}
          </p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight">
            Welcome back
          </h1>
          <p className="mt-2 text-zinc-600 dark:text-zinc-400">
            Here is an overview of your tasks for today.
          </p>
        </section>

        <section
          aria-label="Task overview"
          className="mt-8 grid gap-4 sm:grid-cols-3"
        >
          {overview.map((item) => (
            <article
              key={item.label}
              className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900"
            >
              <p className="text-sm text-zinc-500 dark:text-zinc-400">
                {item.label}
              </p>
              <p className="mt-2 text-3xl font-semibold">{item.value}</p>
            </article>
          ))}
        </section>

        <section className="mt-8 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <h2 className="text-lg font-semibold">Today&apos;s tasks</h2>
              <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
                Focus on what matters most.
              </p>
            </div>

            <button
              type="button"
              data-testid="add-task-button"
              aria-expanded={isFormOpen}
              aria-controls="task-form"
              onClick={() => setIsFormOpen((open) => !open)}
              className="rounded-xl bg-zinc-900 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-zinc-800 dark:bg-zinc-50 dark:text-zinc-900 dark:hover:bg-zinc-200"
            >
              Add task
            </button>
          </div>

          <div aria-live="polite">
            {errorMessage && (
              <p
                data-testid="error-banner"
                className="mt-4 text-sm text-red-600 dark:text-red-400"
              >
                {errorMessage}
              </p>
            )}
          </div>

          {isFormOpen && (
            <form
              id="task-form"
              data-testid="task-form"
              onSubmit={handleCreateTask}
              className="mt-6 rounded-xl border border-zinc-200 p-4 dark:border-zinc-700"
            >
              <div className="flex flex-col gap-4 sm:flex-row">
                <div className="space-y-2 sm:flex-1">
                  <label
                    htmlFor="task-title"
                    className="block text-sm font-medium text-zinc-700 dark:text-zinc-300"
                  >
                    Title
                  </label>
                  <input
                    id="task-title"
                    name="title"
                    type="text"
                    data-testid="task-title-input"
                    placeholder="Plan the next sprint"
                    required
                    className={fieldClassName}
                  />
                </div>

                <div className="space-y-2 sm:w-40">
                  <label
                    htmlFor="task-priority"
                    className="block text-sm font-medium text-zinc-700 dark:text-zinc-300"
                  >
                    Priority
                  </label>
                  <select
                    id="task-priority"
                    name="priority"
                    data-testid="task-priority-select"
                    defaultValue="MEDIUM"
                    className={fieldClassName}
                  >
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                  </select>
                </div>

                <div className="space-y-2 sm:w-44">
                  <label
                    htmlFor="task-due-date"
                    className="block text-sm font-medium text-zinc-700 dark:text-zinc-300"
                  >
                    Due date (optional)
                  </label>
                  <input
                    id="task-due-date"
                    name="dueDate"
                    type="date"
                    data-testid="task-due-date-input"
                    className={fieldClassName}
                  />
                </div>
              </div>

              <div className="mt-4 flex flex-col gap-2 sm:flex-row">
                <button
                  type="submit"
                  data-testid="task-submit"
                  disabled={isSubmitting}
                  className="rounded-xl bg-zinc-900 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-zinc-800 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-zinc-50 dark:text-zinc-900 dark:hover:bg-zinc-200"
                >
                  {isSubmitting ? "Adding task..." : "Add task"}
                </button>
                <button
                  type="button"
                  onClick={() => setIsFormOpen(false)}
                  className="rounded-xl border border-zinc-200 px-4 py-2.5 text-sm font-medium transition-colors hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
                >
                  Cancel
                </button>
              </div>
            </form>
          )}

          {isLoading && (
            <p className="mt-6 text-sm text-zinc-500 dark:text-zinc-400">
              Loading tasks...
            </p>
          )}

          {!isLoading && hasLoaded && tasks.length === 0 && (
            <p className="mt-6 text-sm text-zinc-500 dark:text-zinc-400">
              No tasks yet
            </p>
          )}

          {tasks.length > 0 && (
            <ul
              data-testid="task-list"
              className="mt-6 divide-y divide-zinc-100 dark:divide-zinc-800"
            >
              {tasks.map((task) => (
                <li
                  key={task.id}
                  data-testid="task-item"
                  className="flex items-center gap-3 py-4 first:pt-0 last:pb-0 sm:gap-4"
                >
                  <input
                    type="checkbox"
                    data-testid="task-checkbox"
                    checked={task.completed}
                    disabled={busyTaskIds.has(task.id)}
                    onChange={() => handleToggleCompleted(task)}
                    aria-label={
                      task.completed
                        ? `Mark ${task.title} as not completed`
                        : `Mark ${task.title} as completed`
                    }
                    className="size-5 rounded border-zinc-300 accent-zinc-900 dark:border-zinc-700 dark:accent-zinc-100"
                  />
                  <div className="min-w-0 flex-1">
                    <p
                      className={`font-medium ${
                        task.completed
                          ? "text-zinc-400 line-through dark:text-zinc-500"
                          : ""
                      }`}
                    >
                      {task.title}
                    </p>
                    {task.dueDate && (
                      <p className="mt-0.5 text-xs text-zinc-500 dark:text-zinc-400">
                        Due {task.dueDate}
                      </p>
                    )}
                  </div>
                  <span className="rounded-full bg-zinc-100 px-3 py-1 text-xs font-medium text-zinc-600 dark:bg-zinc-800 dark:text-zinc-300">
                    {priorityLabels[task.priority]}
                  </span>
                  <button
                    type="button"
                    data-testid="task-delete-button"
                    disabled={busyTaskIds.has(task.id)}
                    onClick={() => setTaskPendingDelete(task)}
                    aria-label={`Delete ${task.title}`}
                    className="rounded-lg border border-zinc-200 px-3 py-1.5 text-xs font-medium text-zinc-600 transition-colors hover:bg-zinc-100 hover:text-red-600 disabled:cursor-not-allowed disabled:opacity-60 dark:border-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-800 dark:hover:text-red-400"
                  >
                    Delete
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>

      {taskPendingDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-zinc-950/50 px-4">
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-dialog-title"
            aria-describedby="delete-dialog-description"
            onKeyDown={handleDialogKeyDown}
            className="w-full max-w-sm rounded-2xl border border-zinc-200 bg-white p-6 shadow-lg dark:border-zinc-800 dark:bg-zinc-900"
          >
            <h2 id="delete-dialog-title" className="text-lg font-semibold">
              Delete task
            </h2>
            <p
              id="delete-dialog-description"
              className="mt-2 text-sm text-zinc-600 dark:text-zinc-400"
            >
              Are you sure you want to delete &quot;{taskPendingDelete.title}
              &quot;? This action cannot be undone.
            </p>
            <div className="mt-6 flex flex-col gap-2 sm:flex-row sm:justify-end">
              <button
                type="button"
                data-testid="cancel-delete"
                autoFocus
                disabled={isDeleting}
                onClick={() => setTaskPendingDelete(null)}
                className="rounded-xl border border-zinc-200 px-4 py-2.5 text-sm font-medium transition-colors hover:bg-zinc-100 disabled:cursor-not-allowed disabled:opacity-60 dark:border-zinc-700 dark:hover:bg-zinc-800"
              >
                Cancel
              </button>
              <button
                type="button"
                data-testid="confirm-delete"
                disabled={isDeleting}
                onClick={handleConfirmDelete}
                className="rounded-xl bg-red-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isDeleting ? "Deleting..." : "Delete"}
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
