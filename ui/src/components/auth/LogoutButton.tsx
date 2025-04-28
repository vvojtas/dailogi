import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";

export function LogoutButton() {
  const [isLoading, setIsLoading] = useState(false);

  const handleLogout = async () => {
    try {
      setIsLoading(true);
      const response = await fetch("/api/auth/logout", {
        method: "POST",
      });

      if (!response.ok) {
        throw new Error("Los domaga się twej obecności - błąd uniemożliwił wylogowanie");
      }

      // Clear user data from global store
      // store.clearUser();

      toast.success("Żegnaj - pomyślnie wylogowano");
      window.location.href = "/";
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : "Los domaga się twej obecności - błąd uniemożliwił wylogowanie"
      );
    } finally {
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
