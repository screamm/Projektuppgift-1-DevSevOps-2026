"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

const USER_EMAIL_KEY = "todo-devsecops-user-email";

type HealthResponse = {
  status: string;
  message: string;
};

type DashboardSummaryResponse = {
  username?: string;
  email?: string;
  accountStatus?: string;
  userCount?: number;
  tasksToday?: number;
  completed?: number;
  remaining?: number;
  message?: string;
};

type UserResponse = {
  username: string;
  email: string;
};

type ListUsersResponse = {
  users: UserResponse[];
};

const tasks = [
  { title: "Plan the next sprint", priority: "High" },
  { title: "Review security checklist", priority: "Medium" },
  { title: "Update project documentation", priority: "Low" },
];

export default function DashboardPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);
  const [healthStatus, setHealthStatus] = useState("checking");
  const [summary, setSummary] = useState<DashboardSummaryResponse | null>(null);
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null);
  const [users, setUsers] = useState<UserResponse[]>([]);

  useEffect(() => {
    const storedEmail = sessionStorage.getItem(USER_EMAIL_KEY);

    if (!storedEmail) {
      router.replace("/sign-in");
      return;
    }

    async function loadDashboard(userEmail: string) {
      setIsLoading(true);
      setMessage("");

      try {
        const [healthResponse, summaryResponse, userResponse, usersResponse] =
          await Promise.all([
            fetch("/api/health", { cache: "no-store" }),
            fetch(
              `/api/dashboard/summary?email=${encodeURIComponent(userEmail)}`,
              { cache: "no-store" },
            ),
            fetch(`/api/users/${encodeURIComponent(userEmail)}`, {
              cache: "no-store",
            }),
            fetch("/api/users", { cache: "no-store" }),
          ]);

        const healthData = (await healthResponse.json()) as HealthResponse;
        const summaryData =
          (await summaryResponse.json()) as DashboardSummaryResponse;
        const userData = userResponse.ok
          ? ((await userResponse.json()) as UserResponse)
          : null;
        const usersData = usersResponse.ok
          ? ((await usersResponse.json()) as ListUsersResponse)
          : { users: [] };

        if (!healthResponse.ok) {
          setHealthStatus("offline");
        } else {
          setHealthStatus(healthData.status);
        }

        if (!summaryResponse.ok || !summaryData.username) {
          setIsError(true);
          setMessage(summaryData.message ?? "Could not load dashboard summary.");
          return;
        }

        setSummary(summaryData);
        setCurrentUser(userData);
        setUsers(usersData.users ?? []);
      } catch {
        setIsError(true);
        setMessage("Could not connect to the server. Please try again.");
        setHealthStatus("offline");
      } finally {
        setIsLoading(false);
      }
    }

    void loadDashboard(storedEmail);
  }, [router]);

  const overview = [
    { label: "Tasks today", value: String(summary?.tasksToday ?? "–") },
    { label: "Completed", value: String(summary?.completed ?? "–") },
    { label: "Remaining", value: String(summary?.remaining ?? "–") },
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

          <div className="flex items-center gap-3">
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
              href="/profile"
              className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium transition-colors hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
            >
              Profile
            </Link>
            <Link
              href="/sign-in"
              className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium transition-colors hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
              onClick={() => sessionStorage.removeItem(USER_EMAIL_KEY)}
            >
              Sign out
            </Link>
          </div>
        </div>
      </header>

      <div className="mx-auto max-w-6xl px-6 py-10">
        {isLoading ? (
          <p className="text-sm text-zinc-500 dark:text-zinc-400">
            Loading dashboard...
          </p>
        ) : (
          <>
            <section>
              <p className="text-sm text-zinc-500 dark:text-zinc-400">
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
              {currentUser && (
                <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
                  Signed in as {currentUser.email}
                </p>
              )}
              {message && (
                <p
                  aria-live="polite"
                  className={`mt-3 text-sm ${
                    isError
                      ? "text-red-600 dark:text-red-400"
                      : "text-green-700 dark:text-green-400"
                  }`}
                >
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
                  className="rounded-xl bg-zinc-900 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-zinc-800 dark:bg-zinc-50 dark:text-zinc-900 dark:hover:bg-zinc-200"
                >
                  Add task
                </button>
              </div>

              <ul className="mt-6 divide-y divide-zinc-100 dark:divide-zinc-800">
                {tasks.map((task) => (
                  <li
                    key={task.title}
                    className="flex items-center gap-4 py-4 first:pt-0 last:pb-0"
                  >
                    <input
                      type="checkbox"
                      aria-label={`Mark ${task.title} as completed`}
                      className="size-5 rounded border-zinc-300 accent-zinc-900 dark:border-zinc-700 dark:accent-zinc-100"
                    />
                    <span className="min-w-0 flex-1 font-medium">
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
              <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
                Loaded from GET /api/users.
              </p>
              <ul className="mt-4 divide-y divide-zinc-100 dark:divide-zinc-800">
                {users.length === 0 ? (
                  <li className="py-3 text-sm text-zinc-500 dark:text-zinc-400">
                    No users found.
                  </li>
                ) : (
                  users.map((user) => (
                    <li
                      key={user.email}
                      className="flex items-center justify-between py-3 text-sm"
                    >
                      <span className="font-medium">{user.username}</span>
                      <span className="text-zinc-500 dark:text-zinc-400">
                        {user.email}
                      </span>
                    </li>
                  ))
                )}
              </ul>
            </section>
          </>
        )}
      </div>
    </main>
  );
}
