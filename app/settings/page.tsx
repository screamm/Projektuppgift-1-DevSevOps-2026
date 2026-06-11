"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";

type ApiResponse = {
  success: boolean;
  message: string;
};

type ProfileResponse = ApiResponse & {
  username: string | null;
  email: string | null;
};

type Task = {
  id: number;
  title: string;
  priority: "High" | "Medium" | "Low";
  completed: boolean;
};

const inputClass =
  "block w-full rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2.5 text-zinc-900 outline-none transition-colors focus:border-zinc-400 focus:bg-white focus:ring-2 focus:ring-zinc-900/10 dark:border-zinc-700 dark:bg-zinc-800/50 dark:text-zinc-50 dark:focus:border-zinc-500 dark:focus:bg-zinc-800";

export default function SettingsPage() {
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [tasks, setTasks] = useState<Task[]>([]);
  const [profileMessage, setProfileMessage] = useState<ApiResponse | null>(null);
  const [passwordMessage, setPasswordMessage] = useState<ApiResponse | null>(null);
  const [taskMessage, setTaskMessage] = useState<ApiResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedEmail = localStorage.getItem("userEmail") ?? "";

    if (!storedEmail) {
      Promise.resolve().then(() => setLoading(false));
      return;
    }

    Promise.all([
      fetch(`/api/users/${encodeURIComponent(storedEmail)}`).then((response) =>
        response.json() as Promise<ProfileResponse>,
      ),
      fetch(`/api/users/${encodeURIComponent(storedEmail)}/tasks`).then(
        (response) => response.json() as Promise<Task[]>,
      ),
    ])
      .then(([profile, loadedTasks]) => {
        setEmail(storedEmail);
        if (profile.success && profile.username) {
          setUsername(profile.username);
        } else {
          setProfileMessage(profile);
        }
        setTasks(loadedTasks);
      })
      .catch(() => {
        setProfileMessage({
          success: false,
          message: "Could not load settings.",
        });
      })
      .finally(() => setLoading(false));
  }, []);

  async function updateProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setProfileMessage(null);

    const response = await fetch(`/api/users/${encodeURIComponent(email)}`, {
      method: "PATCH",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({ username }),
    });
    const data = (await response.json()) as ProfileResponse;
    setProfileMessage(data);
    if (data.success && data.username) {
      setUsername(data.username);
    }
  }

  async function updatePassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const formData = new FormData(form);
    setPasswordMessage(null);

    const response = await fetch(
      `/api/users/${encodeURIComponent(email)}/password`,
      {
        method: "PATCH",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({
          currentPassword: formData.get("currentPassword"),
          newPassword: formData.get("newPassword"),
        }),
      },
    );
    const data = (await response.json()) as ApiResponse;
    setPasswordMessage(data);
    if (data.success) {
      form.reset();
    }
  }

  function updateTaskLocally(taskId: number, changes: Partial<Task>) {
    setTasks((current) =>
      current.map((task) => (task.id === taskId ? { ...task, ...changes } : task)),
    );
  }

  async function saveTask(task: Task) {
    setTaskMessage(null);
    const response = await fetch(
      `/api/users/${encodeURIComponent(email)}/tasks/${task.id}`,
      {
        method: "PUT",
        headers: { "content-type": "application/json" },
        body: JSON.stringify(task),
      },
    );
    const updatedTask = (await response.json()) as Task | null;

    if (!response.ok || !updatedTask) {
      setTaskMessage({ success: false, message: "Could not update task." });
      return;
    }

    updateTaskLocally(task.id, updatedTask);
    setTaskMessage({ success: true, message: "Task updated." });
  }

  if (loading) {
    return <main className="p-10 text-center">Loading settings...</main>;
  }

  if (!email) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-zinc-50 px-6 dark:bg-zinc-950">
        <div className="text-center">
          <h1 className="text-2xl font-semibold">Sign in required</h1>
          <Link className="mt-4 inline-block underline" href="/sign-in">
            Go to sign in
          </Link>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-zinc-50 text-zinc-900 dark:bg-zinc-950 dark:text-zinc-50">
      <header className="border-b border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <div>
            <p className="text-xs font-semibold tracking-widest text-zinc-500 uppercase">
              Todo DevSecOps
            </p>
            <h1 className="mt-1 font-semibold">Settings</h1>
          </div>
          <Link
            href="/dashboard"
            className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
          >
            Back to dashboard
          </Link>
        </div>
      </header>

      <div className="mx-auto grid max-w-5xl gap-6 px-6 py-10">
        <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
          <h2 className="text-lg font-semibold">Profile</h2>
          <p className="mt-1 text-sm text-zinc-500">Signed in as {email}</p>
          <form className="mt-5 max-w-xl space-y-4" onSubmit={updateProfile}>
            <label className="block text-sm font-medium" htmlFor="username">
              Username
            </label>
            <input
              className={inputClass}
              id="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
            <button className="rounded-xl bg-zinc-900 px-4 py-2.5 text-sm font-medium text-white dark:bg-zinc-50 dark:text-zinc-900">
              Save username
            </button>
            <StatusMessage response={profileMessage} />
          </form>
        </section>

        <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
          <h2 className="text-lg font-semibold">Password</h2>
          <form className="mt-5 max-w-xl space-y-4" onSubmit={updatePassword}>
            <input
              className={inputClass}
              name="currentPassword"
              type="password"
              autoComplete="current-password"
              placeholder="Current password"
              required
            />
            <input
              className={inputClass}
              name="newPassword"
              type="password"
              autoComplete="new-password"
              placeholder="New password"
              minLength={8}
              required
            />
            <button className="rounded-xl bg-zinc-900 px-4 py-2.5 text-sm font-medium text-white dark:bg-zinc-50 dark:text-zinc-900">
              Change password
            </button>
            <StatusMessage response={passwordMessage} />
          </form>
        </section>

        <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
          <h2 className="text-lg font-semibold">Tasks</h2>
          <p className="mt-1 text-sm text-zinc-500">
            Edit the title, priority or completion status.
          </p>
          <div className="mt-5 space-y-4">
            {tasks.map((task) => (
              <div
                key={task.id}
                className="grid gap-3 rounded-xl border border-zinc-200 p-4 md:grid-cols-[1fr_10rem_auto_auto] md:items-center dark:border-zinc-700"
              >
                <input
                  className={inputClass}
                  value={task.title}
                  onChange={(event) =>
                    updateTaskLocally(task.id, { title: event.target.value })
                  }
                />
                <select
                  className={inputClass}
                  value={task.priority}
                  onChange={(event) =>
                    updateTaskLocally(task.id, {
                      priority: event.target.value as Task["priority"],
                    })
                  }
                >
                  <option>High</option>
                  <option>Medium</option>
                  <option>Low</option>
                </select>
                <label className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={task.completed}
                    onChange={(event) =>
                      updateTaskLocally(task.id, {
                        completed: event.target.checked,
                      })
                    }
                  />
                  Done
                </label>
                <button
                  type="button"
                  onClick={() => saveTask(task)}
                  className="rounded-xl border border-zinc-300 px-4 py-2.5 text-sm font-medium hover:bg-zinc-100 dark:border-zinc-600 dark:hover:bg-zinc-800"
                >
                  Save
                </button>
              </div>
            ))}
          </div>
          <div className="mt-4">
            <StatusMessage response={taskMessage} />
          </div>
        </section>
      </div>
    </main>
  );
}

function StatusMessage({ response }: { response: ApiResponse | null }) {
  if (!response) {
    return null;
  }

  return (
    <p
      aria-live="polite"
      className={`text-sm ${
        response.success
          ? "text-green-700 dark:text-green-400"
          : "text-red-600 dark:text-red-400"
      }`}
    >
      {response.message}
    </p>
  );
}
