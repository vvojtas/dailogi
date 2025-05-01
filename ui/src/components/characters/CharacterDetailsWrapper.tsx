import { useEffect, useState } from "react";
import { navigate } from "@/lib/client/navigate";
import { useLlms } from "@/lib/hooks/useLlms";
import { ROUTES, getCharacterEditUrl } from "@/lib/config/routes";
import { getCharacter, deleteCharacter } from "@/dailogi-api/characters/characters";
import type { CharacterDTO } from "@/dailogi-api/model/characterDTO";
import { CharacterDetails } from "@/components/characters/CharacterDetails";
import type { AxiosError } from "axios";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { toast } from "sonner";

interface CharacterDetailsWrapperProps {
  characterId: number;
}

export function CharacterDetailsWrapper({ characterId }: CharacterDetailsWrapperProps) {
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
        const axiosError = err as AxiosError<{ message: string }>;
        const errorMsg = axiosError.response?.data?.message || "Nie udało się zlokalizować postaci";
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
      const axiosError = err as AxiosError<{ message: string }>;
      const errorMsg = axiosError.response?.data?.message || "Nie udało się zlikwidować postaci";
      setError(errorMsg);
      toast.error(errorMsg);
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
