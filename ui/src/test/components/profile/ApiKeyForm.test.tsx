import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ApiKeyForm } from "@/components/profile/ApiKeyForm";

// Mock the useHydration hook
vi.mock("@/lib/hooks/useHydration", () => ({
  useHydration: () => true,
}));

describe("ApiKeyForm Component", () => {
  const mockProps = {
    apiKey: "",
    hasApiKey: false,
    loading: false,
    onSave: vi.fn(),
    onDelete: vi.fn(),
    onInputChange: vi.fn(),
  };

  it("renders input field and buttons", () => {
    render(<ApiKeyForm {...mockProps} />);

    expect(screen.getByTestId("api-key-input")).toBeInTheDocument();
    expect(screen.getByTestId("save-api-key-button")).toBeInTheDocument();
    expect(screen.getByTestId("delete-api-key-button")).toBeInTheDocument();
  });

  it("displays text input when no API key exists", () => {
    render(<ApiKeyForm {...mockProps} />);

    const input = screen.getByTestId("api-key-input");
    expect(input).toHaveAttribute("type", "text");
    expect(input).toHaveAttribute("placeholder", "Wprowadź sekretny klucz do otwarcia OpenRouter");
  });

  it("displays password input when API key exists", () => {
    render(<ApiKeyForm {...mockProps} hasApiKey={true} />);

    const input = screen.getByTestId("api-key-input");
    expect(input).toHaveAttribute("type", "password");
    expect(input).toHaveAttribute("placeholder", "Twój klucz jest zapisany i zabezpieczony");
  });

  it("disables save button when input is empty", () => {
    render(<ApiKeyForm {...mockProps} />);

    const saveButton = screen.getByTestId("save-api-key-button");
    expect(saveButton).toBeDisabled();
  });

  it("disables delete button when no API key exists", () => {
    render(<ApiKeyForm {...mockProps} hasApiKey={false} />);

    const deleteButton = screen.getByTestId("delete-api-key-button");
    expect(deleteButton).toBeDisabled();
  });

  it("displays proper button labels based on hasApiKey", () => {
    // First render - hasApiKey false
    const { unmount } = render(<ApiKeyForm {...mockProps} hasApiKey={false} />);
    expect(screen.getByTestId("save-api-key-button")).toHaveTextContent("Uwiecznij klucz");

    // Clean up before second render
    unmount();

    // Second render - hasApiKey true
    render(<ApiKeyForm {...mockProps} hasApiKey={true} />);
    expect(screen.getByTestId("save-api-key-button")).toHaveTextContent("Podmień klucz");
  });

  it("calls onInputChange when typing in the input", async () => {
    const user = userEvent.setup();
    render(<ApiKeyForm {...mockProps} />);

    const input = screen.getByTestId("api-key-input");

    await user.type(input, "test-key");
    expect(mockProps.onInputChange).toHaveBeenCalled();
  });

  it("calls onSave when clicking the save button", async () => {
    const user = userEvent.setup();
    render(<ApiKeyForm {...mockProps} apiKey="test-key" />);

    const saveButton = screen.getByTestId("save-api-key-button");
    await user.click(saveButton);

    expect(mockProps.onSave).toHaveBeenCalledWith("test-key");
  });

  it("shows confirm deletion UI when clicking delete button", async () => {
    const user = userEvent.setup();
    render(<ApiKeyForm {...mockProps} hasApiKey={true} />);

    const deleteButton = screen.getByTestId("delete-api-key-button");
    expect(deleteButton).toHaveTextContent("Zlikwiduj klucz");

    await user.click(deleteButton);

    expect(deleteButton).toHaveTextContent("Potwierdź wymazanie");
    expect(mockProps.onDelete).not.toHaveBeenCalled(); // Not called on first click
  });

  it("calls onDelete when confirming deletion", async () => {
    const user = userEvent.setup();
    const onDeleteMock = vi.fn();

    render(<ApiKeyForm {...mockProps} hasApiKey={true} onDelete={onDeleteMock} />);

    const deleteButton = screen.getByTestId("delete-api-key-button");

    // First click shows confirmation
    await user.click(deleteButton);
    // Second click confirms deletion
    await user.click(deleteButton);

    expect(onDeleteMock).toHaveBeenCalledTimes(1);
  });

  it("disables inputs and buttons when loading", () => {
    render(<ApiKeyForm {...mockProps} loading={true} apiKey="test-key" hasApiKey={true} />);

    expect(screen.getByTestId("api-key-input")).toBeDisabled();
    expect(screen.getByTestId("save-api-key-button")).toBeDisabled();
    expect(screen.getByTestId("delete-api-key-button")).toBeDisabled();
    expect(screen.getByTestId("save-api-key-button")).toHaveTextContent("Zapisywanie...");
  });
});
