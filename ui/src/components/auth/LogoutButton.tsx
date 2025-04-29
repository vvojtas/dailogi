import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useAuthStore } from "@/lib/stores/auth.store";
import type { AuthState } from "@/lib/stores/auth.store";

export function LogoutButton() {
  const [isLoading, setIsLoading] = useState(false);
  const logoutStore = useAuthStore((state: AuthState) => state.logout);

  const handleLogout = async () => {
    try {
      setIsLoading(true);
      console.log("[LogoutButton] Calling API /api/auth/logout");
      const response = await fetch("/api/auth/logout", {
        method: "POST",
      });

      if (!response.ok) {
        throw new Error("Los domaga się twej obecności - błąd uniemożliwił wylogowanie");
      }

      // Clear user data from global store BEFORE redirecting
      console.log("[LogoutButton] API success, clearing Zustand store");
      logoutStore();

      toast.success("Żegnaj - pomyślnie wylogowano");

      // Small delay to ensure storage sync before redirect
      console.log("[LogoutButton] About to redirect to home");
      // The slight timeout ensures storage operations complete before redirection
      setTimeout(() => {
        window.location.href = "/";
      }, 100);
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : "Los domaga się twej obecności - błąd uniemożliwił wylogowanie"
      );
      setIsLoading(false);
    }
  };

  return (
    <Button
      variant="ghost"
      onClick={handleLogout}
      disabled={isLoading}
      className="text-destructive hover:text-destructive/90"
    >
      {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
      Wyloguj
    </Button>
  );
}
