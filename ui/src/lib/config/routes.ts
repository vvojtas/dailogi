export const ROUTES = {
  // Public routes
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",

  // Protected routes
  DASHBOARD: "/dashboard",
  CHARACTERS: "/characters",
  SCENES: "/scenes",
  PROFILE: "/profile",

  // API routes are defined in the generated API client
  API: {
    // Placeholder for any additional API routes not covered by the generated client
  },
} as const;

// Helper to check if route is protected
export const isProtectedRoute = (path: string): boolean => {
  const publicPaths: readonly string[] = [ROUTES.HOME, ROUTES.LOGIN, ROUTES.REGISTER, ROUTES.CHARACTERS];

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
