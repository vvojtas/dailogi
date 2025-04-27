import { useCallback, useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import type { CharacterDTO } from "@/dailogi-api/model";
import { deleteCharacter, getCharacters } from "@/dailogi-api/characters/characters";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { ErrorMessage } from "@/components/ui/error-message";
import { CharacterCard } from "./CharacterCard";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
} from "@/components/ui/pagination";
import { toast } from "sonner";
import { useNavigate } from "@/lib/hooks/useNavigate";
import { RefreshCw } from "lucide-react";

interface CharacterListPageProps {
  isLoggedIn: boolean;
  pageSize?: number;
}

export default function CharacterListPage({ isLoggedIn, pageSize = 12 }: CharacterListPageProps) {
  // Navigation
  const navigate = useNavigate();

  // State
  const [characters, setCharacters] = useState<CharacterDTO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [deletingCharacterIds, setDeletingCharacterIds] = useState<number[]>([]);

  // Fetch characters
  const fetchCharacters = useCallback(
    async (isRefresh = false) => {
      try {
        if (isRefresh) {
          setIsRefreshing(true);
        } else {
          setIsLoading(true);
        }
        setError(null);
        const response = await getCharacters({
          page: currentPage,
          size: pageSize,
          includeGlobal: true,
        });
        const data = response.data;
        setCharacters(data.content);
        setTotalPages(data.total_pages);
        if (isRefresh) {
          toast.success("Characters refreshed successfully");
        }
      } catch (err) {
        const message = "Failed to load characters. Please try again later.";
        setError(message);
        toast.error(message);
        console.error("Error fetching characters:", err);
      } finally {
        setIsLoading(false);
        setIsRefreshing(false);
      }
    },
    [currentPage, pageSize]
  );

  useEffect(() => {
    fetchCharacters();
  }, [fetchCharacters]);

  // Action handlers
  const handleNavigateToCreate = () => {
    navigate("/characters/create");
  };

  const handleNavigateToEdit = (characterId: number) => {
    navigate(`/characters/${characterId}/edit`);
  };

  const handleViewDetails = (characterId: number) => {
    navigate(`/characters/${characterId}`);
  };

  const handleDeleteCharacter = async (character: CharacterDTO) => {
    try {
      setDeletingCharacterIds((prev) => [...prev, character.id]);
      await deleteCharacter(character.id);
      toast.success(`Character "${character.name}" has been deleted.`);
      // Optimistically remove the character from the list
      setCharacters((prev) => prev.filter((c) => c.id !== character.id));
      // If this was the last character on the page and not the first page,
      // go to the previous page
      if (characters.length === 1 && currentPage > 0) {
        setCurrentPage((prev) => prev - 1);
      } else {
        // Otherwise just refresh the current page
        await fetchCharacters(true);
      }
    } catch (err) {
      const message = "Failed to delete character. Please try again later.";
      toast.error(message);
      console.error("Error deleting character:", err);
    } finally {
      setDeletingCharacterIds((prev) => prev.filter((id) => id !== character.id));
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleRefresh = () => {
    fetchCharacters(true);
  };

  // Render loading state
  if (isLoading && !characters.length) {
    return <LoadingSpinner />;
  }

  // Render error state with retry button
  if (error && !characters.length) {
    return (
      <div className="text-center py-8">
        <ErrorMessage message={error} className="mb-4" />
        <Button onClick={handleRefresh} variant="outline">
          <RefreshCw className="mr-2 h-4 w-4" />
          Try Again
        </Button>
      </div>
    );
  }

  // Render empty state
  if (!isLoading && !error && characters.length === 0) {
    if (isLoggedIn) {
      return (
        <div className="text-center py-8">
          <p className="text-lg mb-4">You don&apos;t have any characters yet. Create your first one!</p>
          <Button onClick={handleNavigateToCreate}>Create New Character</Button>
        </div>
      );
    }
    return (
      <div className="text-center py-8">
        <p className="text-lg">No global characters available.</p>
      </div>
    );
  }

  return (
    <div>
      {/* Header with create and refresh buttons */}
      <div className="flex justify-between items-center mb-8">
        <div>{isLoggedIn && <Button onClick={handleNavigateToCreate}>Create New Character</Button>}</div>
        <Button
          variant="outline"
          size="icon"
          onClick={handleRefresh}
          disabled={isRefreshing}
          title="Refresh characters"
        >
          <RefreshCw className={`h-4 w-4 ${isRefreshing ? "animate-spin" : ""}`} />
        </Button>
      </div>

      {/* Loading overlay */}
      {isLoading && characters.length > 0 && (
        <div className="fixed inset-0 bg-background/50 flex items-center justify-center z-50">
          <LoadingSpinner />
        </div>
      )}

      {/* Character grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
        {characters.map((character) => (
          <CharacterCard
            key={character.id}
            character={character}
            isOwner={!character.is_global && isLoggedIn}
            isDeleting={deletingCharacterIds.includes(character.id)}
            onEdit={handleNavigateToEdit}
            onDelete={handleDeleteCharacter}
            onViewDetails={handleViewDetails}
          />
        ))}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationLink
                onClick={() => currentPage > 0 && handlePageChange(currentPage - 1)}
                aria-disabled={currentPage === 0}
                className={currentPage === 0 ? "pointer-events-none opacity-50" : ""}
              >
                Previous
              </PaginationLink>
            </PaginationItem>
            {[...Array(totalPages)].map((_, index) => (
              <PaginationItem key={index}>
                {Math.abs(currentPage - index) <= 2 ? (
                  <PaginationLink onClick={() => handlePageChange(index)} isActive={currentPage === index}>
                    {index + 1}
                  </PaginationLink>
                ) : Math.abs(currentPage - index) === 3 ? (
                  <PaginationEllipsis />
                ) : null}
              </PaginationItem>
            ))}
            <PaginationItem>
              <PaginationLink
                onClick={() => currentPage < totalPages - 1 && handlePageChange(currentPage + 1)}
                aria-disabled={currentPage === totalPages - 1}
                className={currentPage === totalPages - 1 ? "pointer-events-none opacity-50" : ""}
              >
                Next
              </PaginationLink>
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      )}
    </div>
  );
}
