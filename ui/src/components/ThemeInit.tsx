import { useEffect } from "react";
import { useThemeStore } from "@/lib/stores/theme.store";

export function ThemeInit() {
  const { initialize } = useThemeStore();

  useEffect(() => {
    // Initialize theme from localStorage or system preference
    initialize();

    // Set up listener for storage changes to sync across tabs
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === "theme") {
        const newTheme = e.newValue as "light" | "dark";
        if (newTheme) {
          useThemeStore.getState().setTheme(newTheme);
        }
      }
    };

    window.addEventListener("storage", handleStorageChange);

    // Cleanup
    return () => {
      window.removeEventListener("storage", handleStorageChange);
    };
  }, [initialize]);

  return null;
}
