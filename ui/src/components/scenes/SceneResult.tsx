import { Card, CardContent } from "@/components/ui/card";
import { CharacterAvatar } from "@/components/characters/CharacterAvatar";

// This is a placeholder for future implementation of dialogue results
export function SceneResult() {
  // In the future, this will display actual dialogue events from the API
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
