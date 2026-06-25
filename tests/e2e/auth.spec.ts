import { test, expect, Page } from "@playwright/test";

/**
 * E2E-tester för sign-up- och sign-in-flödena (POST /api/users och
 * /api/auth/login via Next-API:t till Java-backenden).
 *
 * Backendens användarregister är in-memory och delas mellan tester och mellan
 * desktop-/mobil-projekten, så varje test registrerar en unik användare
 * (unik e-post = unik nyckel i backendens user-map).
 */

function uniqueUser() {
  const id = `${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
  return {
    username: `e2e-user-${id}`,
    email: `e2e${id}@example.com`,
    password: "Sup3rSecret!",
  };
}

async function submitSignUpForm(
  page: Page,
  user: { username: string; email: string; password: string },
): Promise<void> {
  await page.getByLabel("Username").fill(user.username);
  await page.getByLabel("Email").fill(user.email);
  await page.getByLabel("Password").fill(user.password);
  await page.getByRole("button", { name: "Create account" }).click();
}

test.describe("Auth", () => {
  test("sign-up skapar ny användare och visar fel vid duplicerad registrering", async ({
    page,
  }) => {
    const user = uniqueUser();
    await page.goto("/sign-up");

    // Första registreringen lyckas - grönt bekräftelsemeddelande.
    await submitSignUpForm(page, user);
    const successMessage = page.getByText("User created", { exact: true });
    await expect(successMessage).toBeVisible();
    await expect(successMessage).toHaveClass(/text-green-700/);

    // Samma användare igen - formuläret nollställdes efter lyckad
    // registrering, så vi fyller i det på nytt.
    await submitSignUpForm(page, user);
    const errorMessage = page.getByText("User already exists", {
      exact: true,
    });
    await expect(errorMessage).toBeVisible();
    await expect(errorMessage).toHaveClass(/text-red-600/);
  });

  test("sign-in visar fel vid fel lösenord och omdirigerar till dashboard vid lyckad inloggning", async ({
    page,
  }) => {
    const user = uniqueUser();

    // Registrera användaren via API:t (testdata-setup, inte UI-flödet).
    const createResponse = await page.request.post("/api/users", {
      data: user,
    });
    expect(createResponse.ok(), "user-setup ska svara 2xx").toBe(true);

    await page.goto("/sign-in");

    // Fel lösenord -> felmeddelande i felfärg, ingen omdirigering.
    await page.getByLabel("Email").fill(user.email);
    await page.getByLabel("Password").fill("WrongPassword1!");
    await page.getByRole("button", { name: "Sign in" }).click();

    const errorMessage = page.getByText("Invalid email or password", {
      exact: true,
    });
    await expect(errorMessage).toBeVisible();
    await expect(errorMessage).toHaveClass(/text-red-600/);
    await expect(page).toHaveURL(/\/sign-in$/);

    // Rätt lösenord -> omdirigering till dashboarden.
    await page.getByLabel("Password").fill(user.password);
    await page.getByRole("button", { name: "Sign in" }).click();

    await page.waitForURL("**/dashboard");
    await expect(page.getByTestId("task-list")).toBeVisible();
  });
});
