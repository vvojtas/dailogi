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
import axios from "axios";
import { navigate } from "@/lib/hooks/useNavigate";
import { ROUTES } from "@/lib/config/routes";

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

export function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false);

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
      let errorMessage = "Wystąpił błąd podczas inicjalizacji konta";
      if (axios.isAxiosError(error) && error.response) {
        if (error.response.status === 409) {
          form.setError("name", {
            type: "manual",
            message: "Osobę taką już znamy - wybierz nowe imię",
          });
          // Don't show a generic toast if it's a specific field error
          return;
        }
        errorMessage = error.response.data?.message || errorMessage;
      } else if (error instanceof Error) {
        errorMessage = error.message;
      }
      toast.error(errorMessage);
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
                <Input {...field} disabled={isLoading} placeholder="Podaj swoją tożsamość" autoComplete="username" />
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
                  placeholder="Podaj sekretną frazę"
                  autoComplete="new-password"
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
                  disabled={isLoading}
                  placeholder="Powtórz sekretną frazę"
                  autoComplete="new-password"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          Dokonaj inicjalizacji
        </Button>
      </form>
    </Form>
  );
}
