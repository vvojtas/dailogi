import { useState } from "react";
import { toast } from "sonner";
import { ZodError } from "zod";
import type { CharacterDropdownDTO } from "@/dailogi-api/model/characterDropdownDTO";
import type { Llmdto } from "@/dailogi-api/model/llmdto";
import { newSceneFormSchema, type NewSceneFormData } from "@/lib/validation/sceneSchema";
import type { CharacterConfigDto } from "@/dailogi-api/model/characterConfigDto";
import { useDialogueStream } from "./useDialogueStream";

// Define types for the form
export interface LLMOption {
  id: number;
  name: string;
}

// Phase types for the form
export type FormPhase = "config" | "loading" | "result";

export function useNewScene() {
  // State for characters and LLMs
  const [characters, setCharacters] = useState<CharacterDropdownDTO[]>([]);
  const [llms, setLlms] = useState<Llmdto[]>([]);

  // Phase management
  const [phase, setPhase] = useState<FormPhase>("config");

  // Use the dialogue stream hook
  const { dialogueEvents, isLoading, startStream } = useDialogueStream();

  // Start scene generation
  const startScene = async (formData: NewSceneFormData) => {
    setPhase("loading");

    try {
      // Validate form data using Zod schema
      const validatedData = newSceneFormSchema.parse(formData);

      // Filter out unused character configs
      const validConfigs = validatedData.configs.filter(
        (config) => config.characterId !== undefined && config.llmId !== undefined
      );

      // Map to the format expected by the API
      const characterConfigs: CharacterConfigDto[] = validConfigs.map((config) => ({
        character_id: config.characterId || 0,
        llm_id: config.llmId || 0,
      }));

      // Start the stream using the extracted hook
      const success = await startStream({
        scene_description: validatedData.description,
        character_configs: characterConfigs,
        length: 5, // Number of turns - can be configurable later
      });

      // Change phase to result only if successful
      if (success) {
        setPhase("result");
      } else {
        setPhase("config");
      }
    } catch (error: unknown) {
      console.error("Error starting scene:", error);

      // Handle ZodErrors from form validation
      if (error instanceof ZodError) {
        const errorMessage = error.errors?.[0]?.message || "Nieprawidłowe dane formularza";
        toast.error(errorMessage);
      } else {
        // Handle other errors
        const errorMessage = error instanceof Error ? error.message : "Nie zagrało - nieznany błąd";
        toast.error(errorMessage);
      }

      // Revert to config phase
      setPhase("config");
    }
  };

  return {
    // State
    characters,
    setCharacters,
    llms,
    setLlms,
    phase,
    isLoading,
    hasError: false,
    dialogueEvents, // From dialogue stream hook

    // Actions
    startScene,
  };
}

export { newSceneFormSchema, type NewSceneFormData };
