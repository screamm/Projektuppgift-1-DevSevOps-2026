import Link from "next/link";

const overview = [
  { label: "Tasks today", value: "3" },
  { label: "Completed", value: "0" },
  { label: "Remaining", value: "3" },
];

const tasks = [
  { title: "Plan the next sprint", priority: "High" },
  { title: "Review security checklist", priority: "Medium" },
  { title: "Update project documentation", priority: "Low" },
];

export default function DashboardPage() {
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

          <Link
            href="/sign-in"
            className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium transition-colors hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
          >
            Sign out
          </Link>
        </div>
      </header>

      <div className="mx-auto max-w-6xl px-6 py-10">
        <section>
          <p className="text-sm text-zinc-500 dark:text-zinc-400">
            Monday, June 8
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
                <span className="min-w-0 flex-1 font-medium">{task.title}</span>
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
