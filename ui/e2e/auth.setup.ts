import { test as setup, expect } from "@playwright/test";
import { AuthHelper } from "./models/AuthHelper";

/**
 * Global setup for e2e tests that logs in a test user
 * and saves authentication state for reuse in tests.
 *
 * Automatically run before tests via configuration in playwright.config.ts
 */
setup("authenticate", async ({ page }) => {
  const authHelper = new AuthHelper(page);

  // Login test user
  await authHelper.login();

  // Verify login was successful
  expect(await authHelper.isLoggedIn()).toBeTruthy();

  // Save authentication cookies/storage for reuse in tests
  await page.context().storageState({ path: "e2e/.auth/user.json" });
});
