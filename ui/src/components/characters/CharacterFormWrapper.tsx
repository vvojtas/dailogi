import { useEffect, useState } from "react";
import { navigate } from "@/lib/client/navigate";
import { useLlms } from "@/lib/hooks/useLlms";
import { ROUTES, getCharacterDetailUrl } from "@/lib/config/routes";
import { CharacterForm } from "@/components/characters/CharacterForm";
import { getCharacter } from "@/dailogi-api/characters/characters";
import type { CharacterDTO } from "@/dailogi-api/model/characterDTO";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { toast } from "sonner";

interface CharacterFormWrapperProps {
  characterId?: number;
}

export function CharacterFormWrapper({ characterId }: CharacterFormWrapperProps) {
  const { llms, isLoading: isLoadingLlms, error: llmsError } = useLlms();
  const [character, setCharacter] = useState<CharacterDTO | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (characterId) {
      setIsLoading(true);
      getCharacter(characterId)
        .then((response) => {
          setCharacter(response.data);
          setError(null);
        })
        .catch((err) => {
          const errorMsg = err?.response?.data?.message || "Namierzanie postaci zakończone niepowodzeniem";
          setError(errorMsg);
          toast.error(errorMsg);
        })
        .finally(() => {
          setIsLoading(false);
        });
    }
  }, [characterId]);

  const handleSuccess = (savedCharacter: CharacterDTO) => {
    navigate(getCharacterDetailUrl(savedCharacter.id));
  };

  const handleCancel = () => {
    if (characterId) {
      navigate(getCharacterDetailUrl(characterId));
    } else {
      navigate(ROUTES.CHARACTERS);
    }
  };

  if (isLoadingLlms || (characterId && isLoading)) {
    return <LoadingSpinner />;
  }

  if (llmsError) {
    toast.error(llmsError);
    return <div className="flex justify-center items-center h-full">Spróbuj odświeżyć stronę</div>;
  }

  if (error) {
    return <div className="flex justify-center items-center h-full">Spróbuj odświeżyć stronę</div>;
  }

  if (characterId && !character) {
    toast.error("Postać zniknęła ze sceny życia...");
    return <div className="flex justify-center items-center h-full">Spróbuj odświeżyć stronę</div>;
  }

  return <CharacterForm llms={llms} initialData={character} onSubmitSuccess={handleSuccess} onCancel={handleCancel} />;
}
