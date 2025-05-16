import { useState, useCallback, useEffect } from "react";
import type { CharacterDTO } from "@/dailogi-api/model";
import { deleteCharacter, getCharacters } from "@/dailogi-api/characters/characters";
import { toast } from "sonner";
import { DailogiError } from "@/lib/errors/DailogiError";
import { handleCharacterDeleteError } from "../utils/errorHandlers/characterErrors";

interface UseCharactersOptions {
  pageSize?: number;
}

interface CharactersState {
  characters: CharacterDTO[];
  isLoading: boolean;
  isRefreshing: boolean;
  error: string | null;
  currentPage: number;
  totalPages: number;
  deletingCharacterIds: number[];
}

/**
 * Hook managing state, pagination and operations on the character list.
 *
 * Provides:
 * - fetching characters from API with loading state and error handling
 * - deleting characters with proper error handling and notifications
 * - pagination management
 * - list refreshing
 *
 * @param options - Configuration options such as page size
 * @returns Character state and helper functions for data management
 */
export function useCharacters({ pageSize = 12 }: UseCharactersOptions = {}) {
  const [state, setState] = useState<CharactersState>({
    characters: [],
    isLoading: true,
    isRefreshing: false,
    error: null,
    currentPage: 0,
    totalPages: 0,
    deletingCharacterIds: [],
  });

  const fetchCharacters = useCallback(
    async (isRefresh = false) => {
      try {
        setState((prev) => ({
          ...prev,
          isLoading: !isRefresh && prev.isLoading,
          isRefreshing: isRefresh,
          error: null,
        }));

        const response = await getCharacters({
          page: state.currentPage,
          size: pageSize,
          includeGlobal: true,
        });

        const data = response.data;
        setState((prev) => ({
          ...prev,
          characters: data.content,
          totalPages: data.total_pages,
        }));

        if (isRefresh) {
          toast.success("Uaktualniono profile");
        }
      } catch (err) {
        // Skip showing error message if it was already displayed
        if (err instanceof DailogiError && err.displayed) {
          console.error("Error fetching characters:", err);
          return;
        }

        const message = "Poszukiwania nie przyniosły rezultatu. Może szczęście uśmiechnie się do ciebie później.";
        setState((prev) => ({ ...prev, error: message }));
        toast.error(message);
        console.error("Error fetching characters:", err);
      } finally {
        setState((prev) => ({
          ...prev,
          isLoading: false,
          isRefreshing: false,
        }));
      }
    },
    [state.currentPage, pageSize]
  );

  const handleDeleteCharacter = useCallback(
    async (character: CharacterDTO) => {
      try {
        setState((prev) => ({
          ...prev,
          deletingCharacterIds: [...prev.deletingCharacterIds, character.id],
        }));

        await deleteCharacter(character.id);
        toast.success(`Pomyślnie zlikwidowano "${character.name}"`);

        // Optimistically remove the character from the list
        setState((prev) => ({
          ...prev,
          characters: prev.characters.filter((c) => c.id !== character.id),
        }));

        // If this was the last character on the page and not the first page,
        // go to the previous page
        if (state.characters.length === 1 && state.currentPage > 0) {
          setState((prev) => ({
            ...prev,
            currentPage: prev.currentPage - 1,
          }));
        } else {
          // Otherwise just refresh the current page
          await fetchCharacters(true);
        }
      } catch (err) {
        // Skip showing error message if it was already displayed
        if (err instanceof DailogiError && err.displayed) {
          console.error("Error deleting character:", err);
          return;
        }

        const message = handleCharacterDeleteError(err);
        if (message) {
          toast.error(message);
        }
        console.error("Error deleting character:", err);
      } finally {
        setState((prev) => ({
          ...prev,
          deletingCharacterIds: prev.deletingCharacterIds.filter((id) => id !== character.id),
        }));
      }
    },
    [state.characters, state.currentPage, fetchCharacters]
  );

  const handlePageChange = useCallback((page: number) => {
    setState((prev) => ({ ...prev, currentPage: page }));
  }, []);

  const handleRefresh = useCallback(() => {
    fetchCharacters(true);
  }, [fetchCharacters]);

  // Effect to fetch data when page changes
  useEffect(() => {
    fetchCharacters();
  }, [state.currentPage, fetchCharacters]);

  return {
    ...state,
    handleDeleteCharacter,
    handlePageChange,
    handleRefresh,
    fetchCharacters,
  };
}
