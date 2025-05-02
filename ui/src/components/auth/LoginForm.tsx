import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { login } from "@/dailogi-api/authentication/authentication";
import type { LoginCommand } from "@/dailogi-api/model";
import { useAuthStore } from "@/lib/stores/auth.store";
import type { AuthState } from "@/lib/stores/auth.store";
import { ROUTES } from "@/lib/config/routes";
import { navigate } from "@/lib/client/navigate";
import { DailogiError } from "@/lib/errors/DailogiError";

const loginSchema = z.object({
  name: z.string().min(1, "Zdradź swoją tożsamość"),
  password: z.string().min(1, "Podaj sekretną frazę"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

/**
 * Function to handle API error responses and return appropriate Polish error messages
 */
function handleApiError(error: unknown): string | null {
  // If it's already a DailogiError and was displayed, don't show another toast
  if (error instanceof DailogiError && error.displayed) {
    return null;
  }

  // Get error code and message from response
  let errorCode: string | undefined;

  if (error instanceof DailogiError) {
    errorCode = error.errorData?.code;
  }

  // Default error message
  let errorMsg = "Nieokreślony błąd podczas procesu weryfikacji";

  if (errorCode) {
    switch (errorCode) {
      case "INVALID_CREDENTIALS":
        errorMsg = "Nie tak brzmiał sekret powierzony nam wcześniej";
        break;
      case "AUTHENTICATION_FAILED":
        errorMsg = "Weryfikacja tożsamości nie powiodła się";
        break;
      case "AUTH_ERROR":
        errorMsg = "Nieoczekiwany błąd podczas procesu weryfikacji";
        break;
      case "VALIDATION_ERROR":
        errorMsg = "Nie wszystkie pola są wypełnione poprawnie";
        break;
      default:
        // Use message from errorData if available
        if (error instanceof DailogiError && error.errorData?.message) {
          errorMsg = error.errorData.message;
        }
    }
  }

  return errorMsg;
}

export function LoginForm() {
  const [isLoading, setIsLoading] = useState(false);
  const setUser = useAuthStore((state: AuthState) => state.setUser);

  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      name: "",
      password: "",
    },
  });

  async function onSubmit(data: LoginCommand) {
    try {
      setIsLoading(true);
      console.log("[LoginForm] Attempting login...");
      const response = await login(data);

      console.log("[LoginForm] Login API success. User data:", response.data.user);
      setUser(response.data.user, response.data.expires_in);

      toast.success("Zalogowano pomyślnie");
      navigate(ROUTES.HOME);
    } catch (error) {
      console.error("[LoginForm] Login failed:", error);

      const errorMsg = handleApiError(error);
      if (errorMsg) {
        toast.error(errorMsg);
      }
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
        <FormField
          control={form.control}
          name="name"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Nazwa użytkownika</FormLabel>
              <FormControl>
                <Input {...field} disabled={isLoading} placeholder="Kim jesteś?" autoComplete="username" />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="password"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Hasło</FormLabel>
              <FormControl>
                <Input
                  {...field}
                  type="password"
                  disabled={isLoading}
                  placeholder="Czy znasz tajną frazę?"
                  autoComplete="current-password"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          Zaloguj się
        </Button>
      </form>
    </Form>
  );
}
