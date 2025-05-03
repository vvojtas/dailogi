import { test, expect } from "@playwright/test";

test.describe("Example E2E test", () => {
  test("homepage has the correct title", async ({ page }) => {
    await page.goto("/");

    // Assert that the page has the expected title
    await expect(page).toHaveTitle(/d-AI-logi/i);
  });

  test("navigation works", async ({ page }) => {
    await page.goto("/");

    // Click on a navigation link (adjust selector as needed)
    // await page.click('text=Characters');

    // Assert that we've navigated to the expected URL
    // await expect(page).toHaveURL(/.*characters/);
  });
});
