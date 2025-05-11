import { test, expect } from "@playwright/test";

// Test for unauthenticated access to profile
test.describe("Profile page - unauthenticated", () => {
  test("redirects to login when accessed without authentication", async ({ page }) => {
    // Try to access profile page directly
    await page.goto("/profile");

    // Verify redirect to login page
    await expect(page).toHaveURL(/.*login/);
    await expect(page.getByTestId("login-page")).toBeVisible();
  });
});
