import { ThemeProvider as NextThemesProvider } from "next-themes";
import type { ReactNode } from "react";

type Theme = "light" | "dark" | "system";
type Attribute = "class" | "data-theme";

interface ThemeProviderProps {
  children: ReactNode;
  attribute?: Attribute;
  defaultTheme?: Theme;
  enableSystem?: boolean;
  storageKey?: string;
  disableTransitionOnChange?: boolean;
}

export function ThemeProvider({ children, ...props }: ThemeProviderProps) {
  return <NextThemesProvider {...props}>{children}</NextThemesProvider>;
}
