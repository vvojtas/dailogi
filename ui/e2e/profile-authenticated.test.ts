import { test, expect } from "@playwright/test";
import { ProfilePage } from "./models/ProfilePage";

// Tests for authenticated user API key management
test.describe("Profile API key management", () => {
  let profilePage: ProfilePage;

  test.beforeEach(async ({ page }) => {
    profilePage = new ProfilePage(page);
    await profilePage.goto();
    await profilePage.assertPageLoaded();
  });

  test("complete API key management flow", async () => {
    // Verify if the API key is not set initially (clean state for test)
    if (await profilePage.hasApiKey()) {
      await profilePage.deleteApiKey();

      // Verify the inactive badge is shown after deletion
      await expect(profilePage.statusBadgeInactive).toBeVisible();
    }

    // Enter a test API key
    const testApiKey = "test-api-key-" + Date.now();
    await profilePage.enterApiKey(testApiKey);

    // Save the API key
    await profilePage.saveApiKey();

    // Verify the API key is saved (active badge is shown)
    await expect(profilePage.statusBadgeActive).toBeVisible({ timeout: 15000 });

    // Check input field is emptied after saving
    await expect(profilePage.apiKeyInput).toHaveValue("");

    // Delete the API key
    await profilePage.deleteApiKey();

    // Verify the API key is deleted (inactive badge is shown)
    await expect(profilePage.statusBadgeInactive).toBeVisible();
  });

  test("handles API error responses appropriately", async ({ page }) => {
    // Mock API error response for PUT request
    await page.route("**/api/users/current/api-key", async (route) => {
      const method = route.request().method();

      if (method === "PUT") {
        await route.fulfill({
          status: 400,
          contentType: "application/json",
          body: JSON.stringify({ message: "Nieprawidłowy format klucza API" }),
        });
      } else {
        // Allow other requests to proceed normally
        await route.continue();
      }
    });

    // Try to save an invalid API key
    await profilePage.enterApiKey("invalid-key");
    await profilePage.saveApiKeyButton.click();

    // Verify error toast appears - library-specific selector for sonner
    await expect(
      page.locator('[data-sonner-toast][data-type="error"]').filter({
        hasText: "Nasi spece mówią, że poprawne klucze wyglądają inaczej",
      })
    ).toBeVisible({
      timeout: 10000,
    });

    // Ensure API key status remains unchanged
    if (await profilePage.hasApiKey()) {
      await expect(profilePage.statusBadgeActive).toBeVisible();
    } else {
      await expect(profilePage.statusBadgeInactive).toBeVisible();
    }
  });
});
