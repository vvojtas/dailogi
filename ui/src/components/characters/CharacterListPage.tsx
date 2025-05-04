import { useCallback } from "react";
import { navigate } from "@/lib/client/navigate";
import { CharacterListHeader } from "@/components/characters/CharacterListHeader";
import { CharacterGrid } from "@/components/characters/CharacterGrid";
import { CharacterPagination } from "@/components/characters/CharacterPagination";
import { CharacterListStatus } from "@/components/characters/CharacterListStatus";
import { useAuthStore } from "@/lib/stores/auth.store";
import { ROUTES, getCharacterDetailUrl, getCharacterEditUrl } from "@/lib/config/routes";
import { useCharacters } from "@/lib/hooks/useCharacters";

interface CharacterListPageProps {
  pageSize?: number;
}

export default function CharacterListPage({ pageSize = 12 }: Readonly<CharacterListPageProps>) {
  // Auth state
  const isLoggedIn = useAuthStore((state) => state.getIsLoggedIn());

  // Use characters hook
  const {
    characters,
    isLoading,
    isRefreshing,
    error,
    currentPage,
    totalPages,
    deletingCharacterIds,
    handleDeleteCharacter,
    handlePageChange,
    handleRefresh,
  } = useCharacters({ pageSize });

  // Navigation handlers
  const handleNavigateToCreate = useCallback(() => {
    navigate(ROUTES.CHARACTER_CREATE);
  }, []);

  const handleNavigateToEdit = useCallback((characterId: number) => {
    navigate(getCharacterEditUrl(characterId));
  }, []);

  const handleViewDetails = useCallback((characterId: number) => {
    navigate(getCharacterDetailUrl(characterId));
  }, []);

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
