import { type Page, expect } from "@playwright/test";

/**
 * Helper class providing authentication functions for e2e tests
 */
export class AuthHelper {
  readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  /**
   * Logs in test user to the application
   *
   * @param username Username (defaults to 'test')
   * @param password User password (defaults to 'test-dailogi')
   */
  async login(username = "test", password = "test-dailogi") {
    // Navigate to login page
    await this.page.goto("/login");

    // Ensure login page container is visible
    await expect(this.page.getByTestId("login-page")).toBeVisible();

    // Get the login button and wait for it to be enabled (hydration complete)
    const loginButton = this.page.getByTestId("login-submit-button");
    await expect(loginButton).toBeEnabled();

    // Wait for the username input to be visible before filling
    const usernameInput = this.page.getByTestId("username-input");
    await usernameInput.waitFor({ state: "visible" });

    // Fill in login form
    await usernameInput.fill(username);
    await this.page.getByTestId("password-input").fill(password);

    // Click login button and wait for the network response from the API
    await Promise.all([
      this.page.waitForResponse((resp) => resp.url().includes("/api/auth/login") && resp.status() === 200),
      loginButton.click(),
    ]);

    // Now that the API call succeeded, wait for navigation (if any) or check state
    await this.page.waitForURL("/"); // Keep this if redirect is expected

    // Verify user is logged in by checking username display
    await expect(this.page.getByTestId("username-display")).toHaveText(username);
  }

  /**
   * Logs out user from the application
   */
  async logout() {
    // Get the logout button
    const logoutButton = this.page.getByTestId("logout-button");
    // Wait for the button to be visible before clicking
    await logoutButton.waitFor({ state: "visible" });

    // Click logout and wait for the network response
    // click() automatically waits for the button to be visible and enabled
    await Promise.all([
      this.page.waitForResponse((resp) => resp.url().includes("/api/auth/logout") && resp.status() === 200),
      logoutButton.click(),
    ]);

    // Wait for redirect after logout
    await this.page.waitForURL("/");

    // Verify user is logged out - login link should be visible
    await expect(this.page.getByTestId("login-link")).toBeVisible();
  }

  /**
   * Checks if user is logged in
   *
   * @returns true if user is logged in, false otherwise
   */
  async isLoggedIn(): Promise<boolean> {
    // Use a try-catch or check visibility with a timeout for robustness
    try {
      // Check if the username display element is visible within a short timeout
      await expect(this.page.getByTestId("username-display")).toBeVisible({ timeout: 1000 });
      return true;
    } catch {
      // If the element is not visible within the timeout, assume not logged in
      // console.debug("isLoggedIn check failed"); // Optional: for debugging
      return false;
    }
  }
}
