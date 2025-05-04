import { useState } from "react";
import { toast } from "sonner";
import { createCharacter, updateCharacter } from "@/dailogi-api/characters/characters";
import type { CharacterDTO } from "@/dailogi-api/model/characterDTO";
import type { CreateCharacterCommand } from "@/dailogi-api/model/createCharacterCommand";
import type { UpdateCharacterCommand } from "@/dailogi-api/model/updateCharacterCommand";
import { fileToBase64 } from "@/lib/utils/fileUtils";
import { handleCharacterCreateUpdateError } from "@/lib/utils/errorHandlers/characterErrors";

interface UseCharacterFormOptions {
  onSuccess: (character: CharacterDTO) => void;
}

export interface CharacterFormValues {
  name: string;
  short_description: string;
  description: string;
  default_llm_id?: string;
  avatar?: File;
}

export function useCharacterForm({ onSuccess }: UseCharacterFormOptions) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const createNewCharacter = async (data: CharacterFormValues) => {
    // Return early if already submitting
    if (isSubmitting) return;

    setIsSubmitting(true);

    try {
      const createData: CreateCharacterCommand = {
        name: data.name,
        short_description: data.short_description,
        description: data.description,
        default_llm_id: data.default_llm_id ? parseInt(data.default_llm_id, 10) : undefined,
      };

      // Handle avatar for new character
      if (data.avatar) {
        try {
          const base64Data = await fileToBase64(data.avatar);
          createData.avatar = {
            data: base64Data,
            content_type: data.avatar.type,
          };
        } catch (error) {
          console.error("Error while encoding avatar:", error);
          toast.error("Nie udało się przetworzyć awatara. Spróbuj ponownie.");
          setIsSubmitting(false);
          return;
        }
      }

      const response = await createCharacter(createData);
      toast.success("Postać została powołana do życia!");
      onSuccess(response.data);
    } catch (err) {
      const errorMsg = handleCharacterCreateUpdateError(err);
      if (errorMsg) {
        toast.error(errorMsg);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const updateExistingCharacter = async (characterId: number, data: CharacterFormValues) => {
    // Return early if already submitting
    if (isSubmitting) return;

    setIsSubmitting(true);

    try {
      const updateData: UpdateCharacterCommand = {
        name: data.name,
        short_description: data.short_description,
        description: data.description,
        default_llm_id: data.default_llm_id ? parseInt(data.default_llm_id, 10) : undefined,
      };

      const response = await updateCharacter(characterId, updateData);
      toast.success("Postać została odmieniona!");
      onSuccess(response.data);
    } catch (err) {
      const errorMsg = handleCharacterCreateUpdateError(err);
      if (errorMsg) {
        toast.error(errorMsg);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return {
    isSubmitting,
    createNewCharacter,
    updateExistingCharacter,
  };
}
