"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

const USER_EMAIL_KEY = "todo-devsecops-user-email";

type GetProfileResponse = {
  username?: string;
  email?: string;
  message?: string;
};

type UpdateProfileResponse = {
  success: boolean;
  message: string;
  email?: string;
};

type UpdatePasswordResponse = {
  success: boolean;
  message: string;
};

type DeleteUserResponse = {
  success: boolean;
  message: string;
};

export default function ProfilePage() {
  const router = useRouter();
  const [currentEmail, setCurrentEmail] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isUpdatingPassword, setIsUpdatingPassword] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    const storedEmail = sessionStorage.getItem(USER_EMAIL_KEY);

    if (!storedEmail) {
      router.replace("/sign-in");
      return;
    }

    async function loadProfile(userEmail: string) {
      setIsLoading(true);
      setMessage("");

      try {
        const response = await fetch(
          `/api/profile?email=${encodeURIComponent(userEmail)}`,
          { cache: "no-store" },
        );
        const data = (await response.json()) as GetProfileResponse;

        if (!response.ok || !data.username || !data.email) {
          setIsError(true);
          setMessage(data.message ?? "Could not load profile.");
          return;
        }

        setCurrentEmail(userEmail);
        setUsername(data.username);
        setEmail(data.email);
      } catch {
        setIsError(true);
        setMessage("Could not connect to the server. Please try again.");
      } finally {
        setIsLoading(false);
      }
    }

    void loadProfile(storedEmail);
  }, [router]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    setIsSubmitting(true);

    try {
      const response = await fetch("/api/profile", {
        method: "PUT",
        headers: {
          "content-type": "application/json",
        },
        body: JSON.stringify({
          currentEmail,
          username,
          email,
        }),
      });
      const data = (await response.json()) as UpdateProfileResponse;
      const failed = !response.ok || !data.success;

      setIsError(failed);
      setMessage(data.message);

      if (!failed) {
        const updatedEmail = data.email ?? email;
        sessionStorage.setItem(USER_EMAIL_KEY, updatedEmail);
        setCurrentEmail(updatedEmail);
        setEmail(updatedEmail);
      }
    } catch {
      setIsError(true);
      setMessage("Could not connect to the server. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handlePasswordSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    setIsUpdatingPassword(true);

    try {
      const response = await fetch(
        `/api/users/${encodeURIComponent(currentEmail)}/password`,
        {
          method: "PUT",
          headers: {
            "content-type": "application/json",
          },
          body: JSON.stringify({
            currentPassword,
            newPassword,
          }),
        },
      );
      const data = (await response.json()) as UpdatePasswordResponse;
      const failed = !response.ok || !data.success;

      setIsError(failed);
      setMessage(data.message);

      if (!failed) {
        setCurrentPassword("");
        setNewPassword("");
      }
    } catch {
      setIsError(true);
      setMessage("Could not connect to the server. Please try again.");
    } finally {
      setIsUpdatingPassword(false);
    }
  }

  async function handleDeleteAccount() {
    const confirmed = window.confirm(
      "Are you sure you want to delete your account? This cannot be undone.",
    );

    if (!confirmed) {
      return;
    }

    setMessage("");
    setIsDeleting(true);

    try {
      const response = await fetch(
        `/api/users/${encodeURIComponent(currentEmail)}`,
        { method: "DELETE" },
      );
      const data = (await response.json()) as DeleteUserResponse;
      const failed = !response.ok || !data.success;

      setIsError(failed);
      setMessage(data.message);

      if (!failed) {
        sessionStorage.removeItem(USER_EMAIL_KEY);
        router.replace("/sign-in");
      }
    } catch {
      setIsError(true);
      setMessage("Could not connect to the server. Please try again.");
    } finally {
      setIsDeleting(false);
    }
  }

  return (
    <main className="min-h-screen bg-zinc-50 font-sans text-zinc-900 dark:bg-zinc-950 dark:text-zinc-50">
      <header className="border-b border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <div>
            <p className="text-xs font-semibold tracking-widest text-zinc-500 uppercase dark:text-zinc-400">
              Todo DevSecOps
            </p>
            <p className="mt-1 font-semibold">Profile</p>
          </div>

          <Link
            href="/dashboard"
            className="rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium transition-colors hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
          >
            Back to dashboard
          </Link>
        </div>
      </header>

      <div className="mx-auto flex max-w-2xl flex-col gap-8 px-6 py-10">
        {isLoading ? (
          <div
            aria-live="polite"
            className="w-full rounded-2xl border border-zinc-200 bg-white p-8 text-center shadow-sm dark:border-zinc-800 dark:bg-zinc-900"
          >
            <p className="text-sm text-zinc-500 dark:text-zinc-400">
              Loading profile...
            </p>
          </div>
        ) : (
          <>
            <div className="w-full rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
              <div className="mb-6">
                <h1 className="text-2xl font-semibold tracking-tight">
                  Manage your profile
                </h1>
                <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
                  Update your name and email address.
                </p>
              </div>

              <form className="space-y-5" onSubmit={handleSubmit}>
                <div className="space-y-2">
                  <label
                    htmlFor="username"
                    className="block text-sm font-medium text-zinc-700 dark:text-zinc-300"
                  >
                    Name
                  </label>
                  <input
                    id="username"
                    name="username"
                    type="text"
                    autoComplete="name"
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    placeholder="Your name"
                    required
                    className="block w-full rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2.5 text-zinc-900 placeholder:text-zinc-400 transition-colors outline-none focus:border-zinc-400 focus:bg-white focus:ring-2 focus:ring-zinc-900/10 dark:border-zinc-700 dark:bg-zinc-800/50 dark:text-zinc-50 dark:placeholder:text-zinc-500 dark:focus:border-zinc-500 dark:focus:bg-zinc-800 dark:focus:ring-zinc-50/10"
                  />
                </div>

                <div className="space-y-2">
                  <label
                    htmlFor="email"
                    className="block text-sm font-medium text-zinc-700 dark:text-zinc-300"
                  >
                    Email
                  </label>
                  <input
                    id="email"
                    name="email"
                    type="email"
                    autoComplete="email"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    placeholder="you@example.com"
                    required
                    className="block w-full rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2.5 text-zinc-900 placeholder:text-zinc-400 transition-colors outline-none focus:border-zinc-400 focus:bg-white focus:ring-2 focus:ring-zinc-900/10 dark:border-zinc-700 dark:bg-zinc-800/50 dark:text-zinc-50 dark:placeholder:text-zinc-500 dark:focus:border-zinc-500 dark:focus:bg-zinc-800 dark:focus:ring-zinc-50/10"
                  />
                </div>

                <button
                  type="submit"
                  disabled={isSubmitting || !currentEmail}
                  className="mt-2 flex h-11 w-full items-center justify-center rounded-xl bg-zinc-900 text-sm font-medium text-white transition-colors hover:bg-zinc-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-zinc-900 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-zinc-50 dark:text-zinc-900 dark:hover:bg-zinc-200 dark:focus-visible:outline-zinc-50"
                >
                  {isSubmitting ? "Saving..." : "Save changes"}
                </button>
              </form>
            </div>

            <div className="w-full rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
              <div className="mb-6">
                <h2 className="text-lg font-semibold">Change password</h2>
                <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
                  Use at least 8 characters for your new password.
                </p>
              </div>

              <form className="space-y-5" onSubmit={handlePasswordSubmit}>
                <div className="space-y-2">
                  <label
                    htmlFor="currentPassword"
                    className="block text-sm font-medium text-zinc-700 dark:text-zinc-300"
                  >
                    Current password
                  </label>
                  <input
                    id="currentPassword"
                    name="currentPassword"
                    type="password"
                    autoComplete="current-password"
                    value={currentPassword}
                    onChange={(event) => setCurrentPassword(event.target.value)}
                    required
                    className="block w-full rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2.5 text-zinc-900 placeholder:text-zinc-400 transition-colors outline-none focus:border-zinc-400 focus:bg-white focus:ring-2 focus:ring-zinc-900/10 dark:border-zinc-700 dark:bg-zinc-800/50 dark:text-zinc-50 dark:placeholder:text-zinc-500 dark:focus:border-zinc-500 dark:focus:bg-zinc-800 dark:focus:ring-zinc-50/10"
                  />
                </div>

                <div className="space-y-2">
                  <label
                    htmlFor="newPassword"
                    className="block text-sm font-medium text-zinc-700 dark:text-zinc-300"
                  >
                    New password
                  </label>
                  <input
                    id="newPassword"
                    name="newPassword"
                    type="password"
                    autoComplete="new-password"
                    value={newPassword}
                    onChange={(event) => setNewPassword(event.target.value)}
                    minLength={8}
                    required
                    className="block w-full rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2.5 text-zinc-900 placeholder:text-zinc-400 transition-colors outline-none focus:border-zinc-400 focus:bg-white focus:ring-2 focus:ring-zinc-900/10 dark:border-zinc-700 dark:bg-zinc-800/50 dark:text-zinc-50 dark:placeholder:text-zinc-500 dark:focus:border-zinc-500 dark:focus:bg-zinc-800 dark:focus:ring-zinc-50/10"
                  />
                </div>

                <button
                  type="submit"
                  disabled={isUpdatingPassword || !currentEmail}
                  className="flex h-11 w-full items-center justify-center rounded-xl border border-zinc-200 bg-white text-sm font-medium transition-colors hover:bg-zinc-100 disabled:cursor-not-allowed disabled:opacity-60 dark:border-zinc-700 dark:bg-zinc-900 dark:hover:bg-zinc-800"
                >
                  {isUpdatingPassword ? "Updating..." : "Update password"}
                </button>
              </form>
            </div>

            <div className="w-full rounded-2xl border border-red-200 bg-white p-8 shadow-sm dark:border-red-900/50 dark:bg-zinc-900">
              <h2 className="text-lg font-semibold text-red-700 dark:text-red-400">
                Delete account
              </h2>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
                Permanently remove your account from the system.
              </p>
              <button
                type="button"
                disabled={isDeleting || !currentEmail}
                onClick={handleDeleteAccount}
                className="mt-4 rounded-xl bg-red-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-red-500 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isDeleting ? "Deleting..." : "Delete account"}
              </button>
            </div>

            {message && (
              <p
                aria-live="polite"
                className={`text-sm ${
                  isError
                    ? "text-red-600 dark:text-red-400"
                    : "text-green-700 dark:text-green-400"
                }`}
              >
                {message}
              </p>
            )}
          </>
        )}
      </div>
    </main>
  );
}
