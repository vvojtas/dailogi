import { type Page, type Locator, expect } from "@playwright/test";

export class HomePage {
  readonly page: Page;
  readonly heading: Locator;
  readonly loginButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.locator("h1").first();
    this.loginButton = page.getByRole("button", { name: /login/i });
  }

  async goto() {
    await this.page.goto("/");
  }

  async assertPageLoaded() {
    await expect(this.page).toHaveTitle(/d-AI-logi/);
    await expect(this.heading).toBeVisible();
  }

  async clickLogin() {
    await this.loginButton.click();
  }
}
