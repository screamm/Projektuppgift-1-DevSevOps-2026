"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";

const USER_EMAIL_KEY = "todo-devsecops-user-email";

type HealthResponse = {
  status: string;
};

type DashboardSummaryResponse = {
  username?: string;
  email?: string;
  accountStatus?: string;
  userCount?: number;
  message?: string;
};

type UserResponse = {
  username: string;
  email: string;
};

type ListUsersResponse = {
  users: UserResponse[];
};

type Task = {
  id: number;
  title: string;
  priority: string;
  completed: boolean;
};

type ErrorResponse = {
  message?: string;
};

export default function DashboardPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [healthStatus, setHealthStatus] = useState("checking");
  const [summary, setSummary] = useState<DashboardSummaryResponse | null>(null);
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);

  useEffect(() => {
    const storedEmail = sessionStorage.getItem(USER_EMAIL_KEY);
    if (!storedEmail) {
      router.replace("/sign-in");
      return;
    }

    async function loadDashboard(userEmail: string) {
      try {
        const [healthResponse, summaryResponse, usersResponse, tasksResponse] =
          await Promise.all([
            fetch("/api/health", { cache: "no-store" }),
            fetch(
              `/api/dashboard/summary?email=${encodeURIComponent(userEmail)}`,
              { cache: "no-store" },
            ),
            fetch("/api/users", { cache: "no-store" }),
            fetch(`/api/users/${encodeURIComponent(userEmail)}/tasks`, {
              cache: "no-store",
            }),
          ]);

        const healthData = (await healthResponse.json()) as HealthResponse;
        const summaryData =
          (await summaryResponse.json()) as DashboardSummaryResponse;
        const usersData = usersResponse.ok
          ? ((await usersResponse.json()) as ListUsersResponse)
          : { users: [] };
        const tasksData: unknown = await tasksResponse.json();

        setEmail(userEmail);
        setHealthStatus(healthResponse.ok ? healthData.status : "offline");
        setUsers(usersData.users ?? []);
        if (tasksResponse.ok && isTaskArray(tasksData)) {
          setTasks(tasksData);
        } else {
          const error = tasksData as ErrorResponse;
          setTasks([]);
          setMessage(error.message ?? "Could not load tasks.");
        }

        if (!summaryResponse.ok || !summaryData.username) {
          setMessage(summaryData.message ?? "Could not load dashboard summary.");
          return;
        }
        setSummary(summaryData);
      } catch {
        setMessage("Could not connect to the server. Please try again.");
        setHealthStatus("offline");
      } finally {
        setIsLoading(false);
      }
    }

    void loadDashboard(storedEmail);
  }, [router]);

  const completed = useMemo(
    () => tasks.filter((task) => task.completed).length,
    [tasks],
  );
  const overview = [
    { label: "Tasks today", value: tasks.length },
    { label: "Completed", value: completed },
    { label: "Remaining", value: tasks.length - completed },
  ];

  async function toggleTask(task: Task) {
    const updated = { ...task, completed: !task.completed };
    setTasks((current) =>
      current.map((item) => (item.id === task.id ? updated : item)),
    );

    const response = await fetch(
      `/api/users/${encodeURIComponent(email)}/tasks/${task.id}`,
      {
        method: "PUT",
        headers: { "content-type": "application/json" },
        body: JSON.stringify(updated),
      },
    );

    if (!response.ok) {
      setTasks((current) =>
        current.map((item) => (item.id === task.id ? task : item)),
      );
      setMessage("Could not update task.");
    }
  }

  function signOut() {
    sessionStorage.removeItem(USER_EMAIL_KEY);
  }

  return (
    <main className="min-h-screen bg-zinc-50 font-sans text-zinc-900 dark:bg-zinc-950 dark:text-zinc-50">
      <header className="border-b border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <div>
            <p className="text-xs font-semibold tracking-widest text-zinc-500 uppercase">
              Todo DevSecOps
            </p>
            <p className="mt-1 font-semibold">Dashboard</p>
          </div>
          <nav className="flex items-center gap-2">
            <span
              className={`rounded-full px-3 py-1 text-xs font-medium ${
                healthStatus === "ok"
                  ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                  : "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"
              }`}
            >
              Backend: {healthStatus}
            </span>
            <Link
              href="/settings"
              className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
            >
              Settings
            </Link>
            <Link
              href="/profile"
              className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
            >
              Profile
            </Link>
            <Link
              href="/sign-in"
              onClick={signOut}
              className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
            >
              Sign out
            </Link>
          </nav>
        </div>
      </header>

      <div className="mx-auto max-w-6xl px-6 py-10">
        {isLoading ? (
          <p className="text-sm text-zinc-500">Loading dashboard...</p>
        ) : (
          <>
            <section>
              <p className="text-sm text-zinc-500">
                {new Date().toLocaleDateString("en-US", {
                  weekday: "long",
                  month: "long",
                  day: "numeric",
                })}
              </p>
              <h1 className="mt-2 text-3xl font-semibold tracking-tight">
                Welcome back{summary?.username ? `, ${summary.username}` : ""}
              </h1>
              <p className="mt-2 text-zinc-600 dark:text-zinc-400">
                Account status: {summary?.accountStatus ?? "unknown"} ·{" "}
                {summary?.userCount ?? 0} registered users
              </p>
              {message && (
                <p className="mt-3 text-sm text-red-600" aria-live="polite">
                  {message}
                </p>
              )}
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
                  <p className="text-sm text-zinc-500">{item.label}</p>
                  <p className="mt-2 text-3xl font-semibold">{item.value}</p>
                </article>
              ))}
            </section>

            <section className="mt-8 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
              <h2 className="text-lg font-semibold">Today&apos;s tasks</h2>
              <p className="mt-1 text-sm text-zinc-500">
                Manage task details from Settings.
              </p>
              <ul className="mt-6 divide-y divide-zinc-100 dark:divide-zinc-800">
                {tasks.map((task) => (
                  <li
                    key={task.id}
                    className="flex items-center gap-4 py-4 first:pt-0 last:pb-0"
                  >
                    <input
                      type="checkbox"
                      checked={task.completed}
                      onChange={() => toggleTask(task)}
                      aria-label={`Mark ${task.title} as completed`}
                      className="size-5 accent-zinc-900"
                    />
                    <span
                      className={`min-w-0 flex-1 font-medium ${
                        task.completed ? "text-zinc-400 line-through" : ""
                      }`}
                    >
                      {task.title}
                    </span>
                    <span className="rounded-full bg-zinc-100 px-3 py-1 text-xs font-medium text-zinc-600 dark:bg-zinc-800 dark:text-zinc-300">
                      {task.priority}
                    </span>
                  </li>
                ))}
              </ul>
            </section>

            <section className="mt-8 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
              <h2 className="text-lg font-semibold">Registered users</h2>
              <ul className="mt-4 divide-y divide-zinc-100 dark:divide-zinc-800">
                {users.map((user) => (
                  <li
                    key={user.email}
                    className="flex items-center justify-between py-3 text-sm"
                  >
                    <span className="font-medium">{user.username}</span>
                    <span className="text-zinc-500">{user.email}</span>
                  </li>
                ))}
              </ul>
            </section>
          </>
        )}
      </div>
    </main>
  );
}

function isTask(value: unknown): value is Task {
  if (typeof value !== "object" || value === null) {
    return false;
  }

  const task = value as Record<string, unknown>;
  return (
    typeof task.id === "number" &&
    typeof task.title === "string" &&
    typeof task.priority === "string" &&
    typeof task.completed === "boolean"
  );
}

function isTaskArray(value: unknown): value is Task[] {
  return Array.isArray(value) && value.every(isTask);
}
