export const ROUTES = {
  // Public routes
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",

  // Protected routes
  DASHBOARD: "/dashboard",
  CHARACTERS: "/characters",
  CHARACTER_CREATE: "/characters/new",
  CHARACTER_DETAIL_PATTERN: "/characters/:id",
  CHARACTER_EDIT_PATTERN: "/characters/:id/edit",
  SCENES: "/scenes",
  SCENE_NEW: "/scenes/new",
  PROFILE: "/profile",

  // API routes are defined in the generated API client
  API: {
    // Placeholder for any additional API routes not covered by the generated client
  },
} as const;

// Helper to check if route is protected
export const isProtectedRoute = (path: string): boolean => {
  const publicPaths: readonly string[] = [ROUTES.HOME, ROUTES.LOGIN, ROUTES.REGISTER, ROUTES.CHARACTERS];

  // Character detail pages are public (but API will handle authorization)
  if (path.match(/^\/characters\/\d+$/)) {
    return false;
  }

  // If path starts with /api, it's handled by API routes
  if (path.startsWith("/api")) {
    return false;
  }

  // If path matches any public route exactly, it's not protected
  if (publicPaths.includes(path)) {
    return false;
  }

  // If path starts with any public route, it's not protected
  if (publicPaths.some((route) => path.startsWith(route))) {
    return false;
  }

  // All other routes are protected
  return true;
};

// --- Route Helper Functions ---

/**
 * Generates the URL for the character detail page.
 * @param id - The ID of the character.
 * @returns The URL string.
 */
export function getCharacterDetailUrl(id: number | string): string {
  return ROUTES.CHARACTER_DETAIL_PATTERN.replace(":id", String(id));
}

/**
 * Generates the URL for the character edit page.
 * @param id - The ID of the character.
 * @returns The URL string.
 */
export function getCharacterEditUrl(id: number | string): string {
  return ROUTES.CHARACTER_EDIT_PATTERN.replace(":id", String(id));
}
