import axios, { type AxiosInstance, type AxiosError } from "axios";
import { ROUTES } from "../routes";
import { toast } from "sonner";
import { navigate } from "@/lib/client/navigate";
import { useAuthStore } from "@/lib/stores/auth.store";
import { DailogiError } from "@/lib/errors/DailogiError";
import { extractErrorResponseDTO } from "@/lib/errors/errorUtils";

// Browser-to-Astro instance (for client-side usage)
export const browserApi: AxiosInstance = axios.create({});

// Add request interceptors
browserApi.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
);

// Add response interceptors for browser API
browserApi.interceptors.response.use(
  (response) => {
    console.log("browser axios response", response);
    return response;
  },
  (error: AxiosError) => {
    if (typeof window !== "undefined") {
      if (error.response) {
        // Extract error data in a standardized format
        const errorData = extractErrorResponseDTO(error);
        const status = error.response.status;
        let toastShown = false;

        // Handle specific status codes with if-else
        if (status === 401) {
          // Special case: Don't redirect or show toast for invalid credentials
          // Let the login form handle this error
          if (errorData?.code === "INVALID_CREDENTIALS") {
            console.log("Invalid credentials, letting login form handle it");
          } else {
            // Other 401 errors - logout user and redirect to login
            toast.error("Nie wiadomo kim jesteś - ujawnij się");
            useAuthStore.getState().logout();
            navigate(ROUTES.LOGIN);
            toastShown = true;
          }
        } else if (status === 403) {
          toast.error("Nie dla twoich oczu");
          toastShown = true;
        } else if (status === 404) {
          toast.error("To czego szukasz nie istnieje");
          toastShown = true;
        } else if (status >= 400 && status < 500) {
          // Other client errors (4xx)
          const message = errorData?.message || "";
          console.log("Error with backend call", status, message);
        } else if (status == 503) {
          toast.error("Serwery są zajętą planowaniem przyszłości AI, spróbuj ponownie później");
          toastShown = true;
        } else if (status >= 500) {
          // Server errors (5xx) not 503
          toast.error("Niefortunny zbieg okoliczności doprowadził do wystąpienia błędu");
          toastShown = true;
        } else {
          // Handle other error cases
          const message = errorData?.message || "";
          console.log("Error with backend call", status, message);
          toast.error("Wystąpił błąd, nikt go nieoczekiwał, ale i tak wystąpił");
          toastShown = true;
        }

        return Promise.reject(new DailogiError(error, toastShown, errorData));
      } else if (error.request) {
        // Network error
        toast.error("Nie udało się nawiązać kontaktu z serwerem");
        return Promise.reject(new DailogiError(error, true));
      }
    }
    return Promise.reject(error);
  }
);

// Patch the global axios instance for browser environment
if (typeof window !== "undefined") {
  console.log("Applying browser-side Axios patch...");
  Object.keys(browserApi.defaults).forEach((key) => {
    if (key !== "headers") {
      (axios.defaults as Record<string, unknown>)[key] = (browserApi.defaults as Record<string, unknown>)[key];
    }
  });
  axios.interceptors.request = browserApi.interceptors.request;
  axios.interceptors.response = browserApi.interceptors.response;
  console.log("Browser-side Axios patch applied.");
}

export default browserApi;
