import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { ErrorMessage } from "@/components/ui/error-message";
import { Button } from "@/components/ui/button";
import { RefreshCw } from "lucide-react";

interface CharacterListStatusProps {
  isLoading: boolean;
  error: string | null;
  charactersCount: number;
  isLoggedIn: boolean;
  onRetry: () => void;
  onCreate: () => void;
}

export function CharacterListStatus({
  isLoading,
  error,
  charactersCount,
  isLoggedIn,
  onRetry,
  onCreate,
}: CharacterListStatusProps) {
  // Initial loading state
  if (isLoading && charactersCount === 0) {
    return <LoadingSpinner />;
  }

  // Error state when no characters are loaded
  if (error && charactersCount === 0) {
    return (
      <div className="text-center py-8">
        <ErrorMessage message={error} className="mb-4" />
        <Button onClick={onRetry} variant="outline">
          <RefreshCw className="mr-2 h-4 w-4" />
          Try Again
        </Button>
      </div>
    );
  }

  // Empty state after loading without errors
  if (!isLoading && !error && charactersCount === 0) {
    if (isLoggedIn) {
      return (
        <div className="text-center py-8">
          <p className="text-lg mb-4">You don&apos;t have any characters yet. Create your first one!</p>
          <Button onClick={onCreate}>Create New Character</Button>
        </div>
      );
    }
    return (
      <div className="text-center py-8">
        <p className="text-lg">No global characters available.</p>
      </div>
    );
  }

  // Loading overlay when characters are already present (e.g., pagination change)
  if (isLoading && charactersCount > 0) {
    return (
      <div className="fixed inset-0 bg-background/50 flex items-center justify-center z-50">
        <LoadingSpinner />
      </div>
    );
  }

  // If none of the above, render nothing (implies characters are loaded and displayed)
  return null;
}
