import { create } from "zustand";

export type Theme = "light" | "dark";

export interface ThemeState {
  theme: Theme;
  isDark: boolean;
  setTheme: (theme: Theme) => void;
  toggleTheme: () => void;
  initialize: () => void;
}

export const useThemeStore = create<ThemeState>((set, get) => ({
  theme: "light",
  isDark: false,

  setTheme: (theme: Theme) => {
    // Save to localStorage
    localStorage.setItem("theme", theme);

    // Apply to document
    document.documentElement.classList.toggle("dark", theme === "dark");

    // Update store
    set({ theme, isDark: theme === "dark" });
  },

  toggleTheme: () => {
    const currentTheme = get().theme;
    const newTheme = currentTheme === "light" ? "dark" : "light";
    get().setTheme(newTheme);
  },

  initialize: () => {
    // Get saved theme
    const savedTheme = localStorage.getItem("theme") as Theme | null;
    const systemPrefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;

    if (!savedTheme) {
      // Use system preference if no saved theme
      const initialTheme = systemPrefersDark ? "dark" : "light";
      get().setTheme(initialTheme);
    } else {
      // Use saved theme
      get().setTheme(savedTheme);
    }
  },
}));
