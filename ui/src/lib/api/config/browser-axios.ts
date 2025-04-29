import axios, { type AxiosInstance } from "axios";
import { ROUTES } from "../../config/routes";
import { toast } from "sonner";

const browserApi: AxiosInstance = axios.create({
  // Configure base URL if needed, e.g., pointing to Astro's backend API prefix
  // baseURL: '/api',
});

// Add request interceptor
browserApi.interceptors.request.use(
  (config) => {
    // Add any default headers if needed
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor
browserApi.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      let message: string;
      // Handle specific error cases
      switch (error.response.status) {
        case 401:
          // Unauthorized - redirect to login
          // Check if running in the browser context before redirecting
          if (typeof window !== "undefined") {
            window.location.href = ROUTES.LOGIN;
          }
          break;
        case 403:
          toast.error("Nie masz uprawnień do wykonania tej akcji."); // Changed message
          break;
        case 404:
          toast.error("Nie znaleziono zasobu."); // Changed message
          break;
        case 500:
          toast.error("Wystąpił wewnętrzny błąd serwera."); // Changed message
          break;
        default:
          // Handle other error cases
          message = error.response.data?.message || "Wystąpił nieoczekiwany błąd."; // Changed message
          toast.error(message);
      }
    } else if (error.request) {
      // Network error - Ensure this runs only client-side
      if (typeof window !== "undefined") {
        toast.error("Błąd sieci. Sprawdź połączenie internetowe."); // Changed message
      }
    }
    return Promise.reject(error);
  }
);

export default browserApi;
