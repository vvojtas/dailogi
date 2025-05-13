import { useState, useEffect } from "react";
import type { CharacterDropdownDTO } from "@/dailogi-api/model/characterDropdownDTO";
import type { Llmdto } from "@/dailogi-api/model/llmdto";
import { newSceneFormSchema, type NewSceneFormData } from "@/lib/validation/sceneSchema";
import type { DialogueEvent } from "@/dailogi-api-custom/dialogues";
import type { CharacterConfigDto } from "@/dailogi-api/model/characterConfigDto";

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
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // SSE state
  const [dialogueEvents, setDialogueEvents] = useState<DialogueEvent[]>([]);
  const [abortController, setAbortController] = useState<AbortController | null>(null);

  // Cleanup fetch controller on unmount
  useEffect(() => {
    return () => {
      if (abortController) {
        abortController.abort();
      }
    };
  }, [abortController]);

  // Helper to parse SSE events
  const processSSEChunk = (chunk: string) => {
    // Znajdź wszystkie kompletne zdarzenia SSE (format: "data: {...}\n\n")
    const pattern = /data: ({.*?})\n\n/g;
    let match;

    // Iteruj po wszystkich dopasowaniach
    while ((match = pattern.exec(chunk)) !== null) {
      try {
        // Wyciągnij JSON ze zdarzenia
        const jsonData = match[1];
        const eventData = JSON.parse(jsonData);

        console.log("Parsed SSE event:", eventData);

        // Dodaj zdarzenie do stanu
        setDialogueEvents((prev) => [...prev, eventData]);
      } catch (error) {
        console.error("Error parsing SSE event JSON:", error, match[0]);
      }
    }
  };

  // Start scene generation
  const startScene = async (formData: NewSceneFormData) => {
    setPhase("loading");
    setIsLoading(true);
    setDialogueEvents([]);

    // Abort any existing request
    if (abortController) {
      abortController.abort();
    }

    // Create new abort controller
    const controller = new AbortController();
    setAbortController(controller);

    try {
      // Filter out unused character configs
      const validConfigs = formData.configs.filter(
        (config) => config.characterId !== undefined && config.llmId !== undefined
      );

      // Sprawdź, czy mamy wystarczającą liczbę postaci
      if (validConfigs.length < 2) {
        throw new Error("Wymagane są co najmniej dwie postacie");
      }

      // Sprawdź, czy mamy opis sceny
      if (!formData.description.trim()) {
        throw new Error("Opis sceny jest wymagany");
      }

      // Map to the format expected by the API
      const characterConfigs: CharacterConfigDto[] = validConfigs.map((config) => ({
        character_id: config.characterId || 0,
        llm_id: config.llmId || 0,
      }));

      const requestData = {
        scene_description: formData.description,
        character_configs: characterConfigs,
        length: 10, // Number of turns - can be configurable later
      };

      console.log("Starting scene with data:", requestData);

      // Użyj jednego żądania POST do uruchomienia strumienia
      const response = await fetch("/api/dialogues/stream", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "text/event-stream",
          "Cache-Control": "no-cache",
        },
        body: JSON.stringify(requestData),
        signal: controller.signal,
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error("API error:", response.status, errorText);
        throw new Error(`HTTP error! Status: ${response.status}, Details: ${errorText || "No details"}`);
      }

      if (!response.body) {
        throw new Error("Response body is null");
      }

      // Zmień fazę na result już teraz
      setPhase("result");

      // Czytaj i przetwarzaj strumień SSE
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";

      try {
        while (true) {
          const { done, value } = await reader.read();

          if (done) {
            // Przetwórz pozostałe dane w buforze, jeśli istnieją
            if (buffer.trim()) {
              processSSEChunk(buffer);
            }
            console.log("Stream complete");
            break;
          }

          // Dekoduj chunk i dodaj do bufora
          const chunk = decoder.decode(value, { stream: true });
          console.log("Received chunk:", chunk);
          buffer += chunk;

          // Przetwórz zdarzenia SSE
          processSSEChunk(buffer);

          // Usuń przetworzony tekst z bufora, ale zachowaj potencjalnie niekompletne zdarzenie
          const lastEventIndex = buffer.lastIndexOf("\n\n");
          if (lastEventIndex !== -1) {
            buffer = buffer.substring(lastEventIndex + 2);
          }
        }
      } catch (readError) {
        if (readError instanceof DOMException && readError.name === "AbortError") {
          console.log("Stream reading aborted");
        } else {
          console.error("Error reading from stream:", readError);
          setError("Błąd podczas odczytu strumienia dialogu");
          setPhase("config");
        }
      }
    } catch (err) {
      console.error("Error starting scene:", err);
      const errorMessage = err instanceof Error ? err.message : "Nie zagrało - nieznany błąd";
      setError(errorMessage);
      setPhase("config");
      throw err;
    } finally {
      setIsLoading(false);
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
    error,
    dialogueEvents, // Export events for display

    // Actions
    startScene,
  };
}

export { newSceneFormSchema, type NewSceneFormData };
