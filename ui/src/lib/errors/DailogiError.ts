import type { AxiosError } from "axios";
import type { ErrorResponseDTO } from "@/dailogi-api/model/errorResponseDTO";
import { extractErrorResponseDTO } from "./errorUtils";

/**
 * Custom error class wrapping AxiosError with additional meta information
 */
export class DailogiError<T = unknown> extends Error {
  readonly displayed: boolean;
  readonly status?: number;
  readonly errorData?: ErrorResponseDTO;

  constructor(originalError: AxiosError<T>, displayed = false, errorResponseDTO?: ErrorResponseDTO) {
    super(errorResponseDTO?.message || originalError.message);
    this.name = "DailogiError";
    this.displayed = displayed;
    this.status = originalError.response?.status;

    // Use provided ErrorResponseDTO if available, otherwise keep the existing behavior
    if (errorResponseDTO) {
      this.errorData = errorResponseDTO;
    } else {
      this.errorData = extractErrorResponseDTO(originalError) as ErrorResponseDTO;
    }

    // Maintain the prototype chain for instanceof to work correctly
    Object.setPrototypeOf(this, DailogiError.prototype);
  }
}
