import { Button } from "@/components/ui/button";
import { Loader2, Play } from "lucide-react";

interface StartSceneButtonProps {
  onClick: () => void;
  disabled: boolean;
  isLoading?: boolean;
}

export function StartSceneButton({ onClick, disabled, isLoading = false }: Readonly<StartSceneButtonProps>) {
  return (
    <Button onClick={onClick} disabled={disabled} data-testid="start-scene-button" size="lg" className="px-6">
      {isLoading ? (
        <>
          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
          Trwa generowanie...
        </>
      ) : (
        <>
          <Play className="mr-2 h-4 w-4" />
          Rozpocznij dialog
        </>
      )}
    </Button>
  );
}
