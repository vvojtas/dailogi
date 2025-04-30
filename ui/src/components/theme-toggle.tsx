import { Moon, Sun } from "lucide-react";
import { useEffect, useState } from "react";
import { Switch } from "@/components/ui/switch";

export function ThemeToggle() {
  const [isDark, setIsDark] = useState(false);

  // Initialize from localStorage and detect system theme
  useEffect(() => {
    // Get saved theme
    const savedTheme = localStorage.getItem("theme") as "light" | "dark" | null;
    // Default to system preference if no saved theme
    if (!savedTheme) {
      const systemPrefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      setIsDark(systemPrefersDark);
    } else {
      setIsDark(savedTheme === "dark");
    }
  }, []);

  const toggleTheme = () => {
    const newIsDark = !isDark;
    setIsDark(newIsDark);

    // Save to localStorage
    localStorage.setItem("theme", newIsDark ? "dark" : "light");

    // Apply to document
    document.documentElement.classList.toggle("dark", newIsDark);
  };

  return (
    <div className="flex items-center gap-2">
      <Sun className="h-[1.2rem] w-[1.2rem] text-muted-foreground" />
      <Switch checked={isDark} onCheckedChange={toggleTheme} aria-label="Przełącz motyw" />
      <Moon className="h-[1.2rem] w-[1.2rem] text-muted-foreground" />
    </div>
  );
}
