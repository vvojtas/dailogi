import { Moon, Sun } from "lucide-react";
import { Switch } from "@/components/ui/switch";
import { useThemeStore } from "@/lib/stores/theme.store";

export function ThemeToggle() {
  const { isDark, toggleTheme } = useThemeStore();

  return (
    <div className="flex items-center gap-2">
      <Sun className="h-[1.2rem] w-[1.2rem] text-muted-foreground" />
      <Switch checked={isDark} onCheckedChange={toggleTheme} aria-label="Przełącz motyw" />
      <Moon className="h-[1.2rem] w-[1.2rem] text-muted-foreground" />
    </div>
  );
}
