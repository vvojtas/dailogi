import { test, expect } from "@playwright/test";
import { CharactersPage } from "./models/CharactersPage";
import { CharacterFormPage } from "./models/CharacterFormPage";
import { CharacterDetailsPage } from "./models/CharacterDetailsPage";

// Generate unique character name to avoid duplicates during test runs
const uniqueSuffix = Date.now().toString().slice(-5);
const TEST_CHARACTER = {
  name: `Test Character ${uniqueSuffix}`,
  shortDescription: "A character created during automated testing",
  biography: "This character was automatically created by the e2e test suite to verify the character creation flow.",
};

// Test using saved authentication state (uses 'authenticated' project in the Playwright config)
test.describe("Character Creation", () => {
  test("authenticated user can create a new character", async ({ page }) => {
    // Initialize page objects
    const charactersPage = new CharactersPage(page);
    const characterFormPage = new CharacterFormPage(page);
    const characterDetailsPage = new CharacterDetailsPage(page);

    // Step 1: Go to the characters gallery page
    await charactersPage.goto();
    await charactersPage.assertPageLoaded();

    // Ensure the create character button is visible
    await expect(charactersPage.createCharacterButton).toBeVisible();

    // Step 2: Click on the create character button
    await charactersPage.clickCreateCharacter();

    // Verify we're on the character form page
    await characterFormPage.assertPageLoaded();

    // Step 3: Fill out the character form
    await characterFormPage.fillForm(TEST_CHARACTER);

    // Take a screenshot before submitting (for debugging)
    await page.screenshot({ path: "test-results/character-form-filled.png" });

    // Submit the form
    await characterFormPage.submitForm();

    // Step 4: Verify character details page shows correct information
    // After submission, we should be redirected to the character details page
    await characterDetailsPage.assertPageLoaded();

    // Verify the character details match what we entered
    await characterDetailsPage.verifyCharacterDetails(TEST_CHARACTER);

    // Take a screenshot of character details page (for debugging)
    await page.screenshot({ path: "test-results/character-details.png" });

    // Step 5: Go back to the gallery
    await characterDetailsPage.backToGallery();

    // Verify we're back at the characters page
    await charactersPage.assertPageLoaded();

    // Step 6: Verify the new character appears in the gallery
    // Wait for the grid to be visible with longer timeout
    await expect(charactersPage.characterGrid).toBeVisible({ timeout: 10000 });

    // Explicitly wait for at least one character card to appear in the grid
    await charactersPage.characterGrid
      .locator('[data-testid^="character-card-"]')
      .first()
      .waitFor({ state: "visible", timeout: 10000 });

    // Take a screenshot of gallery before checking for character
    await page.screenshot({ path: "test-results/character-gallery.png" });

    // Log test character name for debugging
    console.log(`Looking for character with name: ${TEST_CHARACTER.name}`);

    // Add a small delay (optional, may not be needed now)
    // await page.waitForTimeout(1000);

    // Verify our character is in the grid
    const characterExists = await charactersPage.hasCharacterWithName(TEST_CHARACTER.name);
    expect(characterExists, `Character "${TEST_CHARACTER.name}" should exist in the gallery`).toBeTruthy();

    // Take a screenshot of gallery with new character (for debugging)
    await page.screenshot({ path: "test-results/character-gallery-with-new.png" });
  });
});
