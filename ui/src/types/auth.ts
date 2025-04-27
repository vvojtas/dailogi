/**
 * Type guard to check if a user is authenticated
 */
export function isAuthenticated(locals: App.Locals): locals is App.Locals & { userId: string; token: string } {
  return locals.isLoggedIn === true && typeof locals.userId === "string" && typeof locals.token === "string";
}

/**
 * Type representing an authenticated user's data
 */
export interface AuthenticatedUser {
  id: string;
  name: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Type representing the authentication state
 */
export interface AuthState {
  isAuthenticated: boolean;
  user: AuthenticatedUser | null;
  token: string | null;
}
