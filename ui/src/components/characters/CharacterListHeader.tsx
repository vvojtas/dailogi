import { Button } from "@/components/ui/button";
import { RefreshCw } from "lucide-react";

interface CharacterListHeaderProps {
  isLoggedIn: boolean;
  isRefreshing: boolean;
  onCreate: () => void;
  onRefresh: () => void;
}

export function CharacterListHeader({ isLoggedIn, isRefreshing, onCreate, onRefresh }: CharacterListHeaderProps) {
  return (
    <div className="flex justify-between items-center mb-8">
      <div>{isLoggedIn && <Button onClick={onCreate}>Create New Character</Button>}</div>
      <Button variant="outline" size="icon" onClick={onRefresh} disabled={isRefreshing} title="Refresh characters">
        <RefreshCw className={`h-4 w-4 ${isRefreshing ? "animate-spin" : ""}`} />
      </Button>
    </div>
  );
}
