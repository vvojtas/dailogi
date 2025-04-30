import { useEffect, useState, useCallback } from "react";
import { useLlmStore } from "@/lib/stores/llm.store";

/**
 * Hook for accessing and managing LLM data
 */
export function useLlms() {
  // Get store state and actions
  const { llms, isLoading, error, fetchLlms, getLlmById, getLlmNameById } = useLlmStore();

  // Local state to track if initial loading has been done
  const [initialized, setInitialized] = useState(false);

  // Initialize data on first mount
  useEffect(() => {
    if (!initialized && llms.length === 0) {
      fetchLlms().then(() => setInitialized(true));
    } else {
      setInitialized(true);
    }
  }, [fetchLlms, initialized, llms.length]);

  // Function to refresh LLM data on demand
  const refreshLlms = useCallback(async () => {
    return await fetchLlms();
  }, [fetchLlms]);

  // Get LLM name with error handling
  const getLlmName = useCallback(
    (id: number | undefined | null): string => {
      if (id === undefined || id === null) return "Nie wybrano modelu";
      const name = getLlmNameById(id);
      return name || "Nieznany model";
    },
    [getLlmNameById]
  );

  return {
    // Data
    llms,
    isLoading: isLoading || !initialized,
    error,
    initialized,

    // Actions
    refreshLlms,
    getLlmById,
    getLlmName,
  };
}
