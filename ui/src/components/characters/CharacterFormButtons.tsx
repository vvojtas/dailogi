import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";

interface CharacterFormButtonsProps {
  isSubmitting: boolean;
  isHydrated: boolean;
  isEditMode: boolean;
  onCancel: () => void;
}

export function CharacterFormButtons({ isSubmitting, isHydrated, isEditMode, onCancel }: CharacterFormButtonsProps) {
  const isDisabled = isSubmitting || !isHydrated;

  return (
    <div className="flex justify-end space-x-4">
      <Button
        type="button"
        variant="outline"
        onClick={onCancel}
        disabled={isDisabled}
        data-testid="character-cancel-btn"
      >
        {isEditMode ? "Opuść profil" : "Porzuć postać"}
      </Button>
      <Button type="submit" disabled={isDisabled} data-testid="character-submit-btn">
        {isSubmitting ? (
          <>
            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
            Postać pracuje nad sobą...
          </>
        ) : isEditMode ? (
          "Odmień postać"
        ) : (
          "Powołaj do życia"
        )}
      </Button>
    </div>
  );
}
