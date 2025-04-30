import { useEffect } from "react";

export function ThemeInit() {
  useEffect(() => {
    const applyTheme = () => {
      // Get saved theme from localStorage
      const savedTheme = localStorage.getItem("theme") || "light";
      const systemPrefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;

      // If theme not explicitly set, use system preference
      if (!savedTheme) {
        const shouldUseDark = systemPrefersDark;
        document.documentElement.classList.toggle("dark", shouldUseDark);
        localStorage.setItem("theme", shouldUseDark ? "dark" : "light");
      } else {
        // Apply the explicit theme choice
        const shouldUseDark = savedTheme === "dark";
        document.documentElement.classList.toggle("dark", shouldUseDark);
      }
    };

    // Apply theme immediately
    applyTheme();

    // Set up listener for storage changes to sync across tabs
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === "theme") {
        const shouldUseDark = e.newValue === "dark";
        document.documentElement.classList.toggle("dark", shouldUseDark);
      }
    };

    window.addEventListener("storage", handleStorageChange);

    // Cleanup
    return () => {
      window.removeEventListener("storage", handleStorageChange);
    };
  }, []);

  return null;
}
