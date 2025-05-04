import { type Page, type Locator, expect } from "@playwright/test";

/**
 * Page Object Model for Character Details page
 */
export class CharacterDetailsPage {
  readonly page: Page;
  readonly characterNameHeading: Locator;
  readonly shortDescription: Locator;
  readonly biography: Locator;
  readonly backToGalleryButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.characterNameHeading = page.getByTestId("character-detail-name");
    this.shortDescription = page.getByTestId("character-detail-short-desc");
    this.biography = page.getByTestId("character-detail-bio");
    this.backToGalleryButton = page.getByTestId("back-to-gallery-btn");
  }

  /**
   * Navigate to character details page by ID
   */
  async goto(characterId: number) {
    await this.page.goto(`/characters/${characterId}`);
  }

  /**
   * Assert that the details page is loaded
   */
  async assertPageLoaded() {
    await expect(this.page).toHaveTitle(/Profil postaci/);
    await expect(this.characterNameHeading).toBeVisible();
  }

  /**
   * Verify character details match the expected data
   */
  async verifyCharacterDetails(characterData: { name: string; shortDescription: string; biography: string }) {
    await expect(this.characterNameHeading).toHaveText(characterData.name);
    await expect(this.shortDescription).toHaveText(characterData.shortDescription);
    await expect(this.biography).toHaveText(characterData.biography);
  }

  /**
   * Navigate back to the gallery
   */
  async backToGallery() {
    await this.backToGalleryButton.click();
  }
}
