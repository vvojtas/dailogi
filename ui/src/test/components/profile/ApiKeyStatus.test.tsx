import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { ApiKeyStatus } from "@/components/profile/ApiKeyStatus";

describe("ApiKeyStatus Component", () => {
  it("displays green badge when API key is present", () => {
    render(<ApiKeyStatus hasApiKey={true} />);

    const badge = screen.getByText("Klucz leży bezpiecznie w naszych archiwach");
    expect(badge).toBeInTheDocument();
    expect(badge.className).toContain("bg-green-500");
  });

  it("displays secondary badge when API key is not present", () => {
    render(<ApiKeyStatus hasApiKey={false} />);

    const badge = screen.getByText("Jeszcze nie powierzono nam klucza API");
    expect(badge).toBeInTheDocument();
    expect(badge.className).toContain("bg-secondary");
  });
});
