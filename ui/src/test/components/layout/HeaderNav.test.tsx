import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { HeaderNav } from "@/components/layout/HeaderNav";
import { useAuthStore } from "@/lib/stores/auth.store";
import { useThemeStore } from "@/lib/stores/theme.store";
import { ROUTES } from "@/lib/config/routes";
import type { UserDto } from "@/dailogi-api/model";

// Custom hook to mock useAuthStore with specific login state and user data
const mockUseAuthStore = (isLoggedIn: boolean, user: UserDto | null) => {
  // Ignore type errors as we're mocking behavior, not structure
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  vi.mocked(useAuthStore).mockImplementation((selector: any) => {
    // Emulate the original selector function behavior
    if (typeof selector === "function") {
      const mockState = {
        getIsLoggedIn: () => isLoggedIn,
        getUser: () => user,
      };
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      return selector(mockState as any);
    }
    return undefined;
  });
};

// Mock modules
vi.mock("@/lib/stores/auth.store");
vi.mock("@/lib/stores/theme.store");

vi.mock("@/components/auth/LogoutButton", () => ({
  LogoutButton: () => <button data-testid="logout-button">Odejdź</button>,
}));

vi.mock("@/components/theme-toggle", () => ({
  ThemeToggle: () => <div data-testid="theme-toggle">Przełącznik motywu</div>,
}));

describe("HeaderNav Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();

    // Mock the theme toggle store
    vi.mocked(useThemeStore).mockReturnValue({
      theme: "light",
      isDark: false,
      setTheme: vi.fn(),
      toggleTheme: vi.fn(),
      initialize: vi.fn(),
    });
  });

  it("renders correctly for not logged in user", () => {
    // Set up mocks for logged out user
    mockUseAuthStore(false, null);

    render(<HeaderNav />);

    // Check if elements for logged out users are displayed
    expect(screen.getByText("Galeria Postaci")).toBeInTheDocument();
    expect(screen.getByText("Historia Scen")).toBeInTheDocument();
    expect(screen.getByText("Nowa Scena")).toBeInTheDocument();

    // Check if login/register buttons are visible
    expect(screen.getByText("Ujawnij się")).toBeInTheDocument();
    expect(screen.getByText("Dołącz")).toBeInTheDocument();

    // Check if theme toggle is visible
    expect(screen.getByTestId("theme-toggle")).toBeInTheDocument();

    // Check if buttons for logged out users are disabled
    expect(screen.getByText("Historia Scen")).toHaveClass("cursor-not-allowed");
    expect(screen.getByText("Nowa Scena")).toHaveClass("cursor-not-allowed");
  });

  it("renders correctly for logged in user", () => {
    // Set up mocks for logged in user
    const testUser: UserDto = { id: 1, name: "TestUser" };
    mockUseAuthStore(true, testUser);

    render(<HeaderNav />);

    // Check if elements for logged in users are displayed
    expect(screen.getByText("Galeria Postaci")).toBeInTheDocument();

    // Check if buttons for logged in users are active (links instead of spans)
    const historyLink = screen.getByText("Historia Scen");
    expect(historyLink.tagName.toLowerCase()).toBe("a");
    expect(historyLink).toHaveAttribute("href", ROUTES.SCENES);

    const newSceneLink = screen.getByText("Nowa Scena");
    expect(newSceneLink.tagName.toLowerCase()).toBe("a");
    expect(newSceneLink).toHaveAttribute("href", ROUTES.SCENE_NEW);

    // Check if user information is displayed
    expect(screen.getByText("Scenarzysta:")).toBeInTheDocument();

    // Check if username is displayed
    const userNameElement = screen.getByText("TestUser");
    expect(userNameElement).toBeInTheDocument();
    expect(userNameElement.tagName.toLowerCase()).toBe("span");
    expect(userNameElement).toHaveClass("italic");

    // Check if logout button is visible
    expect(screen.getByTestId("logout-button")).toBeInTheDocument();
  });

  it("has correct navigation links", () => {
    // Set up mocks for logged out user
    mockUseAuthStore(false, null);

    render(<HeaderNav />);

    // Check if links have correct URLs
    const charactersLink = screen.getByText("Galeria Postaci");
    expect(charactersLink).toHaveAttribute("href", ROUTES.CHARACTERS);

    const loginLink = screen.getByText("Ujawnij się");
    expect(loginLink).toHaveAttribute("href", ROUTES.LOGIN);

    const registerLink = screen.getByText("Dołącz");
    expect(registerLink).toHaveAttribute("href", ROUTES.REGISTER);
  });

  it("displays user profile link when logged in", () => {
    // Set up mocks for logged in user
    const testUser: UserDto = { id: 1, name: "TestUser" };
    mockUseAuthStore(true, testUser);

    render(<HeaderNav />);

    // Check if username is displayed
    const userNameElement = screen.getByText("TestUser");
    expect(userNameElement).toBeInTheDocument();

    // Check if profile link is correct
    const profileLink = userNameElement.closest("a");
    expect(profileLink).toHaveAttribute("href", ROUTES.PROFILE);
  });

  it("shows theme toggle component", () => {
    // Set up mocks for logged out user
    mockUseAuthStore(false, null);

    render(<HeaderNav />);

    // Check if theme toggle component is rendered
    expect(screen.getByTestId("theme-toggle")).toBeInTheDocument();
  });
});
