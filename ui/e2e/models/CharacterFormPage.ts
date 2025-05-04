import { type Page, type Locator, expect } from "@playwright/test";

/**
 * Page Object Model for Character Form page
 */
export class CharacterFormPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly form: Locator;
  readonly nameInput: Locator;
  readonly shortDescInput: Locator;
  readonly bioInput: Locator;
  readonly llmSelect: Locator;
  readonly submitButton: Locator;
  readonly cancelButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.locator("h1:has-text('Powołaj nową postać')");
    this.form = page.getByTestId("character-form");
    this.nameInput = page.getByTestId("character-name-input");
    this.shortDescInput = page.getByTestId("character-short-desc-input");
    this.bioInput = page.getByTestId("character-bio-input");
    this.llmSelect = page.getByTestId("character-llm-select");
    this.submitButton = page.getByTestId("character-submit-btn");
    this.cancelButton = page.getByTestId("character-cancel-btn");
  }

  /**
   * Navigate to new character form page
   */
  async goto() {
    await this.page.goto("/characters/new");
  }

  /**
   * Assert that the form page is loaded
   */
  async assertPageLoaded() {
    await expect(this.page).toHaveTitle(/Powołaj nową postać/);
    await expect(this.heading).toBeVisible();
    await expect(this.form).toBeVisible();
  }

  /**
   * Fill the character form with test data
   */
  async fillForm(characterData: { name: string; shortDescription: string; biography: string; llmId?: string }) {
    await this.nameInput.fill(characterData.name);
    await this.shortDescInput.fill(characterData.shortDescription);
    await this.bioInput.fill(characterData.biography);

    // Select LLM if provided
    if (characterData.llmId) {
      await this.llmSelect.click();
      await this.page.getByRole("option", { name: new RegExp(characterData.llmId) }).click();
    }
  }

  /**
   * Submit the character form
   */
  async submitForm() {
    await this.submitButton.click();
  }

  /**
   * Cancel form submission
   */
  async cancelForm() {
    await this.cancelButton.click();
  }

  /**
   * Create a character with the given data
   */
  async createCharacter(characterData: { name: string; shortDescription: string; biography: string; llmId?: string }) {
    await this.fillForm(characterData);
    await this.submitForm();
  }
}
