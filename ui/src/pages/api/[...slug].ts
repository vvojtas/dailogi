import type { APIRoute } from "astro";

const backendBaseUrl = import.meta.env.SPRING_BACKEND_BASE_URL;
const sessionCookieName = "session_token";

export const ALL: APIRoute = async ({ request, cookies, params }) => {
  const token = cookies.get(sessionCookieName)?.value;
  const slug = params.slug;

  if (!backendBaseUrl) {
    console.error("BACKEND_API_URL environment variable is not set.");
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
  // Astro/Vite adds connection: keep-alive, which might cause issues with some backends/proxies
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
      // Pass through redirect handling
      redirect: "manual", // Let the client handle redirects based on backend response
    });

    // Create a new response based on the backend response
    // Important: Create a new Headers object to avoid modifying the original
    const responseHeaders = new Headers(backendResponse.headers);

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
