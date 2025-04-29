import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { UserDto } from "@/dailogi-api/model";

export interface AuthState {
  user: UserDto | null;
  isLoggedIn: boolean;
  setUser: (user: UserDto | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isLoggedIn: false,
      setUser: (user: UserDto | null) => set({ user, isLoggedIn: !!user }),
      logout: () => set({ user: null, isLoggedIn: false }),
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({ user: state.user, isLoggedIn: state.isLoggedIn }),
    }
  )
);
