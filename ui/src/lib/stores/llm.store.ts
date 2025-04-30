import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import type { Llmdto } from "@/dailogi-api/model";
import { getLLMs } from "@/dailogi-api/llm/llm";

// Cache duration in milliseconds (default: 1 hour)
const CACHE_DURATION = 60 * 60 * 1000;

export interface LlmsState {
  // Data
  llms: Llmdto[];
  lastFetched: number | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchLlms: () => Promise<Llmdto[]>;
  getLlmById: (id: number) => Llmdto | undefined;
  getLlmNameById: (id: number) => string | null;
  reset: () => void;
}

export const useLlmStore = create<LlmsState>()(
  persist(
    (set, get) => ({
      // Initial state
      llms: [],
      lastFetched: null,
      isLoading: false,
      error: null,

      // Fetch all LLMs from the API
      fetchLlms: async () => {
        const { llms, lastFetched } = get();

        // Return cached data if it's still fresh
        if (llms.length > 0 && lastFetched && Date.now() - lastFetched < CACHE_DURATION) {
          return llms;
        }

        // Otherwise fetch fresh data
        try {
          set({ isLoading: true, error: null });
          const response = await getLLMs();
          const data = Array.isArray(response.data) ? response.data : [];
          set({
            llms: data,
            lastFetched: Date.now(),
            isLoading: false,
          });
          return data;
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : "Failed to fetch LLMs";
          set({ error: errorMessage, isLoading: false });
          return [];
        }
      },

      // Get LLM by ID
      getLlmById: (id: number) => {
        return get().llms.find((llm) => llm.id === id);
      },

      // Get LLM name by ID
      getLlmNameById: (id: number) => {
        const llm = get().getLlmById(id);
        return llm ? llm.name : null;
      },

      // Reset store state
      reset: () => {
        set({
          llms: [],
          lastFetched: null,
          isLoading: false,
          error: null,
        });
      },
    }),
    {
      name: "llm-storage",
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        llms: state.llms,
        lastFetched: state.lastFetched,
      }),
    }
  )
);
