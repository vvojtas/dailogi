import type { MiddlewareHandler } from "astro";
import { defineMiddleware } from "astro:middleware";

export const onRequest: MiddlewareHandler = defineMiddleware(async (context, next) => {
  // Initialize locals with default values
  context.locals.isLoggedIn = false;
  context.locals.token = undefined; // Initialize token as undefined

  // Get the session token from cookies
  const token = context.cookies.get("session")?.value;

  if (token) {
    try {
      // Here you would typically validate the token
      // For now, we'll just assume the presence of a token means the user is logged in
      context.locals.isLoggedIn = true;
      context.locals.token = token;

      // You could also decode the token to get the user ID
      // const decoded = jwt.verify(token, process.env.JWT_SECRET);
      // context.locals.userId = decoded.sub;
    } catch (error) {
      // If token validation fails, we'll keep isLoggedIn as false
      console.error("Error validating token:", error);
    }
  }

  // Continue to the next middleware or route handler
  return next();
});
