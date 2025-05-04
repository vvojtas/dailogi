import { Button } from "@/components/ui/button";
import { RefreshCw } from "lucide-react";

interface CharacterListHeaderProps {
  isLoggedIn: boolean;
  isRefreshing: boolean;
  onCreate: () => void;
  onRefresh: () => void;
}

export function CharacterListHeader({
  isLoggedIn,
  isRefreshing,
  onCreate,
  onRefresh,
}: Readonly<CharacterListHeaderProps>) {
  return (
    <div className="flex justify-between items-center mb-8">
      <div>
        {isLoggedIn && (
          <Button onClick={onCreate} data-testid="create-character-btn">
            Powołaj nową postać
          </Button>
        )}
      </div>
      <Button variant="outline" size="icon" onClick={onRefresh} disabled={isRefreshing} title="Odśwież postacie">
        <RefreshCw className={`h-4 w-4 ${isRefreshing ? "animate-spin" : ""}`} />
      </Button>
    </div>
  );
}
