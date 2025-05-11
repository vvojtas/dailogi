import { type Page, type Locator, expect } from "@playwright/test";

/**
 * Page Object Model for Profile page
 */
export class ProfilePage {
  readonly page: Page;
  readonly profileManager: Locator;
  readonly welcomeHeader: Locator;
  readonly apiKeyInput: Locator;
  readonly saveApiKeyButton: Locator;
  readonly deleteApiKeyButton: Locator;
  readonly apiKeyStatus: Locator;
  readonly statusBadgeActive: Locator;
  readonly statusBadgeInactive: Locator;

  constructor(page: Page) {
    this.page = page;
    this.profileManager = page.getByTestId("profile-api-key-manager");
    this.welcomeHeader = page.getByTestId("profile-welcome-header");
    this.apiKeyInput = page.getByTestId("api-key-input");
    this.saveApiKeyButton = page.getByTestId("save-api-key-button");
    this.deleteApiKeyButton = page.getByTestId("delete-api-key-button");
    this.apiKeyStatus = page.getByTestId("api-key-status");
    this.statusBadgeActive = page.getByTestId("api-key-status-badge-active");
    this.statusBadgeInactive = page.getByTestId("api-key-status-badge-inactive");
  }

  /**
   * Navigate to profile page
   */
  async goto() {
    await this.page.goto("/profile");
  }

  /**
   * Assert that profile page is loaded
   */
  async assertPageLoaded() {
    await expect(this.page).toHaveTitle(/Profil użytkownika/);
    await expect(this.profileManager).toBeVisible();
    await expect(this.welcomeHeader).toBeVisible();
    await expect(this.apiKeyInput).toBeEnabled();
  }

  /**
   * Enter an API key in the input field
   */
  async enterApiKey(key: string) {
    await this.apiKeyInput.waitFor({ state: "visible" });
    await this.apiKeyInput.fill(key);
  }

  /**
   * Save current API key
   */
  async saveApiKey() {
    await this.saveApiKeyButton.waitFor({ state: "visible" });
    await expect(this.saveApiKeyButton).toBeEnabled();

    console.log("ProfilePage: About to click save API key button");

    await this.saveApiKeyButton.click();

    try {
      console.log("ProfilePage: Waiting for save API key response...");
      const response = await this.page.waitForResponse(
        (resp) => resp.url().includes("/api/users/current/api-key") && resp.request().method() === "PUT",
        { timeout: 10000 }
      );
      console.log("ProfilePage: Received API response for saving API key");
      const responseBody = await response.json();
      console.log(`ProfilePage: API Response Status: ${response.status()}, Body: ${JSON.stringify(responseBody)}`);
    } catch (error) {
      console.error("ProfilePage: Error waiting for API response or processing it in saveApiKey:", error);
    }
  }

  /**
   * Delete current API key with confirmation
   */
  async deleteApiKey() {
    await this.deleteApiKeyButton.waitFor({ state: "visible" });
    await expect(this.deleteApiKeyButton).toBeEnabled();

    // First click to show confirmation
    await this.deleteApiKeyButton.click();

    // Wait for confirm text to appear
    await expect(this.deleteApiKeyButton).toHaveText("Potwierdź wymazanie");

    // Second click to confirm deletion
    await Promise.all([
      this.page.waitForResponse(
        (resp) => resp.url().includes("/api/users/current/api-key") && resp.request().method() === "DELETE"
      ),
      this.deleteApiKeyButton.click(),
    ]);
  }

  /**
   * Check if API key is currently set (based on badge visibility)
   */
  async hasApiKey(): Promise<boolean> {
    try {
      await this.statusBadgeActive.waitFor({ state: "visible", timeout: 1000 });
      return true;
    } catch {
      return false;
    }
  }
}
