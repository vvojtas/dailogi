import type { APIRoute } from "astro";
import { SESSION_COOKIE_NAME } from "@/lib/config/cookies";
import { ENV } from "@/lib/config/env";

const backendBaseUrl = ENV.SPRING_BACKEND_BASE_URL;

/**
 * Specjalny endpoint do obsługi SSE (Server-Sent Events) dla dialogów.
 * Skonfigurowany tak, aby prawidłowo przekazywać strumień bez buforowania.
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

  // Ustaw krytyczne nagłówki
  backendHeaders.set("Content-Type", "application/json");
  backendHeaders.set("Accept", "text/event-stream");

  // Dodaj nagłówki zapobiegające buforowaniu
  backendHeaders.set("Cache-Control", "no-cache, no-store, no-transform");
  backendHeaders.set("Connection", "keep-alive");
  backendHeaders.set("X-Accel-Buffering", "no");

  // Add Authorization header if token exists
  if (token) {
    backendHeaders.set("Authorization", `Bearer ${token}`);
  }

  try {
    // Pobierz dane JSON z żądania
    const requestData = await request.json();

    console.log("Przekazuję żądanie do backendu:", {
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
      // Jeśli backend zwrócił błąd, przekaż go dalej
      const errorText = await backendResponse.text();
      console.error(`Backend SSE error (${backendResponse.status}):`, errorText);

      return new Response(errorText, {
        status: backendResponse.status,
        headers: { "Content-Type": backendResponse.headers.get("Content-Type") || "application/json" },
      });
    }

    // Stwórz nowy TransformStream do przekazywania danych
    const { readable, writable } = new TransformStream();

    // Uruchom przetwarzanie w tle, bez czekania na jego zakończenie
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
            console.log("SSE stream zakończony");
            await writer.close();
            break;
          }

          // Loguj otrzymanie chunka
          console.log(`SSE chunk otrzymany: ${value.byteLength} bajtów`);

          // Natychmiast zapisz do strumienia wyjściowego
          await writer.write(value);

          // Pokaż zawartość chunka w formacie tekstowym (opcjonalnie)
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

    // Zwróć odpowiedź z własnym strumieniem - natychmiast, bez czekania na zakończenie przetwarzania
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
