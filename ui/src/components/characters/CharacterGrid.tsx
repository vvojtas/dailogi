import type { CharacterDTO } from "@/dailogi-api/model";
import { CharacterCard } from "./CharacterCard";

interface CharacterGridProps {
  characters: CharacterDTO[];
  isLoggedIn: boolean;
  deletingCharacterIds: number[];
  onEdit: (characterId: number) => void;
  onDelete: (character: CharacterDTO) => void;
  onViewDetails: (characterId: number) => void;
}

export function CharacterGrid({
  characters,
  isLoggedIn,
  deletingCharacterIds,
  onEdit,
  onDelete,
  onViewDetails,
}: CharacterGridProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
      {characters.map((character) => (
        <CharacterCard
          key={character.id}
          character={character}
          isOwner={!character.is_global && isLoggedIn}
          isDeleting={deletingCharacterIds.includes(character.id)}
          onEdit={onEdit}
          onDelete={onDelete}
          onViewDetails={onViewDetails}
        />
      ))}
    </div>
  );
}
