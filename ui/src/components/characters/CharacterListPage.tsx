import { useCallback, useEffect, useState } from "react";
import type { CharacterDTO } from "@/dailogi-api/model";
import { deleteCharacter, getCharacters } from "@/dailogi-api/characters/characters";
import { toast } from "sonner";
import { navigate } from "@/lib/hooks/useNavigate";
import { CharacterListHeader } from "@/components/characters/CharacterListHeader";
import { CharacterGrid } from "@/components/characters/CharacterGrid";
import { CharacterPagination } from "@/components/characters/CharacterPagination";
import { CharacterListStatus } from "@/components/characters/CharacterListStatus";
import { useAuthStore } from "@/lib/stores/auth.store";

interface CharacterListPageProps {
  pageSize?: number;
}

export default function CharacterListPage({ pageSize = 12 }: CharacterListPageProps) {
  // Auth state
  const isLoggedIn = useAuthStore((state) => state.isLoggedIn);

  // Navigation
  // const navigate = useNavigate();

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
          toast.success("Uaktualniono profile");
        }
      } catch (err) {
        const message = "Poszukiwania nie przyniosły rezultatu. Może szczęście uśmiechnie się do ciebie później.";
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

  const handleDeleteCharacter = useCallback(
    async (character: CharacterDTO) => {
      try {
        setDeletingCharacterIds((prev) => [...prev, character.id]);
        await deleteCharacter(character.id);
        toast.success(`Pomyślnie zlikwidowano "${character.name}"`);
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
        const message = "Niewiarygodne. Postać uniknęła likwidacji. Możesz ponowić próbę później.";
        toast.error(message);
        console.error("Error deleting character:", err);
      } finally {
        setDeletingCharacterIds((prev) => prev.filter((id) => id !== character.id));
      }
    },
    [characters, currentPage, fetchCharacters]
  );

  const handlePageChange = useCallback(
    (page: number) => {
      setCurrentPage(page);
    },
    [setCurrentPage]
  );

  const handleRefresh = useCallback(() => {
    fetchCharacters(true);
  }, [fetchCharacters]);

  return (
    <div>
      <CharacterListHeader
        isLoggedIn={isLoggedIn}
        isRefreshing={isRefreshing}
        onCreate={handleNavigateToCreate}
        onRefresh={handleRefresh}
      />

      <CharacterListStatus
        isLoading={isLoading}
        error={error}
        charactersCount={characters.length}
        isLoggedIn={isLoggedIn}
        onRetry={handleRefresh}
        onCreate={handleNavigateToCreate}
      />

      {!error && characters.length > 0 && (
        <CharacterGrid
          characters={characters}
          isLoggedIn={isLoggedIn}
          deletingCharacterIds={deletingCharacterIds}
          onEdit={handleNavigateToEdit}
          onDelete={handleDeleteCharacter}
          onViewDetails={handleViewDetails}
        />
      )}

      <CharacterPagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
    </div>
  );
}

// Action handlers
function handleNavigateToCreate() {
  navigate("/characters/create");
}

function handleNavigateToEdit(characterId: number) {
  navigate(`/characters/${characterId}/edit`);
}

function handleViewDetails(characterId: number) {
  navigate(`/characters/${characterId}`);
}
