import { defineConfig, devices } from "@playwright/test";
import path from "path";
import { fileURLToPath } from "url";
import dotenv from "dotenv";

// ES Modules equivalent for __dirname
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
dotenv.config({ path: path.resolve(__dirname, ".env.e2e") });

const springBackendUrl = process.env.SPRING_BACKEND_BASE_URL ?? "http://localhost:8080";
if (!process.env.SPRING_BACKEND_BASE_URL) {
  console.warn(
    `WARN: SPRING_BACKEND_BASE_URL not found in process.env (expected from .env.e2e), defaulting webServer to ${springBackendUrl}`
  );
}

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: "./e2e",
  /* Maximum time one test can run for. */
  timeout: 30 * 1000,
  expect: {
    /**
     * Maximum time expect() should wait for the condition to be met.
     * For example in `await expect(locator).toHaveText();`
     */
    timeout: 5000,
  },
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: [["html"], ["json", { outputFile: "playwright-report/test-results.json" }]],
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    baseURL: "http://localhost:3000",
    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: "on-first-retry",
    /* Take screenshot on test failure */
    screenshot: "only-on-failure",
  },

  /* Configure projects for major browsers */
  projects: [
    // Setup project that will create a logged-in state
    {
      name: "setup",
      testMatch: /auth\.setup\.ts/,
    },

    {
      name: "chromium",
      use: {
        ...devices["Desktop Chrome"],
      },
      // Run only public tests and general example tests
      testMatch: ["**/*public.test.ts", "**/*example.spec.ts"],
    },

    // Project for authenticated tests
    {
      name: "authenticated",
      use: {
        ...devices["Desktop Chrome"],
        // Use the authenticated state from setup
        storageState: path.join(__dirname, "e2e/.auth/user.json"),
      },
      dependencies: ["setup"],
      // Run only tests requiring authentication and general example tests
      testMatch: ["**/*authenticated.test.ts", "**/*example.spec.ts", "**/*character-creation.test.ts"],
    },
  ],

  /* Run your local dev server before starting the tests */
  webServer: {
    command: `cross-env SPRING_BACKEND_BASE_URL=${springBackendUrl} npm run dev`,
    url: "http://localhost:3000",
    reuseExistingServer: !process.env.CI,
  },
});
