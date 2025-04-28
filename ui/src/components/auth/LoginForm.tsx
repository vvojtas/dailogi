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
import axios from "axios";

const loginSchema = z.object({
  name: z.string().min(1, "Zdradź swoją tożsamość"),
  password: z.string().min(1, "Podaj sekretną frazę"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function LoginForm() {
  const [isLoading, setIsLoading] = useState(false);

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
      // Use the generated API client function
      const response = await login(data);

      // Handle successful login - response.data contains JwtResponseDTO
      // The actual JWT token is handled via HttpOnly cookie by the Astro API endpoint
      // We might still need user data from the response if returned, for the store

      // Update global store with user data if available
      // store.setUser(response.data.user); // Assuming JwtResponseDTO contains user

      toast.success("Zalogowano pomyślnie");
      window.location.href = "/dashboard";
    } catch (error: unknown) {
      let errorMessage = "Nie tak brzmiał sekret powierzony nam wcześniej";
      if (axios.isAxiosError(error) && error.response) {
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
