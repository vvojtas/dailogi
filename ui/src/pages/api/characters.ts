import type { APIContext } from "astro";

// Base URL of your Java backend - read from environment variable
const BACKEND_BASE_URL = import.meta.env.PUBLIC_BACKEND_BASE_URL || "https://localhost";

if (!import.meta.env.PUBLIC_BACKEND_BASE_URL) {
  console.warn("PUBLIC_BACKEND_BASE_URL environment variable not set. Falling back to 'https://localhost'.");
}

// Basic proxy handler for GET requests
export async function GET({ request }: APIContext) {
  const url = new URL(request.url);
  const backendUrl = `${BACKEND_BASE_URL}/api/characters${url.search}`;

  console.log(`Proxying GET request to: ${backendUrl}`);

  try {
    const response = await fetch(backendUrl, {
      method: "GET",
      headers: {
        // Forward essential headers
        // Add 'Authorization': request.headers.get('Authorization') if needed
        "Content-Type": request.headers.get("Content-Type") || "application/json",
        // Add other headers you might need to forward
      },
      // IMPORTANT: This bypasses SSL certificate validation for localhost.
      // Use only in development environments!
      // For production, you need proper certificate handling.
      // agent: new (require('https').Agent)({ rejectUnauthorized: false }) // Requires node-fetch or similar
      // Node's built-in fetch might handle this differently or require global env var
    });

    // Return the response from the backend directly
    return new Response(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers: response.headers,
    });
  } catch (error: unknown) {
    console.error("Error proxying request to backend:", error);
    // Check if the error is an instance of Error and has a cause property
    if (error instanceof Error && error.cause && typeof error.cause === "object" && "code" in error.cause) {
      const cause = error.cause as { code: string }; // Type assertion after check
      if (
        cause.code === "UNABLE_TO_VERIFY_LEAF_SIGNATURE" ||
        cause.code === "CERT_HAS_EXPIRED" ||
        cause.code === "DEPTH_ZERO_SELF_SIGNED_CERT"
      ) {
        console.error(
          "HTTPS certificate error. Ensure backend server certificate is valid or adjust Node.js TLS settings for development (e.g., set NODE_TLS_REJECT_UNAUTHORIZED=0)."
        );
        return new Response("Backend certificate error", { status: 502 });
      }
    }
    return new Response("Error connecting to backend", { status: 502 }); // Bad Gateway
  }
}

// TODO: Add handlers for POST, PUT, DELETE, etc. mirroring this structure
// export async function POST({ request }: APIContext) { ... }
// export async function PUT({ request }: APIContext) { ... }
// export async function DELETE({ request }: APIContext) { ... }
