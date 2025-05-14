import { DailogiError } from "@/lib/errors/DailogiError";

/**
 * Function to handle API errors and return appropriate Polish error messages
 * for scene operations
 */
export function handleSceneApiError(error: unknown): string | null {
  // If it's already a DailogiError and was displayed, don't show another toast
  if (error instanceof DailogiError && error.displayed) {
    return null;
  }

  // Get error code from response
  let errorCode: string | undefined;
  if (error instanceof DailogiError) {
    errorCode = error.errorData?.code;
  }

  // Default error message
  let errorMsg = "Nieokreślony błąd podczas tworzenia sceny";

  if (errorCode) {
    switch (errorCode) {
      case "CHARACTER_NOT_FOUND":
        errorMsg = "Postać przepadła i jest niedostępna";
        break;
      case "LLM_NOT_FOUND":
        errorMsg = "LLM u steru nie istnieje";
        break;
      case "VALIDATION_ERROR":
        errorMsg = "Nie wszystkie pola są wypełnione poprawnie";
        break;
      default:
        // Use message from errorData if available
        if (error instanceof DailogiError && error.errorData?.message) {
          errorMsg = error.errorData.message;
        }
    }
  }

  return errorMsg;
}
