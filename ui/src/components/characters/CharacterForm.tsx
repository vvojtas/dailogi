import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import type { Llmdto } from "@/dailogi-api/model";
import type { CharacterDTO } from "@/dailogi-api/model/characterDTO";
import type { CreateCharacterCommand } from "@/dailogi-api/model/createCharacterCommand";
import type { UpdateCharacterCommand } from "@/dailogi-api/model/updateCharacterCommand";
import { createCharacter, updateCharacter } from "@/dailogi-api/characters/characters";
import { Button } from "@/components/ui/button";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { AvatarUploader } from "@/components/characters/AvatarUploader";
import { useState } from "react";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Loader2, CircleSlash } from "lucide-react";
import { toast } from "sonner";
import { DailogiError } from "@/lib/errors/DailogiError";

const characterFormSchema = z.object({
  name: z
    .string()
    .min(1, "Imię postaci musi zostać zapisane w rejestrze")
    .max(100, "Imię postaci jest zbyt długie dla rejestru (max 100 znaków)"),
  short_description: z
    .string()
    .min(1, "Krótki opis nie jest opisem (min 1 znak)")
    .max(500, "Krótki opis nie jest krótki (max 500 znaków)"),
  description: z
    .string()
    .min(1, "Biografia postaci nie jest obszerna (min 1 znak)")
    .max(5000, "Biografia postaci jest zbyt obszerna (max 5000 znaków)"),
  default_llm_id: z.string().optional(),
  avatar: z.custom<File | undefined>().optional(),
});

type CharacterFormData = z.infer<typeof characterFormSchema>;

interface CharacterFormProps {
  llms: Llmdto[];
  initialData?: CharacterDTO | null;
  onSubmitSuccess: (character: CharacterDTO) => void;
  onCancel: () => void;
}

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
  let errorDetails: Record<string, unknown> | undefined;

  if (error instanceof DailogiError) {
    errorCode = error.errorData?.code;
    errorDetails = error.errorData?.details as Record<string, unknown> | undefined;
  }

  // Default error message
  let errorMsg = "Nie udało się zapisać postaci w rejestrze... Spróbuj ponownie";

  if (errorCode) {
    switch (errorCode) {
      case "VALIDATION_ERROR":
        errorMsg = "Formularz zawiera błędy weryfikacji";
        // If we have field-specific errors, show the first one
        if (errorDetails) {
          const firstError = Object.values(errorDetails)[0];
          if (firstError && typeof firstError === "string") {
            errorMsg = `Błąd weryfikacji: ${firstError}`;
          }
        }
        break;
      case "RESOURCE_DUPLICATE":
        errorMsg = "Ta postać już widnieje w rejestrze";
        break;
      case "RESOURCE_NOT_FOUND":
        errorMsg = "Nie odnaleziono postaci w rejestrze";
        break;
      case "TYPE_MISMATCH":
        errorMsg = "Podano nieprawidłowy format danych";
        break;
      case "CHARACTER_LIMIT_EXCEEDED":
        errorMsg = "Osiągnięto limit postaci w Twoim zespole! Więcej nie można stworzyć.";
        break;
    }
  }

  return errorMsg;
}

/**
 * Converts a File to base64 encoding
 */
async function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => {
      if (typeof reader.result === "string") {
        // Remove the data URL prefix (e.g., "data:image/png;base64,")
        const base64 = reader.result.split(",")[1];
        resolve(base64);
      } else {
        reject(new Error("Failed to convert file to base64"));
      }
    };
    reader.onerror = (error) => reject(error);
  });
}

export function CharacterForm({ llms, initialData, onSubmitSuccess, onCancel }: CharacterFormProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const form = useForm<CharacterFormData>({
    resolver: zodResolver(characterFormSchema),
    defaultValues: {
      name: initialData?.name || "",
      short_description: initialData?.short_description || "",
      description: initialData?.description || "",
      default_llm_id: initialData?.default_llm_id?.toString() || undefined,
    },
  });

  const onSubmit = async (data: CharacterFormData) => {
    setIsSubmitting(true);

    try {
      if (initialData) {
        // Update existing character
        const updateData: UpdateCharacterCommand = {
          name: data.name,
          short_description: data.short_description,
          description: data.description,
          default_llm_id: data.default_llm_id ? parseInt(data.default_llm_id, 10) : undefined,
        };

        const response = await updateCharacter(initialData.id, updateData);
        toast.success("Postać została odmieniona!");
        onSubmitSuccess(response.data);
      } else {
        // Create new character with avatar
        const createData: CreateCharacterCommand = {
          name: data.name,
          short_description: data.short_description,
          description: data.description,
          default_llm_id: data.default_llm_id ? parseInt(data.default_llm_id, 10) : undefined,
        };

        // Handle avatar for new character
        if (data.avatar) {
          try {
            const base64Data = await fileToBase64(data.avatar);
            createData.avatar = {
              data: base64Data,
              content_type: data.avatar.type,
            };
          } catch (error) {
            console.error("Error while encoding avatar:", error);
            toast.error("Nie udało się przetworzyć awatara. Spróbuj ponownie.");
            setIsSubmitting(false);
            return;
          }
        }

        const response = await createCharacter(createData);
        toast.success("Postać została powołana do życia!");
        onSubmitSuccess(response.data);
      }
    } catch (err) {
      const errorMsg = handleApiError(err);
      if (errorMsg) {
        toast.error(errorMsg);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <Card>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)}>
            <CardHeader>
              <CardTitle>{initialData ? "Odmień postać" : "Powołaj nową postać"}</CardTitle>
            </CardHeader>

            <CardContent className="space-y-6">
              <div className="flex justify-center mb-6">
                <AvatarUploader
                  initialAvatarUrl={initialData?.avatar_url}
                  characterId={initialData?.id}
                  onAvatarChange={(file) => form.setValue("avatar", file || undefined)}
                />
              </div>

              <FormField
                control={form.control}
                name="name"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Imię postaci</FormLabel>
                    <FormControl>
                      <Input placeholder="Kogo przedstawiasz?" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="short_description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Krótki opis</FormLabel>
                    <FormControl>
                      <Input placeholder="Sama esencja jestestwa" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Biografia</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="Kim ona jest?"
                        className="min-h-[150px] scrollbar-thin scrollbar-thumb-secondary scrollbar-track-transparent"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="default_llm_id"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Model językowy</FormLabel>
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <FormControl>
                        <SelectTrigger className="min-w-[180px]!">
                          <SelectValue placeholder="Wybierz najstosowniejszy LLM" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="undefined">
                          <CircleSlash className="h-4 w-4 mr-2" /> Brak modelu
                        </SelectItem>
                        {llms.map((llm) => (
                          <SelectItem key={llm.id} value={llm.id.toString()}>
                            {llm.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </CardContent>

            <CardFooter className="flex justify-end space-x-4">
              <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
                {initialData ? "Opuść profil" : "Porzuć postać"}
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Postać pracuje nad sobą...
                  </>
                ) : initialData ? (
                  "Odmień postać"
                ) : (
                  "Powołaj do życia"
                )}
              </Button>
            </CardFooter>
          </form>
        </Form>
      </Card>
    </div>
  );
}
