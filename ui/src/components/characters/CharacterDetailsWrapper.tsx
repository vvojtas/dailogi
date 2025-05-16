import { useEffect, useState } from "react";
import { navigate } from "@/lib/client/navigate";
import { useLlms } from "@/lib/hooks/useLlms";
import { ROUTES, getCharacterEditUrl } from "@/lib/config/routes";
import { getCharacter, deleteCharacter } from "@/dailogi-api/characters/characters";
import type { CharacterDTO } from "@/dailogi-api/model/characterDTO";
import { CharacterDetails } from "@/components/characters/CharacterDetails";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { toast } from "sonner";
import { DailogiError } from "@/lib/errors/DailogiError";
import { handleCharacterDeleteError } from "@/lib/utils/errorHandlers/characterErrors";

interface CharacterDetailsWrapperProps {
  characterId: number;
}

export function CharacterDetailsWrapper({ characterId }: Readonly<CharacterDetailsWrapperProps>) {
  const { llms, isLoading: isLoadingLlms } = useLlms();
  const [character, setCharacter] = useState<CharacterDTO | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    setIsLoading(true);
    getCharacter(characterId)
      .then((response) => {
        setCharacter(response.data);
        setError(null);
      })
      .catch((err) => {
        // Skip if error was already displayed by global handler
        if (err instanceof DailogiError && err.displayed) {
          console.error("Error fetching character:", err);
          setError("Nie udało się zlokalizować postaci");
          return;
        }

        const errorMsg =
          err instanceof DailogiError
            ? err.errorData?.message || "Nie udało się zlokalizować postaci"
            : "Nie udało się zlokalizować postaci";

        setError(errorMsg);
        toast.error(errorMsg);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [characterId]);

  const handleEdit = () => {
    navigate(getCharacterEditUrl(characterId));
  };

  const handleDelete = async (character: CharacterDTO) => {
    setIsDeleting(true);
    try {
      await deleteCharacter(character.id);
      toast.success(`Pomyślnie zlikwidowano "${character.name}"`);
      navigate(ROUTES.CHARACTERS);
    } catch (err) {
      // Skip if error was already displayed by global handler
      if (err instanceof DailogiError && err.displayed) {
        console.error("Error deleting character:", err);
        setIsDeleting(false);
        return;
      }

      const errorMsg = handleCharacterDeleteError(err);
      if (errorMsg) {
        setError(errorMsg);
        toast.error(errorMsg);
      }
      setIsDeleting(false);
    }
  };

  if (isLoading || isLoadingLlms) {
    return <LoadingSpinner />;
  }

  if (error) {
    return <div className="flex justify-center items-center h-full">Spróbuj odświeżyć stronę</div>;
  }

  if (!character) {
    toast.error("Postać gdzieś zaginęła...");
    return <div className="flex justify-center items-center h-full">Spróbuj odświeżyć stronę</div>;
  }

  return (
    <CharacterDetails
      character={character}
      llms={llms}
      isDeleting={isDeleting}
      onEdit={handleEdit}
      onDelete={handleDelete}
    />
  );
}
