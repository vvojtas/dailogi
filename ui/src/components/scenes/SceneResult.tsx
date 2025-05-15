import { useEffect, useState, useRef, useCallback } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { CharacterAvatar } from "@/components/characters/CharacterAvatar";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import type {
  DialogueEvent,
  CharacterStartEvent,
  TokenEvent,
  CharacterCompleteEvent,
} from "@/dailogi-api-custom/dialogues";
import type { CharacterDropdownDTO } from "@/dailogi-api/model/characterDropdownDTO";
import type { Llmdto } from "@/dailogi-api/model/llmdto";

interface SceneResultProps {
  dialogueEvents?: DialogueEvent[];
  characters?: CharacterDropdownDTO[];
  llms?: Llmdto[];
}

// Temporary type for messages
interface Message {
  characterId: number;
  llmId: number;
  characterName: string;
  avatarUrl?: string;
  content: string;
  isComplete: boolean;
}

export function SceneResult({ dialogueEvents = [], characters = [], llms = [] }: SceneResultProps) {
  const [messages, setMessages] = useState<Message[]>([]);
  const processedEventsRef = useRef<Set<string>>(new Set());
  const [lastProcessedIndex, setLastProcessedIndex] = useState<number>(-1);
  const viewportRef = useRef<HTMLDivElement>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Function to scroll to the bottom of the messages container
  const scrollToBottom = useCallback(() => {
    // Attempt to scroll the viewport directly
    if (viewportRef.current) {
      viewportRef.current.scrollTop = viewportRef.current.scrollHeight;
    }

    // Also try to scroll to the end marker element, as a fallback
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, []);

  // Apply scrolling after render and after messages update
  useEffect(() => {
    // Use a small timeout to ensure DOM is updated
    const timeoutId = setTimeout(() => {
      scrollToBottom();
    }, 50);

    return () => clearTimeout(timeoutId);
  }, [messages, scrollToBottom]);

  // Function to get LLM name by ID
  const getLlmName = useCallback(
    (llmId: number) => {
      const llm = llms.find((l) => l.id === llmId);
      return llm?.name || "Nieznany model";
    },
    [llms]
  );

  // Optimized function for processing events
  const processEvent = useCallback(
    (event: DialogueEvent) => {
      console.log(`Processing event of type: ${event.type}`);

      // Mark this event as processed if it has an ID
      if ("id" in event) {
        processedEventsRef.current.add(event.id);
      }

      switch (event.type) {
        case "character-start": {
          const characterEvent = event as CharacterStartEvent;
          const characterId = characterEvent.character_config.character_id;
          const llmId = characterEvent.character_config.llm_id || 0;
          const character = characters.find((c) => c.id === characterId);

          console.log(`Character start event for character ${characterId}`, character);

          // Add new message for character starting to speak
          setMessages((prev) => {
            const newMessages = [
              ...prev,
              {
                characterId,
                llmId,
                characterName: character?.name || `Character ${characterId}`,
                avatarUrl: character?.avatar_url,
                content: "",
                isComplete: false,
              },
            ];
            console.log("Updated messages after character start:", newMessages);
            return newMessages;
          });
          break;
        }

        case "token": {
          const tokenEvent = event as TokenEvent;
          const characterId = tokenEvent.character_config.character_id;

          console.log(`Token event for character ${characterId}: "${tokenEvent.token}"`);

          // Add token to the last message from this character
          setMessages((prev) => {
            const lastMessageIndex = [...prev]
              .reverse()
              .findIndex((msg) => msg.characterId === characterId && !msg.isComplete);

            if (lastMessageIndex === -1) {
              console.warn(`No incomplete message found for character ${characterId}`);
              return prev;
            }

            // Convert from reversed index
            const actualIndex = prev.length - 1 - lastMessageIndex;
            const newMessages = [...prev];
            newMessages[actualIndex] = {
              ...newMessages[actualIndex],
              content: newMessages[actualIndex].content + " " + tokenEvent.token,
            };

            console.log(`Updated message at index ${actualIndex} with token: "${tokenEvent.token}"`);
            return newMessages;
          });
          break;
        }

        case "character-complete": {
          const completeEvent = event as CharacterCompleteEvent;

          // Mark the last message from this character as complete
          setMessages((prev) => {
            const lastMessageIndex = [...prev]
              .reverse()
              .findIndex((msg) => msg.characterId === completeEvent.character_id && !msg.isComplete);

            if (lastMessageIndex === -1) return prev;

            // Convert from reversed index
            const actualIndex = prev.length - 1 - lastMessageIndex;
            const newMessages = [...prev];
            newMessages[actualIndex] = {
              ...newMessages[actualIndex],
              isComplete: true,
            };

            return newMessages;
          });
          break;
        }
      }
    },
    [characters]
  );

  // Process new events as soon as they're received
  useEffect(() => {
    if (!dialogueEvents.length || dialogueEvents.length <= lastProcessedIndex) return;

    // Process only new events, one by one
    const nextEvent = dialogueEvents[lastProcessedIndex + 1];
    if (nextEvent) {
      processEvent(nextEvent);
      setLastProcessedIndex(lastProcessedIndex + 1);
    }
  }, [dialogueEvents, lastProcessedIndex, processEvent]);

  // Process remaining events in batches
  useEffect(() => {
    if (!dialogueEvents.length || dialogueEvents.length <= lastProcessedIndex + 1) return;

    // Set timeout for processing next batch of events
    const timeoutId = setTimeout(() => {
      setLastProcessedIndex((prev) => Math.min(prev + 1, dialogueEvents.length - 1));
    }, 10); // Small delay ensures UI fluidity

    return () => clearTimeout(timeoutId);
  }, [dialogueEvents, lastProcessedIndex]);

  // If we have events but no messages yet
  if (dialogueEvents.length > 0 && messages.length === 0) {
    return (
      <div className="space-y-6 py-4">
        <h3 className="text-lg font-medium">Wystawiony dialog</h3>
        <p className="text-sm text-muted-foreground italic">Czekamy na rozpoczęcie rozmowy między postaciami...</p>
      </div>
    );
  }

  return (
    <div className="flex gap-4">
      <div className="space-y-6 py-4 flex-1">
        <h3 className="text-lg font-medium">Wystawiony dialog</h3>

        <ScrollArea className="h-[500px] pr-4" viewportRef={viewportRef}>
          <div className="space-y-4">
            {messages.map((message, index) => (
              <Card key={index} className="overflow-hidden">
                <CardContent className="p-4">
                  <div className="flex items-start gap-4">
                    <div className="flex flex-col items-center gap-1">
                      <CharacterAvatar
                        hasAvatar={!!message.avatarUrl}
                        avatarUrl={message.avatarUrl}
                        characterName={message.characterName}
                        className="h-10 w-10"
                      />
                    </div>
                    <div className="flex-1">
                      <p className="font-semibold">
                        {message.characterName}
                        {message.llmId > 0 && (
                          <Badge variant="secondary" className="text-xs px-2 py-0 ml-2">
                            {getLlmName(message.llmId)}
                          </Badge>
                        )}
                      </p>
                      <p className="text-sm mt-1 text-muted-foreground">
                        {message.content}
                        {!message.isComplete && (
                          <span className="animate-pulse inline-block h-4 w-2 ml-1 bg-muted-foreground rounded-sm"></span>
                        )}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
            {/* Element referencyjny na końcu listy wiadomości */}
            <div ref={messagesEndRef} className="h-1" />
          </div>
        </ScrollArea>
      </div>
    </div>
  );
}
