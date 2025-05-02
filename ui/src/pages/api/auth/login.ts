import "@/lib/config/axios/server";
import { login } from "@/dailogi-api/authentication/authentication";
import type { APIRoute } from "astro";
import { createTokenCookieOptions } from "@/lib/config/cookies";
import type { AxiosError } from "axios";
import type { ErrorResponseDTO } from "@/dailogi-api/model/errorResponseDTO";

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
    const cookieValue = createTokenCookieOptions(data.access_token, data.expires_in);

    // Do not send the token to the client
    data.access_token = "";
    return new Response(JSON.stringify(data), {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Set-Cookie": cookieValue,
      },
    });
  } catch (error) {
    console.error("Login error:", error);

    // Check if this is an Axios error with response data
    if (error && typeof error === "object" && "isAxiosError" in error) {
      const axiosError = error as AxiosError<ErrorResponseDTO>;

      if (axiosError.response) {
        // Return the original error response data and status
        return new Response(
          JSON.stringify(
            axiosError.response.data || {
              message: "Wystąpił błąd podczas logowania",
              code: "AUTHENTICATION_ERROR",
              timestamp: new Date().toISOString(),
            }
          ),
          {
            status: axiosError.response.status || 500,
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
      }
    }

    // Fallback for non-axios errors or network errors
    return new Response(
      JSON.stringify({
        message: "Nie udało się nawiązać kontaktu z serwerem",
        code: "NETWORK_ERROR",
        timestamp: new Date().toISOString(),
      }),
      {
        status: 500,
        headers: {
          "Content-Type": "application/json",
        },
      }
    );
  }
};
