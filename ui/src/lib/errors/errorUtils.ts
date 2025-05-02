import type { AxiosError } from "axios";
import type { ErrorResponseDTO } from "@/dailogi-api/model/errorResponseDTO";

/**
 * Extracts ErrorResponseDTO from an AxiosError if possible
 * @param error The AxiosError to extract from
 * @returns The extracted ErrorResponseDTO or undefined if not found
 */
export function extractErrorResponseDTO(error: AxiosError): ErrorResponseDTO | undefined {
  if (!error.response?.data) {
    return undefined;
  }

  const data = error.response.data;

  // Check if data is already an ErrorResponseDTO
  if (typeof data === "object" && data !== null && "message" in data && "code" in data) {
    return data as ErrorResponseDTO;
  }

  // Create fallback ErrorResponseDTO if possible
  if (typeof data === "object" && data !== null) {
    const responseData = data as Record<string, unknown>;
    const message = typeof responseData.message === "string" ? responseData.message : "Unknown error";
    return {
      message,
      code: "UNKNOWN_ERROR",
      timestamp: new Date().toISOString(),
    };
  }

  // Create generic error if no usable data
  return {
    message: typeof data === "string" ? data : "Unknown error",
    code: "UNKNOWN_ERROR",
    timestamp: new Date().toISOString(),
  };
}
