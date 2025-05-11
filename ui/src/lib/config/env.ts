/**
 * Environment variable helper that works in both development and production environments
 *
 * Handles both:
 * - Production deployments with system environment variables (process.env)
 * - Local development/tests with .env files (import.meta.env)
 */
export function getEnv(key: string): string | undefined {
  // Try process.env first (for production environments)
  if (process.env && key in process.env && process.env[key] !== undefined && process.env[key] !== "") {
    return process.env[key];
  }

  // Fall back to import.meta.env (for development with .env files)
  return (import.meta.env as Record<string, string>)[key];
}

// Cache common environment variables for performance
export const ENV = {
  SPRING_BACKEND_BASE_URL: getEnv("SPRING_BACKEND_BASE_URL"),
};
