import { useEffect, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { CharacterAvatar } from "@/components/characters/CharacterAvatar";
import type {
  DialogueEvent,
  CharacterStartEvent,
  TokenEvent,
  CharacterCompleteEvent,
} from "@/dailogi-api-custom/dialogues";
import type { CharacterDropdownDTO } from "@/dailogi-api/model/characterDropdownDTO";

interface SceneResultProps {
  dialogueEvents?: DialogueEvent[];
  characters?: CharacterDropdownDTO[];
}

// Tymczasowy typ dla wiadomości
interface Message {
  characterId: number;
  characterName: string;
  avatarUrl?: string;
  content: string;
  isComplete: boolean;
}

// Przykładowy dialog do wyświetlenia, gdy nie ma prawdziwych danych
const placeholderDialogue = [
  {
    characterId: 1,
    characterName: "Postać 1",
    avatarUrl: undefined,
    content: "Odgrywam swoją rolę w tym dialogu, ale tak naprawdę moje słowa nie istnieją jeszcze w pełni.",
  },
  {
    characterId: 2,
    characterName: "Postać 2",
    avatarUrl: undefined,
    content:
      "Gdy wiarygodność to sztuka, a prawdą jest iluzja, kto wie, gdzie kończy się maska, a zaczyna prawdziwe oblicze?",
  },
  {
    characterId: 1,
    characterName: "Postać 1",
    avatarUrl: undefined,
    content:
      "Sztuczny dialog to tylko zapowiedź tego, co wkrótce stworzy inteligencja - cyfrowa dramaturgia tworzona w ciszy serwerów.",
  },
];

export function SceneResult({ dialogueEvents = [], characters = [] }: SceneResultProps) {
  const [messages, setMessages] = useState<Message[]>([]);

  // Przetwarzanie zdarzeń dialogowych
  useEffect(() => {
    if (!dialogueEvents.length) return;

    // Funkcja do przetwarzania zdarzenia
    const processEvent = (event: DialogueEvent) => {
      switch (event.type) {
        case "character-start": {
          const characterEvent = event as CharacterStartEvent;
          const characterId = characterEvent.character_config.character_id;
          const character = characters.find((c) => c.id === characterId);

          // Dodaj nową wiadomość dla rozpoczynającej się wypowiedzi postaci
          setMessages((prev) => [
            ...prev,
            {
              characterId,
              characterName: character?.name || `Postać ${characterId}`,
              avatarUrl: character?.avatar_url,
              content: "",
              isComplete: false,
            },
          ]);
          break;
        }

        case "token": {
          const tokenEvent = event as TokenEvent;
          const characterId = tokenEvent.character_config.character_id;

          // Dodaj token do ostatniej wiadomości od tej postaci
          setMessages((prev) => {
            const lastMessageIndex = [...prev]
              .reverse()
              .findIndex((msg) => msg.characterId === characterId && !msg.isComplete);

            if (lastMessageIndex === -1) return prev;

            // Konwertuj z odwróconego indeksu
            const actualIndex = prev.length - 1 - lastMessageIndex;
            const newMessages = [...prev];
            newMessages[actualIndex] = {
              ...newMessages[actualIndex],
              content: newMessages[actualIndex].content + tokenEvent.token,
            };

            return newMessages;
          });
          break;
        }

        case "character-complete": {
          const completeEvent = event as CharacterCompleteEvent;

          // Oznacz ostatnią wiadomość od tej postaci jako zakończoną
          setMessages((prev) => {
            const lastMessageIndex = [...prev]
              .reverse()
              .findIndex((msg) => msg.characterId === completeEvent.character_id && !msg.isComplete);

            if (lastMessageIndex === -1) return prev;

            // Konwertuj z odwróconego indeksu
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
    };

    // Przetwórz ostatnie zdarzenie
    processEvent(dialogueEvents[dialogueEvents.length - 1]);
  }, [dialogueEvents, characters]);

  // Wyświetlanie danych gdy nie ma zdarzeń lub wiadomości
  if (!dialogueEvents.length) {
    return (
      <div className="space-y-6 py-4">
        <h3 className="text-lg font-medium">Wystawiony dialog</h3>
        <div className="space-y-4">
          {placeholderDialogue.map((message, index) => (
            <Card key={index} className="overflow-hidden">
              <CardContent className="p-4">
                <div className="flex items-start gap-4">
                  <CharacterAvatar
                    hasAvatar={!!message.avatarUrl}
                    avatarUrl={message.avatarUrl}
                    characterName={message.characterName}
                    className="h-10 w-10"
                  />
                  <div className="flex-1">
                    <p className="font-semibold">{message.characterName}</p>
                    <p className="text-sm mt-1 text-muted-foreground">{message.content}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
        <p className="text-sm text-muted-foreground italic">
          Ten widok zostanie wkrótce ożywiony rzeczywistą konwersacją pomiędzy wybranymi postaciami.
        </p>
      </div>
    );
  }

  // Jeśli mamy zdarzenia, ale nie mamy jeszcze wiadomości
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
      {/* Główny panel wiadomości */}
      <div className="space-y-6 py-4 flex-1">
        <h3 className="text-lg font-medium">Wystawiony dialog</h3>

        <div className="space-y-4">
          {messages.map((message, index) => (
            <Card key={index} className="overflow-hidden">
              <CardContent className="p-4">
                <div className="flex items-start gap-4">
                  <CharacterAvatar
                    hasAvatar={!!message.avatarUrl}
                    avatarUrl={message.avatarUrl}
                    characterName={message.characterName}
                    className="h-10 w-10"
                  />
                  <div className="flex-1">
                    <p className="font-semibold">{message.characterName}</p>
                    <p className="text-sm mt-1 text-muted-foreground">{message.content}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      {/* Panel debugowania */}
      <div className="w-96 border-l p-4">
        <h3 className="text-lg font-medium mb-4">Debug - Zdarzenia SSE</h3>
        <div className="space-y-2 max-h-[600px] overflow-y-auto">
          {dialogueEvents.map((event, index) => (
            <div key={index} className="text-xs border p-2 rounded bg-muted">
              <div className="font-semibold">Typ: {event.type}</div>
              <pre className="mt-1 overflow-x-auto">{JSON.stringify(event, null, 2)}</pre>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
