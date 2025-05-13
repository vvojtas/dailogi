import { CharacterSlot } from "./CharacterSlot.tsx";
import type { CharacterOption, LLMOption } from "@/lib/hooks/useNewScene";

interface CharacterSelectionListProps {
  characters: CharacterOption[];
  llms: LLMOption[];
  disabled?: boolean;
}

export function CharacterSelectionList({ characters, llms, disabled = false }: Readonly<CharacterSelectionListProps>) {
  return (
    <div className="mt-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-medium">Postacie</h3>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {Array.from({ length: 3 }).map((_, index) => (
          <CharacterSlot key={index} index={index} characters={characters} llms={llms} disabled={disabled} />
        ))}
      </div>
    </div>
  );
}
