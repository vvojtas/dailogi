import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import type { Llmdto } from "@/dailogi-api/model";
import type { CharacterDTO } from "@/dailogi-api/model/characterDTO";
import { createCharacter, updateCharacter } from "@/dailogi-api/characters/characters";
import { Button } from "@/components/ui/button";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { AvatarUploader } from "@/components/characters/AvatarUploader";
import { useState } from "react";
import type { AxiosError } from "axios";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Loader2, CircleSlash } from "lucide-react";
import { toast } from "sonner";

const characterFormSchema = z.object({
  name: z
    .string()
    .min(1, "Imię postaci musi zostać zapisane w rejestrze")
    .max(100, "Imię postaci jest zbyt długie dla rejestru (max 100 znaków)"),
  short_description: z.string().max(500, "Krótki opis nie jest krótki (max 500 znaków)"),
  description: z.string().max(5000, "Biografia postaci jest zbyt obszerna (max 5000 znaków)"),
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
      const characterData = {
        name: data.name,
        short_description: data.short_description,
        description: data.description,
        default_llm_id: data.default_llm_id ? parseInt(data.default_llm_id, 10) : undefined,
      };

      const response = initialData
        ? await updateCharacter(initialData.id, characterData)
        : await createCharacter(characterData);

      toast.success(initialData ? "Postać została odmieniona!" : "Postać została powołana do życia!");
      onSubmitSuccess(response.data);
    } catch (err) {
      const axiosError = err as AxiosError<{ message: string }>;
      const errorMsg =
        axiosError.response?.data?.message || "Nie udało się zapisać postaci w rejestrze... Spróbuj ponownie";
      toast.error(errorMsg);
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
