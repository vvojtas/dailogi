import { CharacterSlot } from "./CharacterSlot.tsx";
import type { CharacterDropdownDTO } from "@/dailogi-api/model/characterDropdownDTO";
import type { Llmdto } from "@/dailogi-api/model/llmdto";
import type { FormPhase } from "@/lib/hooks/useNewScene";
import { useFormContext } from "react-hook-form";
import type { NewSceneFormData } from "@/lib/hooks/useNewScene";

interface CharacterSelectionListProps {
  characters: CharacterDropdownDTO[];
  llms: Llmdto[];
  disabled?: boolean;
  phase?: FormPhase;
}

export function CharacterSelectionList({
  characters,
  llms,
  disabled = false,
  phase = "config",
}: Readonly<CharacterSelectionListProps>) {
  const form = useFormContext<NewSceneFormData>();
  const isDisplayPhase = phase === "loading" || phase === "result";

  // In display phase, only show filled character slots
  const configs = form.watch("configs");
  const slotsToShow = isDisplayPhase
    ? configs.map((config, index) => ({ config, index })).filter((item) => item.config.characterId !== undefined)
    : Array.from({ length: 3 }).map((_, index) => ({ index }));

  // Determine grid class based on number of slots
  const gridClass =
    slotsToShow.length === 2
      ? "grid grid-cols-1 md:grid-cols-2 md:gap-8 gap-4 md:mx-auto md:max-w-2xl"
      : "grid grid-cols-1 md:grid-cols-3 gap-4";

  return (
    <div className="mt-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-medium">Postacie</h3>
      </div>

      <div className={gridClass}>
        {slotsToShow.map((item) => (
          <CharacterSlot
            key={item.index}
            index={item.index}
            characters={characters}
            llms={llms}
            disabled={disabled}
            phase={phase}
          />
        ))}
      </div>
    </div>
  );
}
