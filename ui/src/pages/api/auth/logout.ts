import "@/lib/config/axios/server";
import type { APIRoute } from "astro";
import { createExpiredCookieOptions } from "@/lib/config/cookies";

export const POST: APIRoute = async () => {
  try {
    // Set cookie expiration to a past date to remove it
    return new Response(JSON.stringify({ message: "Wylogowano pomyślnie" }), {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Set-Cookie": createExpiredCookieOptions(),
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
