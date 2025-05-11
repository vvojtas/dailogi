import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { ProfileApiKeyManager } from "@/components/profile/ProfileApiKeyManager";
import { ApiKeyStatus } from "@/components/profile/ApiKeyStatus";
import { ApiKeyForm } from "@/components/profile/ApiKeyForm";

// Mock the API key components
vi.mock("@/components/profile/ApiKeyStatus", () => ({
  ApiKeyStatus: vi.fn(() => <div data-testid="mocked-api-key-status">Status Mock</div>),
}));

vi.mock("@/components/profile/ApiKeyForm", () => ({
  ApiKeyForm: vi.fn(() => <div data-testid="mocked-api-key-form">Form Mock</div>),
}));

// Mock the hooks
vi.mock("@/lib/hooks/useApiKey", () => ({
  useApiKey: () => ({
    apiKey: "test-key",
    hasApiKey: true,
    loading: false,
    setApiKey: vi.fn(),
    saveKey: vi.fn(),
    deleteKey: vi.fn(),
  }),
}));

// Improved mock for auth store - we need to precisely simulate the component structure
vi.mock("@/lib/stores/auth.store", () => ({
  useAuthStore: vi.fn(() => {
    return (selector: (state: { getUser: () => { name: string }; getIsLoggedIn: () => boolean }) => unknown) => {
      // Simulate calling the selector on the state object
      const state = {
        getUser: () => ({ name: "Test User" }),
        getIsLoggedIn: () => true,
      };
      return selector(state);
    };
  }),
}));

// Mock Tooltip components to avoid rendering issues
vi.mock("@/components/ui/tooltip", () => ({
  Tooltip: ({ children }: React.PropsWithChildren) => <div>{children}</div>,
  TooltipContent: ({ children }: React.PropsWithChildren) => <div>{children}</div>,
  TooltipTrigger: ({ children }: React.PropsWithChildren) => <div>{children}</div>,
}));

// Mock InfoIcon
vi.mock("lucide-react", () => ({
  InfoIcon: () => <div data-testid="info-icon">Info</div>,
}));

describe("ProfileApiKeyManager Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders child components", () => {
    render(<ProfileApiKeyManager />);

    expect(screen.getByTestId("mocked-api-key-status")).toBeInTheDocument();
    expect(screen.getByTestId("mocked-api-key-form")).toBeInTheDocument();
    expect(screen.getByTestId("info-icon")).toBeInTheDocument();
  });

  it("renders welcome message", () => {
    render(<ProfileApiKeyManager />);
    expect(screen.getByText(/witaj/i)).toBeInTheDocument();
  });

  it("passes hasApiKey prop to ApiKeyStatus", () => {
    render(<ProfileApiKeyManager />);
    // Check if the component was called
    expect(ApiKeyStatus).toHaveBeenCalled();
    // Check if the first argument contains hasApiKey=true
    expect(vi.mocked(ApiKeyStatus).mock.calls[0][0]).toHaveProperty("hasApiKey", true);
  });

  it("passes correct props to ApiKeyForm", () => {
    render(<ProfileApiKeyManager />);
    // Check if the component was called
    expect(ApiKeyForm).toHaveBeenCalled();
    // Check if the first argument contains required fields
    const props = vi.mocked(ApiKeyForm).mock.calls[0][0];
    expect(props).toHaveProperty("apiKey", "test-key");
    expect(props).toHaveProperty("hasApiKey", true);
    expect(props).toHaveProperty("loading", false);
    expect(props).toHaveProperty("onSave");
    expect(props).toHaveProperty("onDelete");
    expect(props).toHaveProperty("onInputChange");
  });
});
