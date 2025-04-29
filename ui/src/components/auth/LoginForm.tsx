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
import axios from "axios";
import { navigate } from "@/lib/hooks/useNavigate";

const loginSchema = z.object({
  name: z.string().min(1, "Zdradź swoją tożsamość"),
  password: z.string().min(1, "Podaj sekretną frazę"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

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
      // Update global store with user data
      console.log("[LoginForm] Calling setUser...");
      setUser(response.data.user);
      console.log("[LoginForm] setUser called.");

      toast.success("Zalogowano pomyślnie");
      console.log(`[LoginForm] Redirecting to ${ROUTES.HOME}...`);
      navigate(ROUTES.HOME);
      // Note: Code execution might stop here due to redirect
    } catch (error) {
      let errorMessage = "Nie tak brzmiał sekret powierzony nam wcześniej";
      if (axios.isAxiosError(error) && error.response) {
        errorMessage = error.response.data?.message || errorMessage;
      } else if (error instanceof Error) {
        errorMessage = error.message;
      }
      console.error("[LoginForm] Login failed:", errorMessage, error);
      toast.error(errorMessage);
    } finally {
      // This might not run if redirect happens in try block
      console.log("[LoginForm] Setting isLoading to false (finally block).");
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
