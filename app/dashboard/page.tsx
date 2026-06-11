"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

type Task = {
  id: number;
  title: string;
  priority: string;
  completed: boolean;
};

export default function DashboardPage() {
  const [tasks, setTasks] = useState<Task[]>([]);

  useEffect(() => {
    const storedEmail = localStorage.getItem("userEmail") ?? "";
    if (!storedEmail) {
      return;
    }

    fetch(`/api/users/${encodeURIComponent(storedEmail)}/tasks`)
      .then((response) => response.json() as Promise<Task[]>)
      .then(setTasks)
      .catch(() => setTasks([]));
  }, []);

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
    const email = localStorage.getItem("userEmail") ?? "";
    if (!email) {
      return;
    }

    const updated = { ...task, completed: !task.completed };
    setTasks((current) =>
      current.map((item) => (item.id === task.id ? updated : item)),
    );
    await fetch(
      `/api/users/${encodeURIComponent(email)}/tasks/${task.id}`,
      {
        method: "PUT",
        headers: { "content-type": "application/json" },
        body: JSON.stringify(updated),
      },
    );
  }

  function signOut() {
    localStorage.removeItem("userEmail");
  }

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
              className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
            >
              Settings
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
        <section>
          <p className="text-sm text-zinc-500 dark:text-zinc-400">
            Your task overview
          </p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight">
            Welcome back
          </h1>
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
          <div>
            <h2 className="text-lg font-semibold">Today&apos;s tasks</h2>
            <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              Manage task details from Settings.
            </p>
          </div>

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
                  className="size-5 rounded border-zinc-300 accent-zinc-900 dark:border-zinc-700 dark:accent-zinc-100"
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
      </div>
    </main>
  );
}
