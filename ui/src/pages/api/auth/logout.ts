import "@/lib/config/axios/server";
import type { APIRoute } from "astro";

export const POST: APIRoute = async () => {
  try {
    // Set cookie expiration to a past date to remove it
    const cookieOptions = [
      "session_token=",
      "HttpOnly",
      "Secure",
      "SameSite=Strict",
      "Max-Age=0",
      "Expires=Thu, 01 Jan 1970 00:00:00 GMT",
      "Path=/",
    ];
    return new Response(JSON.stringify({ message: "Wylogowano pomyślnie" }), {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Set-Cookie": cookieOptions.join("; "),
      },
    });
  } catch (error) {
    console.error("Logout error:", error);
    return new Response(JSON.stringify({ message: "Wystąpił błąd podczas wylogowywania" }), {
      status: 500,
      headers: {
        "Content-Type": "application/json",
      },
    });
  }
};
