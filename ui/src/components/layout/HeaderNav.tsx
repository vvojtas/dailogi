import { useAuthStore } from "@/lib/stores/auth.store";
import { LogoutButton } from "@/components/auth/LogoutButton";
import { ROUTES } from "@/lib/config/routes";
import type { AuthState } from "@/lib/stores/auth.store";
import { ThemeToggle } from "@/components/theme-toggle";

export function HeaderNav() {
  // Subscribe only to what's needed
  const isLoggedIn = useAuthStore((state: AuthState) => state.isLoggedIn);
  const userName = useAuthStore((state: AuthState) => state.user?.name);

  return (
    <div className="flex items-center justify-between w-full">
      <div className="flex items-center gap-6">
        <a href={ROUTES.CHARACTERS} className="text-sm font-medium hover:text-primary">
          Galeria Postaci
        </a>
        {isLoggedIn ? (
          <>
            <a href={ROUTES.SCENES} className="text-sm font-medium hover:text-primary">
              Historia Scen
            </a>
            <a
              href={ROUTES.SCENE_NEW}
              className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
            >
              Nowa Scena
            </a>
          </>
        ) : (
          <>
            <span
              className="text-sm font-medium text-muted-foreground cursor-not-allowed opacity-50"
              title="Ujawnij się, aby uzyskać dostęp"
            >
              Historia Scen
            </span>
            <span
              className="rounded-md bg-primary/40 px-4 py-2 text-sm font-medium text-primary-foreground cursor-not-allowed opacity-50"
              title="Ujawnij się, aby uzyskać dostęp"
            >
              Nowa Scena
            </span>
          </>
        )}
      </div>

      <div className="flex items-center gap-4">
        <div className="border-r pr-5">
          <ThemeToggle />
        </div>
        {isLoggedIn ? (
          <div className="flex items-center gap-4">
            Scenarzysta:{" "}
            <a href={ROUTES.PROFILE} className="text-sm font-medium hover:text-primary">
              <span className="italic">{userName}</span>
            </a>
            <LogoutButton />
          </div>
        ) : (
          <>
            <a href={ROUTES.LOGIN} className="text-sm font-medium hover:text-primary mr-4">
              Ujawnij się
            </a>
            <a
              href={ROUTES.REGISTER}
              className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
            >
              Dołącz
            </a>
          </>
        )}
      </div>
    </div>
  );
}
