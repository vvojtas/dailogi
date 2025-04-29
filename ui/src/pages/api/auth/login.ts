import "@/lib/config/axios";
import { login } from "@/dailogi-api/authentication/authentication";
import type { APIRoute } from "astro";

export const POST: APIRoute = async ({ request }) => {
  try {
    const loginData = await request.json();

    // Forward the request to Spring backend using the generated API client
    const response = await login(loginData);

    const data = await response.data;

    if (response.status !== 200) {
      return new Response(JSON.stringify(data), {
        status: response.status,
        headers: {
          "Content-Type": "application/json",
        },
      });
    }

    // Set the JWT token in an HTTP-only cookie
    const cookieOptions = [
      `session_token=${data.access_token}`,
      "HttpOnly",
      "Secure",
      "SameSite=Strict",
      // Set expiration based on token expiry
      `Max-Age=${data.expires_in}`,
      "Path=/",
    ];

    return new Response(JSON.stringify(data), {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Set-Cookie": cookieOptions.join("; "),
      },
    });
  } catch (error) {
    console.error("Login error:", error);
    return new Response(JSON.stringify({ message: "Wystąpił błąd podczas logowania" }), {
      status: 500,
      headers: {
        "Content-Type": "application/json",
      },
    });
  }
};
