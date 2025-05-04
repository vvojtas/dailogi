import { test, expect } from "@playwright/test";
import { AuthHelper } from "./models/AuthHelper"; // Assuming AuthHelper is still needed for logout

// Test using saved authentication state (uses 'authenticated' project)
test.describe("Functionality for authenticated users", () => {
  // This test will be run as part of the 'authenticated' project
  test("logged in user sees their name", async ({ page }) => {
    await page.goto("/");

    // Authentication state is already set by auth.setup.ts
    const usernameDisplay = page.getByTestId("username-display");
    await expect(usernameDisplay).toBeVisible();
    await expect(usernameDisplay).toHaveText("test"); // Assuming 'test' is the expected username
  });

  test("user can logout", async ({ page }) => {
    await page.goto("/");

    // Verify user is logged in at the beginning
    const authHelper = new AuthHelper(page);
    expect(await authHelper.isLoggedIn()).toBeTruthy();

    // Perform logout
    await authHelper.logout();

    // Verify user is successfully logged out
    expect(await authHelper.isLoggedIn()).toBeFalsy();
  });
});
