import { test, expect } from "@playwright/test";
import { AuthHelper } from "./models/AuthHelper";

// Test verifying login and logout process
test.describe("Authentication", () => {
  test("user can login and logout", async ({ page }) => {
    const authHelper = new AuthHelper(page);

    // Check user is not logged in at the beginning
    await page.goto("/");
    expect(await authHelper.isLoggedIn()).toBeFalsy();

    // Login
    await authHelper.login();

    // Verify user is logged in
    expect(await authHelper.isLoggedIn()).toBeTruthy();

    // Logout
    await authHelper.logout();

    // Verify user is logged out
    expect(await authHelper.isLoggedIn()).toBeFalsy();
  });
});
