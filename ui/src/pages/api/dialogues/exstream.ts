import type { APIRoute } from "astro";
import { SESSION_COOKIE_NAME } from "@/lib/config/cookies";
import { ENV } from "@/lib/config/env";

const backendBaseUrl = ENV.SPRING_BACKEND_BASE_URL;

/**
 * Special endpoint for handling SSE (Server-Sent Events) for dialogues.
 * Configured to properly forward the stream without buffering.
 */
export const POST: APIRoute = async ({ request, cookies }) => {
  const token = cookies.get(SESSION_COOKIE_NAME)?.value;

  if (!backendBaseUrl) {
    console.error("SPRING_BACKEND_BASE_URL environment variable is not set.");
    return new Response(JSON.stringify({ message: "Backend service configuration error." }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }

  // Construct the target URL for the backend
  const targetUrl = `${backendBaseUrl.replace(/\/$/, "")}/api/dialogues/stream`;

  // Prepare headers for the backend request
  const backendHeaders = new Headers();

  // Set critical headers
  backendHeaders.set("Content-Type", "application/json");
  backendHeaders.set("Accept", "text/event-stream");

  // Add headers to prevent buffering
  backendHeaders.set("Cache-Control", "no-cache, no-store, no-transform");
  backendHeaders.set("Connection", "keep-alive");
  backendHeaders.set("X-Accel-Buffering", "no");

  // Add Authorization header if token exists
  if (token) {
    backendHeaders.set("Authorization", `Bearer ${token}`);
  }

  try {
    // Get JSON data from the request
    const requestData = await request.json();

    console.log("Forwarding request to backend:", {
      url: targetUrl,
      method: "POST",
      headers: Object.fromEntries(backendHeaders.entries()),
      body: JSON.stringify(requestData),
    });

    // Forward the request to the backend
    const backendResponse = await fetch(targetUrl, {
      method: "POST",
      headers: backendHeaders,
      body: JSON.stringify(requestData),
    });

    if (!backendResponse.ok) {
      // If backend returned an error, pass it on
      const errorText = await backendResponse.text();
      console.error(`Backend SSE error (${backendResponse.status}):`, errorText);

      return new Response(errorText, {
        status: backendResponse.status,
        headers: { "Content-Type": backendResponse.headers.get("Content-Type") || "application/json" },
      });
    }

    // Create a new TransformStream for data forwarding
    const { readable, writable } = new TransformStream();

    // Run processing in the background, without waiting for it to complete
    (async () => {
      const reader = backendResponse.body?.getReader();
      const writer = writable.getWriter();

      if (!reader) {
        console.error("No reader available from backend response");
        writer.close();
        return;
      }

      try {
        while (true) {
          const { done, value } = await reader.read();

          if (done) {
            console.log("SSE stream completed");
            await writer.close();
            break;
          }

          // Log receipt of chunk
          console.log(`SSE chunk received: ${value.byteLength} bytes`);

          // Immediately write to output stream
          await writer.write(value);

          // Show chunk content in text format (optional)
          try {
            const textChunk = new TextDecoder().decode(value, { stream: true });
            console.log("Chunk text preview:", textChunk.substring(0, 100) + (textChunk.length > 100 ? "..." : ""));
          } catch (e) {
            console.error("Couldn't decode chunk as text:", e);
          }
        }
      } catch (e) {
        console.error("Error processing SSE stream:", e);
        writer.abort(e);
      }
    })();

    // Return response with custom stream - immediately, without waiting for processing to complete
    return new Response(readable, {
      status: 200,
      headers: {
        "Content-Type": "text/event-stream",
        "Cache-Control": "no-cache, no-store, no-transform",
        Connection: "keep-alive",
        "X-Accel-Buffering": "no",
        "Transfer-Encoding": "chunked",
      },
    });
  } catch (error) {
    console.error(`Error proxying SSE request to ${targetUrl}:`, error);
    const errorMessage = "Failed to connect to the backend dialogue service.";
    const errorStatus = 503; // Service Unavailable

    return new Response(JSON.stringify({ message: errorMessage }), {
      status: errorStatus,
      headers: { "Content-Type": "application/json" },
    });
  }
};
