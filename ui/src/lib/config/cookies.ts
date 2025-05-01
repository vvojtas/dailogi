/**
 * Cookie configuration and utilities
 */

// Cookie name for the authentication token
export const SESSION_COOKIE_NAME = "session_token";

// Common cookie security options
const COMMON_COOKIE_OPTIONS = ["HttpOnly", "Secure", "SameSite=Strict", "Path=/"];

/**
 * Creates cookie options for an expired cookie to remove it from the client
 * @param cookieName The name of the cookie to expire
 * @returns String with cookie options for expiring/removing the cookie
 */
export function createExpiredCookieOptions(cookieName = SESSION_COOKIE_NAME): string {
  const options = [`${cookieName}=`, ...COMMON_COOKIE_OPTIONS, "Max-Age=0", "Expires=Thu, 01 Jan 1970 00:00:00 GMT"];

  return options.join("; ");
}

/**
 * Creates cookie options for storing a token
 * @param token The token value to store
 * @param maxAge Maximum age in seconds for the cookie
 * @returns String with cookie options for storing the token
 */
export function createTokenCookieOptions(token: string, maxAge: number): string {
  const options = [`${SESSION_COOKIE_NAME}=${token}`, ...COMMON_COOKIE_OPTIONS, `Max-Age=${maxAge}`];

  return options.join("; ");
}
