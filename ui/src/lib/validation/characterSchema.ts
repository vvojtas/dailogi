import { z } from "zod";

export const characterFormSchema = z.object({
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

export type CharacterFormData = z.infer<typeof characterFormSchema>;
