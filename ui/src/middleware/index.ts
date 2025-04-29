import { defineMiddleware } from "astro:middleware";
import { isProtectedRoute } from "@/lib/config/routes";

export const onRequest = defineMiddleware(async ({ request, redirect }, next) => {
  const url = new URL(request.url);
  const path = url.pathname;

  // Skip middleware for API routes and public pages
  if (path.startsWith("/api/")) {
    return next();
  }

  // Check if the route requires protection
  if (isProtectedRoute(path)) {
    // Get session cookie
    const cookies = request.headers.get("cookie");
    const hasSessionToken = cookies?.includes("session_token=");

    // If no session token present, redirect to login
    if (!hasSessionToken) {
      return redirect("/login");
    }
  }

  // Continue to next middleware or page
  return next();
});
