import { useState } from "react";
import type { CharacterDropdownDTO } from "@/dailogi-api/model/characterDropdownDTO";
import type { Llmdto } from "@/dailogi-api/model/llmdto";
import { newSceneFormSchema, type NewSceneFormData, type SelectedConfig } from "@/lib/validation/sceneSchema";

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

  // Form state
  const [description, setDescription] = useState("");
  const [configs, setConfigs] = useState<SelectedConfig>([
    { characterId: undefined, llmId: undefined },
    { characterId: undefined, llmId: undefined },
    { characterId: undefined, llmId: undefined },
  ]);

  // Phase management
  const [phase, setPhase] = useState<FormPhase>("config");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Function to manage the character selection
  const setCharacter = (index: number, characterId: number) => {
    setConfigs((prev) => {
      const newConfigs = [...prev];
      newConfigs[index] = { ...newConfigs[index], characterId };
      return newConfigs;
    });
  };

  // Function to manage the LLM selection
  const setLLM = (index: number, llmId: number) => {
    setConfigs((prev) => {
      const newConfigs = [...prev];
      newConfigs[index] = { ...newConfigs[index], llmId };
      return newConfigs;
    });
  };

  // Start scene generation
  const startScene = async () => {
    setPhase("loading");
    setIsLoading(true);

    // Placeholder for API call to start scene generation
    try {
      // TODO: Implement API call
      setPhase("result");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nie zagrało -  nieznany błąd");
      setPhase("config");
    } finally {
      setIsLoading(false);
    }
  };

  // Check if form is valid
  const isValid = () => {
    const hasValidDescription = description.trim().length > 0 && description.length <= 500;

    // Count filled slots (having both character and llm)
    const filledConfigsCount = configs.filter(
      (config) => config.characterId !== undefined && config.llmId !== undefined
    ).length;

    // Check if each character with selected characterId also has selected llmId
    const allConfiguredCharactersHaveLLM = configs.every((config) => {
      if (config.characterId === undefined) return true;
      return config.llmId !== undefined;
    });

    return hasValidDescription && filledConfigsCount >= 2 && allConfiguredCharactersHaveLLM;
  };

  return {
    // State
    characters,
    setCharacters,
    llms,
    setLlms,
    description,
    setDescription,
    configs,
    phase,
    isLoading,
    error,

    // Actions
    setCharacter,
    setLLM,
    startScene,
    isValid,
  };
}

export { newSceneFormSchema, type NewSceneFormData };
