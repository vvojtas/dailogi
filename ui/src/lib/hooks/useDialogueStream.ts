import { useState, useEffect } from "react";
import { toast } from "sonner";
import type { DialogueEvent } from "@/dailogi-api-custom/dialogues";
import type { CharacterConfigDto } from "@/dailogi-api/model/characterConfigDto";

interface StartStreamParams {
  scene_description: string;
  character_configs: CharacterConfigDto[];
  length?: number;
}

export function useDialogueStream() {
  // Stream state
  const [dialogueEvents, setDialogueEvents] = useState<DialogueEvent[]>([]);
  const [abortController, setAbortController] = useState<AbortController | null>(null);
  const [isLoading, setIsLoading] = useState(false);

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
    console.log("Raw chunk to process:", chunk);

    // Find event pattern: event:type\ndata:{...}\n\n
    const events = chunk.split("\n\n").filter(Boolean);
    console.log("Split events:", events);

    for (const eventBlock of events) {
      // Skip empty blocks or fragments
      if (!eventBlock.includes("event:") || !eventBlock.includes("data:")) {
        console.log("Skipping incomplete event block:", eventBlock);
        continue;
      }

      try {
        // Extract event type and data
        const eventTypeMatch = /event:([^\n]+)/.exec(eventBlock);
        const dataMatch = /data:(\s*\{.+\})/s.exec(eventBlock);

        if (!eventTypeMatch || !dataMatch) {
          console.warn("Incomplete SSE event:", eventBlock);
          console.log("eventTypeMatch:", eventTypeMatch);
          console.log("dataMatch:", dataMatch);
          continue;
        }

        const eventType = eventTypeMatch[1];
        const jsonData = dataMatch[1];

        console.log(`Processing event type: ${eventType}, with data:`, jsonData);

        // Parse data as JSON
        const data = JSON.parse(jsonData);
        console.log("Parsed data:", data);

        // Create event object based on type
        let eventData: DialogueEvent;

        switch (eventType) {
          case "dialogue-start":
            eventData = {
              type: "dialogue-start",
              dialogue_id: data.dialogue_id,
              character_configs: data.character_configs || [],
              turn_count: data.turn_count,
            };
            break;

          case "character-start":
            eventData = {
              type: "character-start",
              character_config: data.character_config,
              id: data.id,
            };

            // Log the original data and transformed event
            console.log("Character start original:", data);
            console.log("Transformed to:", eventData);
            break;

          case "token":
            eventData = {
              type: "token",
              character_config: {
                character_id: data.character_id,
                llm_id: data.llm_id || 0, // Fallback if not provided
              },
              token: data.token,
              id: data.id,
            };

            console.log("Created token event:", eventData);
            break;

          case "character-complete":
            eventData = {
              type: "character-complete",
              character_id: data.character_id,
              token_count: data.token_count,
              id: data.id,
            };
            break;

          case "dialogue-complete":
            eventData = {
              type: "dialogue-complete",
              status: "completed",
              turn_count: data.turn_count,
              id: data.id,
            };
            break;

          case "error":
            eventData = {
              type: "error",
              message: data.message,
              recoverable: data.recoverable,
              id: data.id,
            };

            // Show toast for error events from the stream
            toast.error(`Błąd dialogu: ${data.message}`);
            break;

          default:
            console.warn("Unknown event type:", eventType, data);
            continue;
        }

        console.log("Parsed SSE event:", eventData);

        // Add event to state
        setDialogueEvents((prev) => [...prev, eventData]);
      } catch (error) {
        console.error("Error parsing SSE event:", error, eventBlock);
      }
    }
  };

  // Start dialogue stream
  const startStream = async (params: StartStreamParams) => {
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
      const requestData = {
        scene_description: params.scene_description,
        character_configs: params.character_configs,
        length: params.length || 10, // Default number of turns
      };

      console.log("Starting dialogue stream with data:", requestData);

      // Make POST request to start the stream
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
        const errorMessage = `HTTP error! Status: ${response.status}, Details: ${errorText || "No details"}`;
        toast.error(errorMessage);
        return false;
      }

      if (!response.body) {
        const errorMessage = "Response body is null";
        toast.error(errorMessage);
        return false;
      }

      // Read and process the SSE stream
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";

      try {
        while (true) {
          const { done, value } = await reader.read();

          if (done) {
            // Process remaining data in buffer if any
            if (buffer.trim()) {
              processSSEChunk(buffer);
            }
            console.log("Stream complete");
            break;
          }

          // Decode chunk and add to buffer
          const chunk = decoder.decode(value, { stream: true });
          console.log("Received chunk:", chunk);
          buffer += chunk;

          // Check if we have a full event before processing
          if (buffer.includes("\n\n")) {
            processSSEChunk(buffer);

            // Keep any potentially incomplete event in buffer
            const lastCompleteEventEndIndex = buffer.lastIndexOf("\n\n");
            if (lastCompleteEventEndIndex !== -1 && lastCompleteEventEndIndex < buffer.length - 2) {
              // Keep only the portion after the last complete event
              buffer = buffer.substring(lastCompleteEventEndIndex + 2);
              console.log("Remaining buffer after processing:", buffer);
            } else {
              // Clear buffer if we've processed everything
              buffer = "";
            }
          } else {
            console.log("Buffer doesn't contain complete events yet, waiting for more data");
          }
        }
      } catch (readError) {
        if (readError instanceof DOMException && readError.name === "AbortError") {
          console.log("Stream reading aborted");
        } else {
          console.error("Error reading from stream:", readError);
          const errorMessage = "Błąd podczas odczytu strumienia dialogu";
          toast.error(errorMessage);
          return false;
        }
      }

      return true;
    } catch (err: unknown) {
      console.error("Error in dialogue stream:", err);
      const errorMessage = err instanceof Error ? err.message : "Nieznany błąd podczas streamu dialogu";
      if (err instanceof Error && !err.name.includes("AbortError")) {
        toast.error(errorMessage);
      }
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  // Stop the stream
  const stopStream = () => {
    if (abortController) {
      abortController.abort();
      setAbortController(null);
    }
  };

  return {
    // State
    dialogueEvents,
    isLoading,

    // Actions
    startStream,
    stopStream,
    resetEvents: () => setDialogueEvents([]),
  };
}
