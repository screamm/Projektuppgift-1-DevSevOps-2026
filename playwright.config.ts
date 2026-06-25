import { defineConfig, devices } from "@playwright/test";

// E2E-testerna körs mot PRODUKTIONSBYGGET (next build + next start), inte dev-servern.
// Java-backenden startas från Backend/target/*.jar — bygg den först:
//   ./Backend/mvnw -f Backend/pom.xml -DskipTests package
export default defineConfig({
  testDir: "./tests/e2e",
  fullyParallel: false,
  workers: 1,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [["list"], ["html", { open: "never" }]] : "list",
  use: {
    baseURL: "http://localhost:3000",
    trace: "on-first-retry",
  },
  projects: [
    {
      name: "chromium-desktop",
      use: { ...devices["Desktop Chrome"] },
    },
    {
      name: "mobile-chrome",
      use: { ...devices["Pixel 7"] },
    },
  ],
  webServer: [
    {
      command: "sh -c 'exec java -jar Backend/target/*.jar'",
      url: "http://localhost:8080/api/tasks",
      reuseExistingServer: !process.env.CI,
      timeout: 60_000,
    },
    {
      command: "npm run start",
      url: "http://localhost:3000",
      reuseExistingServer: !process.env.CI,
      timeout: 60_000,
    },
  ],
});
