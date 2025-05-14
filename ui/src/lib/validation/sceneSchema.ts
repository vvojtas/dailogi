import { z } from "zod";

export type SelectedConfig = {
  characterId?: number;
  llmId?: number;
}[];

export interface NewSceneFormData {
  description: string;
  configs: SelectedConfig;
}

// Form validation schema
export const newSceneFormSchema = z.object({
  description: z
    .string()
    .min(1, "Okoliczności muszą zostać opisane")
    .max(1000, "Za dużo wylewności - opis nie może przekraczać 1000 znaków"),
  configs: z
    .array(
      z.object({
        characterId: z.number().optional(),
        llmId: z.number().optional(),
      })
    )
    .length(3) // Always exactly 3 slots
    .refine(
      (configs) => {
        // Check if we have at least 2 filled character+llm pairs
        const filledConfigs = configs.filter(
          (config) => config.characterId !== undefined && config.llmId !== undefined
        );
        return filledConfigs.length >= 2;
      },
      {
        message: "Do dialogu trzeba dwojga - wypełnij przynajmniej dwa sloty",
      }
    )
    .refine(
      (configs) => {
        // Check if each configured character has an LLM assigned
        return configs.every((config) => {
          if (config.characterId === undefined) return true;
          return config.llmId !== undefined;
        });
      },
      {
        message: "Każda wybrana postać musi być obsługiwana przez jakiś LLM",
      }
    ),
});
