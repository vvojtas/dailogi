import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { UserDto } from "@/dailogi-api/model";

interface AuthStateInternal {
  _user: UserDto | null;
  _isLoggedIn: boolean;
}

export interface AuthState {
  expiresAt: number | null;
  setUser: (user: UserDto | null, expirySeconds?: number) => void;
  getUser: () => UserDto | null;
  getIsLoggedIn: () => boolean;
  logout: () => void;
}

// Combine both for the store's internal use
type FullAuthState = AuthStateInternal & AuthState;

export const useAuthStore = create<FullAuthState>()(
  persist(
    (set, get) => ({
      // Internal state (protected by underscore prefix convention)
      _user: null,
      _isLoggedIn: false,

      // Public methods
      expiresAt: null,
      setUser: (user: UserDto | null, expirySeconds?: number) => {
        const expiresAt = user && expirySeconds ? Date.now() + expirySeconds * 1000 : null;
        set({ _user: user, _isLoggedIn: !!user, expiresAt });
      },
      getUser: () => {
        const { _user, expiresAt, logout } = get();
        if (expiresAt && Date.now() > expiresAt) {
          logout();
          return null;
        }
        return _user;
      },
      getIsLoggedIn: () => {
        const { _isLoggedIn, expiresAt, logout } = get();
        if (expiresAt && Date.now() > expiresAt) {
          logout();
          return false;
        }
        return _isLoggedIn;
      },
      logout: () => set({ _user: null, _isLoggedIn: false, expiresAt: null }),
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        _user: state._user,
        _isLoggedIn: state._isLoggedIn,
        expiresAt: state.expiresAt,
      }),
    }
  )
);
