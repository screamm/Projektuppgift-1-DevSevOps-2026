import { test, expect, Page, Locator } from "@playwright/test";

/**
 * E2E-tester för dashboarden (task-CRUD mot backendens in-memory-API).
 *
 * Designregler (backenden delas mellan tester OCH mellan desktop-/mobil-projekten):
 * - Varje test skapar tasks med UNIK titel och letar alltid upp dem via titeln,
 *   aldrig via index i listan.
 * - Seed-tasken "Plan the next sprint" raderas aldrig.
 * - Inga asserts på exakta totalsiffror i översiktskorten (state ackumuleras).
 */

const SEED_TASK_TITLE = "Plan the next sprint";

function uniqueTitle(prefix = "E2E"): string {
  return `${prefix} ${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
}

function taskItemByTitle(page: Page, title: string): Locator {
  return page.getByTestId("task-item").filter({ hasText: title });
}

async function openTaskForm(page: Page): Promise<void> {
  await page.getByTestId("add-task-button").click();
  await expect(page.getByTestId("task-form")).toBeVisible();
}

async function createTaskViaForm(
  page: Page,
  title: string,
  priority: "LOW" | "MEDIUM" | "HIGH" = "MEDIUM",
  dueDate?: string,
): Promise<void> {
  await openTaskForm(page);
  await page.getByTestId("task-title-input").fill(title);
  await page.getByTestId("task-priority-select").selectOption(priority);
  if (dueDate) {
    await page.getByTestId("task-due-date-input").fill(dueDate);
  }
  await page.getByTestId("task-submit").click();
  await expect(taskItemByTitle(page, title)).toBeVisible();
  await expect(page.getByTestId("task-form")).toBeHidden();
}

async function deleteTaskViaUi(page: Page, title: string): Promise<void> {
  await taskItemByTitle(page, title).getByTestId("task-delete-button").click();
  await expect(page.getByRole("dialog")).toBeVisible();
  await page.getByTestId("confirm-delete").click();
  await expect(page.getByRole("dialog")).toBeHidden();
  await expect(taskItemByTitle(page, title)).toHaveCount(0);
}

test.describe("Dashboard - tasks", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/dashboard");
    // Seed-datat garanterar minst en task, så listan ska alltid renderas.
    await expect(page.getByTestId("task-list")).toBeVisible();
  });

  test("visar seed-data från API:t med priority-etikett High", async ({
    page,
  }) => {
    const seedItem = taskItemByTitle(page, SEED_TASK_TITLE);

    await expect(seedItem).toBeVisible();
    await expect(seedItem.getByText("High", { exact: true })).toBeVisible();
    await expect(seedItem.getByText("Due 2026-06-15")).toBeVisible();
  });

  test("skapar en task via formuläret som syns i listan utan omladdning", async ({
    page,
  }) => {
    const title = uniqueTitle();

    // Markör som försvinner vid en sidomladdning/navigering.
    await page.evaluate(() => {
      (window as Window & { __noReloadMarker?: boolean }).__noReloadMarker =
        true;
    });

    await createTaskViaForm(page, title, "MEDIUM", "2026-06-20");

    const item = taskItemByTitle(page, title);
    await expect(item).toBeVisible();
    await expect(item.getByText("Medium", { exact: true })).toBeVisible();
    await expect(item.getByText("Due 2026-06-20")).toBeVisible();

    const markerSurvived = await page.evaluate(
      () =>
        (window as Window & { __noReloadMarker?: boolean }).__noReloadMarker ===
        true,
    );
    expect(markerSurvived, "sidan ska inte ha laddats om").toBe(true);

    // Städa upp via taskens egen titel.
    await deleteTaskViaUi(page, title);
  });

  test("bockar av en task som blir checked och genomstruken efter PUT-svar", async ({
    page,
  }) => {
    const title = uniqueTitle();
    await createTaskViaForm(page, title);

    const item = taskItemByTitle(page, title);
    const checkbox = item.getByTestId("task-checkbox");
    await expect(checkbox).not.toBeChecked();

    const putResponsePromise = page.waitForResponse(
      (response) =>
        response.url().includes("/api/tasks/") &&
        response.request().method() === "PUT",
    );
    await checkbox.click();
    const putResponse = await putResponsePromise;
    expect(putResponse.ok(), "PUT /api/tasks/:id ska svara 2xx").toBe(true);

    // Checkboxen är kontrollerad och blir checked först när PUT-svaret
    // uppdaterat state - expect pollar tills dess.
    await expect(checkbox).toBeChecked();
    await expect(item.getByText(title)).toHaveClass(/line-through/);

    await deleteTaskViaUi(page, title);
  });

  test("raderar en task efter bekräftelse i dialogen", async ({ page }) => {
    const title = uniqueTitle();
    await createTaskViaForm(page, title);

    await taskItemByTitle(page, title)
      .getByTestId("task-delete-button")
      .click();

    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    await expect(
      dialog.getByRole("heading", { name: "Delete task" }),
    ).toBeVisible();
    await expect(dialog).toContainText(title);

    await page.getByTestId("confirm-delete").click();

    await expect(dialog).toBeHidden();
    await expect(taskItemByTitle(page, title)).toHaveCount(0);
    // Seed-tasken ska vara orörd.
    await expect(taskItemByTitle(page, SEED_TASK_TITLE)).toBeVisible();
  });

  test("avbryter radering så att dialogen stängs och tasken finns kvar", async ({
    page,
  }) => {
    const title = uniqueTitle();
    await createTaskViaForm(page, title);

    await taskItemByTitle(page, title)
      .getByTestId("task-delete-button")
      .click();

    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    await expect(
      dialog.getByRole("heading", { name: "Delete task" }),
    ).toBeVisible();

    await page.getByTestId("cancel-delete").click();

    await expect(dialog).toBeHidden();
    await expect(taskItemByTitle(page, title)).toBeVisible();

    await deleteTaskViaUi(page, title);
  });

  test("stänger delete-dialogen med Escape utan att radera tasken", async ({
    page,
  }) => {
    const title = uniqueTitle();
    await createTaskViaForm(page, title);

    await taskItemByTitle(page, title)
      .getByTestId("task-delete-button")
      .click();

    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await page.keyboard.press("Escape");

    await expect(dialog).toBeHidden();
    await expect(taskItemByTitle(page, title)).toBeVisible();

    await deleteTaskViaUi(page, title);
  });

  test("visar backend-valideringsfel när titeln endast är mellanslag", async ({
    page,
  }) => {
    // Titel-inputen har bara `required` (ingen pattern/trim), så en titel med
    // enbart mellanslag passerar HTML-valideringen men stoppas av backendens
    // @NotBlank som ger 400.
    await openTaskForm(page);
    await page.getByTestId("task-title-input").fill("   ");
    await page.getByTestId("task-priority-select").selectOption("MEDIUM");

    const postResponsePromise = page.waitForResponse(
      (response) =>
        response.url().includes("/api/tasks") &&
        response.request().method() === "POST",
    );
    await page.getByTestId("task-submit").click();
    const postResponse = await postResponsePromise;
    expect(postResponse.status(), "blank titel ska ge 400").toBe(400);

    await expect(page.getByTestId("error-banner")).toContainText(
      "Title must not be empty",
    );
    // Formuläret ska vara kvar öppet så att användaren kan rätta felet.
    await expect(page.getByTestId("task-form")).toBeVisible();
  });

  test("visar backend-valideringsfel när titeln är längre än 100 tecken", async ({
    page,
  }) => {
    // Inputen saknar maxLength, så en titel > 100 tecken når backendens
    // @Size(max = 100) som ger 400.
    const tooLongTitle = `E2E ${"x".repeat(100)}`;

    await openTaskForm(page);
    await page.getByTestId("task-title-input").fill(tooLongTitle);

    const postResponsePromise = page.waitForResponse(
      (response) =>
        response.url().includes("/api/tasks") &&
        response.request().method() === "POST",
    );
    await page.getByTestId("task-submit").click();
    const postResponse = await postResponsePromise;
    expect(postResponse.status(), "för lång titel ska ge 400").toBe(400);

    await expect(page.getByTestId("error-banner")).toContainText(
      "Title must be at most 100 characters",
    );
    await expect(taskItemByTitle(page, tooLongTitle)).toHaveCount(0);
  });
});
