import { DailogiError } from "@/lib/errors/DailogiError";

/**
 * Function to handle API error responses and return appropriate Polish error messages
 * for character create and update operations
 */
export function handleCharacterCreateUpdateError(error: unknown): string | null {
  // If it's already a DailogiError and was displayed, don't show another toast
  if (error instanceof DailogiError && error.displayed) {
    return null;
  }

  // Get error code and message from response
  let errorCode: string | undefined;
  let errorDetails: Record<string, unknown> | undefined;

  if (error instanceof DailogiError) {
    errorCode = error.errorData?.code;
    errorDetails = error.errorData?.details as Record<string, unknown> | undefined;
  }

  // Default error message
  let errorMsg = "Nie udało się zapisać postaci w rejestrze... Spróbuj ponownie";

  if (errorCode) {
    switch (errorCode) {
      case "VALIDATION_ERROR":
        errorMsg = "Formularz zawiera błędy weryfikacji";
        // If we have field-specific errors, show the first one
        if (errorDetails) {
          const firstError = Object.values(errorDetails)[0];
          if (firstError && typeof firstError === "string") {
            errorMsg = `Błąd weryfikacji: ${firstError}`;
          }
        }
        break;
      case "RESOURCE_DUPLICATE":
        errorMsg = "Ta postać już widnieje w rejestrze";
        break;
      case "RESOURCE_NOT_FOUND":
        errorMsg = "Nie odnaleziono postaci w rejestrze";
        break;
      case "TYPE_MISMATCH":
        errorMsg = "Podano nieprawidłowy format danych";
        break;
      case "CHARACTER_LIMIT_EXCEEDED":
        errorMsg = "Osiągnięto limit postaci w Twoim zespole! Więcej nie można stworzyć.";
        break;
    }
  }

  return errorMsg;
}
