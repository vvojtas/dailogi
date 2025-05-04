import { type Page, type Locator, expect } from "@playwright/test";

/**
 * Page Object Model for Characters Gallery page
 */
export class CharactersPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly createCharacterButton: Locator;
  readonly characterGrid: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.locator("h1:has-text('Galeria Postaci')");
    this.createCharacterButton = page.getByTestId("create-character-btn");
    this.characterGrid = page.getByTestId("character-grid");
  }

  /**
   * Navigate to characters page
   */
  async goto() {
    await this.page.goto("/characters");
  }

  /**
   * Assert that the characters page is loaded
   */
  async assertPageLoaded() {
    await expect(this.page).toHaveTitle(/Galeria postaci/);
    await expect(this.heading).toBeVisible();
  }

  /**
   * Click the create character button
   */
  async clickCreateCharacter() {
    await this.createCharacterButton.click();
  }

  /**
   * Check if a character with the given name exists in the grid
   */
  async hasCharacterWithName(name: string): Promise<boolean> {
    try {
      // Locate the specific title element using its data-testid and text content
      const titleLocator = this.characterGrid.locator(`[data-testid^="character-card-title-"]:has-text("${name}")`);

      console.log(`Attempting to locate title with data-testid and text: "${name}"`);

      // Wait for the specific title element to be visible
      await titleLocator.waitFor({ state: "visible", timeout: 5000 });

      console.log(`Successfully located title: "${name}"`);
      return true; // If waitFor succeeds, the element exists and is visible
    } catch (error) {
      // If waitFor times out or throws an error, the element wasn't found
      console.log(`Could not locate title: "${name}" within the timeout.`);
      console.error(error);
      return false;
    }
  }

  /**
   * Get character card by ID
   */
  getCharacterCardById(id: number): Locator {
    return this.page.getByTestId(`character-card-${id}`);
  }

  /**
   * Click view details button for a character
   */
  async viewCharacterDetails(id: number) {
    await this.page.getByTestId(`view-character-${id}`).click();
  }
}
