import { test, expect, Page, Locator } from "@playwright/test";

/**
 * E2E-tester för settings-sidan (profil, lösenord och user-scopade tasks via
 * /api/users/{email}/... till Java-backenden).
 *
 * Backenden är in-memory och delas mellan tester och mellan desktop-/mobil-
 * projekten, så varje test registrerar en egen unik användare. Varje ny
 * användare seedas med 3 egna tasks av backenden, vilket ger testerna en känd
 * och isolerad utgångspunkt.
 */

const USER_EMAIL_KEY = "todo-devsecops-user-email";
const SEEDED_FIRST_TASK = "Plan the next sprint";

function uniqueUser() {
  const id = `${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
  return {
    username: `settings-user-${id}`,
    email: `settings${id}@example.com`,
    password: "Sup3rSecret!",
  };
}

async function registerAndOpenSettings(
  page: Page,
): Promise<{ username: string; email: string; password: string }> {
  const user = uniqueUser();
  const createResponse = await page.request.post("/api/users", { data: user });
  expect(createResponse.status(), "user-setup ska svara 201").toBe(201);

  // Settings-sidan läser inloggad användare från sessionStorage vid mount;
  // init-scriptet körs före sidans skript även efter reload.
  await page.addInitScript(
    ({ key, email }) => sessionStorage.setItem(key, email),
    { key: USER_EMAIL_KEY, email: user.email },
  );
  await page.goto("/settings");
  await expect(page.getByText(`Signed in as ${user.email}`)).toBeVisible();
  return user;
}

function tasksSection(page: Page): Locator {
  return page
    .locator("section")
    .filter({ has: page.getByRole("heading", { name: "Tasks" }) });
}

function taskRows(page: Page): Locator {
  return tasksSection(page).locator("div.rounded-xl");
}

test.describe("Settings", () => {
  test("kräver inloggning och länkar till sign-in utan session", async ({
    page,
  }) => {
    await page.goto("/settings");

    await expect(
      page.getByRole("heading", { name: "Sign in required" }),
    ).toBeVisible();
    await page.getByRole("link", { name: "Go to sign in" }).click();
    await expect(page).toHaveURL(/\/sign-in$/);
  });

  test("laddar profil och de tre seedade taskarna för inloggad användare", async ({
    page,
  }) => {
    const user = await registerAndOpenSettings(page);

    await expect(page.getByLabel("Username")).toHaveValue(user.username);
    await expect(taskRows(page)).toHaveCount(3);
    await expect(taskRows(page).first().locator("input").first()).toHaveValue(
      SEEDED_FIRST_TASK,
    );
  });

  test("uppdaterar användarnamnet och persisterar det efter omladdning", async ({
    page,
  }) => {
    await registerAndOpenSettings(page);
    const newUsername = `renamed-${Date.now()}`;

    await page.getByLabel("Username").fill(newUsername);

    const patchResponsePromise = page.waitForResponse(
      (response) =>
        response.url().includes("/api/users/") &&
        response.request().method() === "PATCH",
    );
    await page.getByRole("button", { name: "Save username" }).click();
    const patchResponse = await patchResponsePromise;
    expect(patchResponse.status(), "PATCH ska svara 200").toBe(200);

    await expect(page.getByText("Username updated")).toBeVisible();

    await page.reload();
    await expect(page.getByLabel("Username")).toHaveValue(newUsername);
  });

  test("uppdaterar en task-titel via settings som persisteras efter omladdning", async ({
    page,
  }) => {
    await registerAndOpenSettings(page);
    const newTitle = `Settings task ${Date.now()}`;
    const firstRow = taskRows(page).first();

    await firstRow.locator("input").first().fill(newTitle);

    const putResponsePromise = page.waitForResponse(
      (response) =>
        response.url().includes("/tasks/") &&
        response.request().method() === "PUT",
    );
    await firstRow.getByRole("button", { name: "Save" }).click();
    const putResponse = await putResponsePromise;
    expect(putResponse.status(), "PUT ska svara 200").toBe(200);

    await expect(page.getByText("Task updated.")).toBeVisible();

    await page.reload();
    await expect(taskRows(page).first().locator("input").first()).toHaveValue(
      newTitle,
    );
  });

  test("visar fel från backend när nuvarande lösenord är felaktigt", async ({
    page,
  }) => {
    await registerAndOpenSettings(page);

    await page.getByPlaceholder("Current password").fill("WrongPassword1!");
    await page.getByPlaceholder("New password").fill("NewSecret123!");

    const patchResponsePromise = page.waitForResponse(
      (response) =>
        response.url().includes("/password") &&
        response.request().method() === "PATCH",
    );
    await page.getByRole("button", { name: "Change password" }).click();
    const patchResponse = await patchResponsePromise;
    expect(patchResponse.status(), "fel lösenord ska ge 400").toBe(400);

    const errorMessage = page.getByText("Current password is incorrect", {
      exact: true,
    });
    await expect(errorMessage).toBeVisible();
    await expect(errorMessage).toHaveClass(/text-red-600/);
  });
});
