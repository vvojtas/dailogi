import { useEffect } from "react";
import { useAuthStore } from "@/lib/stores/auth.store";
import { LogoutButton } from "@/components/auth/LogoutButton";
import { ROUTES } from "@/lib/config/routes";
import type { AuthState } from "@/lib/stores/auth.store";

export function HeaderNav() {
  // Subscribe only to the primitive value needed
  const isLoggedIn = useAuthStore((state: AuthState) => state.isLoggedIn);

  console.log(`[HeaderNav] Component rendered. isLoggedIn: ${isLoggedIn}`);

  useEffect(() => {
    // Log when the isLoggedIn value changes from React's perspective
    console.log(`[HeaderNav] useEffect triggered. isLoggedIn changed to: ${isLoggedIn}`);
  }, [isLoggedIn]); // Dependency array ensures this runs when isLoggedIn changes

  return (
    <nav className="flex items-center gap-4">
      {isLoggedIn ? (
        <LogoutButton />
      ) : (
        // Render Login and Register links when logged out
        <div className="flex items-center gap-4">
          <a href={ROUTES.LOGIN} className="text-sm font-medium hover:text-primary">
            Ujawnij się
          </a>
          <a
            href={ROUTES.REGISTER}
            className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
          >
            Dołącz
          </a>
        </div>
      )}
    </nav>
  );
}
