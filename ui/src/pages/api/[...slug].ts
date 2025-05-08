import type { APIRoute } from "astro";
import { SESSION_COOKIE_NAME, createExpiredCookieOptions } from "@/lib/config/cookies";

const backendBaseUrl = process.env.SPRING_BACKEND_BASE_URL;

export const ALL: APIRoute = async ({ request, cookies, params }) => {
  const token = cookies.get(SESSION_COOKIE_NAME)?.value;
  const slug = params.slug;

  if (!backendBaseUrl) {
    console.error("SPRING_BACKEND_BASE_URL environment variable is not set.");
    return new Response(JSON.stringify({ message: "Backend service configuration error." }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }

  if (!slug) {
    // Should generally not happen with [...slug].ts but good practice
    return new Response(JSON.stringify({ message: "Invalid API route." }), {
      status: 400,
      headers: { "Content-Type": "application/json" },
    });
  }

  // Get the URL and extract query parameters
  const url = new URL(request.url);
  const queryString = url.search;

  // Construct the target URL for the backend, including query parameters
  const targetUrl = `${backendBaseUrl.replace(/\/$/, "")}/api/${slug}${queryString}`;

  // Prepare headers for the backend request
  const backendHeaders = new Headers(request.headers);

  // Remove headers that shouldn't be forwarded directly (e.g., host)
  backendHeaders.delete("host");
  // Astro adds connection: keep-alive, which might cause issues with some backends/proxies
  backendHeaders.delete("connection");

  // Add Authorization header if token exists
  if (token) {
    backendHeaders.set("Authorization", `Bearer ${token}`);
  }

  try {
    // Forward the request to the backend
    const backendResponse = await fetch(targetUrl, {
      method: request.method,
      headers: backendHeaders,
      // Forward body only for relevant methods
      body: request.method !== "GET" && request.method !== "HEAD" ? request.body : undefined,
      // Add duplex option when sending a body (required in newer Node.js versions)
      duplex: request.method !== "GET" && request.method !== "HEAD" ? "half" : undefined,
      // Pass through redirect handling
      redirect: "manual", // Let the client handle redirects based on backend response
    } as RequestInit & { duplex?: "half" });

    // Create a new response based on the backend response
    // Important: Create a new Headers object to avoid modifying the original
    const responseHeaders = new Headers(backendResponse.headers);

    // If response status is 401 (Unauthorized), clear the session cookie
    if (backendResponse.status === 401) {
      responseHeaders.set("Set-Cookie", createExpiredCookieOptions());
    }

    // TODO: Ensure CORS headers are handled correctly if needed (Astro might handle this)

    // Return the backend response to the client
    return new Response(backendResponse.body, {
      status: backendResponse.status,
      statusText: backendResponse.statusText,
      headers: responseHeaders,
    });
  } catch (error) {
    console.error(`Error proxying request to ${targetUrl}:`, error);
    let errorMessage = "Failed to connect to the backend service.";
    const errorStatus = 503; // Service Unavailable

    if (error instanceof Error && error.message.includes("ECONNREFUSED")) {
      errorMessage = "Backend service is unavailable.";
    }
    // TODO: Handle other potential fetch errors (timeouts, DNS issues etc.)

    return new Response(JSON.stringify({ message: errorMessage }), {
      status: errorStatus,
      headers: { "Content-Type": "application/json" },
    });
  }
};
