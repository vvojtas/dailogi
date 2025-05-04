import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { register } from "@/dailogi-api/authentication/authentication";
import type { RegisterCommand } from "@/dailogi-api/model";
import { navigate } from "@/lib/client/navigate";
import { ROUTES } from "@/lib/config/routes";
import { DailogiError } from "@/lib/errors/DailogiError";
import { useHydration } from "@/lib/hooks/useHydration";

const registerSchema = z
  .object({
    name: z
      .string()
      .min(3, "Co najmniej 3 znaków użyjesz w nazwie swojej")
      .max(50, "Nie więcej niż 50 znaków stanowić będzie nazwa twa"),
    password: z
      .string()
      .min(8, "Długość hasła nie krótsza będzie niż 8 znaków")
      .regex(/[A-Z]/, "Wielką literę zawierać będzie hasło")
      .regex(/[0-9]/, "Cyfrę zawierać będzie hasło"),
    passwordConfirmation: z.string(),
  })
  .refine((data) => data.password === data.passwordConfirmation, {
    message: "Różnic między hasłami nie może być żadnych",
    path: ["passwordConfirmation"],
  });

type RegisterFormValues = z.infer<typeof registerSchema>;

/**
 * Function to handle API error responses and return appropriate Polish error messages
 */
function handleApiError(error: unknown, form: ReturnType<typeof useForm<RegisterFormValues>>): string | null {
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
  let errorMsg = "Wystąpił błąd podczas inicjalizacji konta";

  if (errorCode) {
    switch (errorCode) {
      case "RESOURCE_DUPLICATE":
        // Handle duplicate username - set form error instead of toast
        form.setError("name", {
          type: "manual",
          message: "Osobę taką już znamy - wybierz nowe imię",
        });
        return null; // Don't show toast
      case "VALIDATION_ERROR":
        errorMsg = "Formularz zawiera błędy weryfikacji";
        break;
      default:
        // Use message from errorData if available
        if (error instanceof DailogiError && error.errorData?.message) {
          errorMsg = error.errorData.message;
        }
    }
  } else if (error instanceof Error) {
    errorMsg = error.message;
  }

  return errorMsg;
}

export function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false);
  const isHydrated = useHydration();

  const form = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      name: "",
      password: "",
      passwordConfirmation: "",
    },
  });

  async function onSubmit(data: RegisterCommand) {
    try {
      setIsLoading(true);
      // Use the generated API client function
      await register(data);

      toast.success("Inicjalizacja konta pomyślna. Zapraszamy.");
      navigate(ROUTES.LOGIN);
    } catch (error: unknown) {
      console.error("Registration error:", error);

      const errorMsg = handleApiError(error, form);
      if (errorMsg) {
        toast.error(errorMsg);
      }
    } finally {
      setIsLoading(false);
    }
  }

  const isDisabled = isLoading || !isHydrated;

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
                <Input
                  {...field}
                  disabled={isDisabled}
                  placeholder="Podaj swoją tożsamość"
                  autoComplete="username"
                  data-testid="register-username-input"
                />
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
                  disabled={isDisabled}
                  placeholder="Podaj sekretną frazę"
                  autoComplete="new-password"
                  data-testid="register-password-input"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="passwordConfirmation"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Powtórz hasło</FormLabel>
              <FormControl>
                <Input
                  {...field}
                  type="password"
                  disabled={isDisabled}
                  placeholder="Powtórz sekretną frazę"
                  autoComplete="new-password"
                  data-testid="register-password-confirm-input"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" className="w-full" disabled={isDisabled} data-testid="register-submit-button">
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          Dokonaj inicjalizacji
        </Button>
      </form>
    </Form>
  );
}
