import { Toaster } from "sonner";
import { useThemeStore } from "@/lib/stores/theme.store";

export function ToasterWithTheme() {
  const { theme } = useThemeStore();

  // Convert our theme format to sonner's expected format
  const sonnerTheme = theme === "dark" ? "dark" : "light";

  return <Toaster richColors closeButton position="top-center" theme={sonnerTheme} toastOptions={{}} />;
}
