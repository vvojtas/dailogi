import axios, { type AxiosInstance, type AxiosError } from "axios";
import { ROUTES } from "./routes";
import { toast } from "sonner";

// Browser-to-Astro instance (for client-side usage)
export const browserApi: AxiosInstance = axios.create({});

// Astro-to-Spring instance (for server-side usage)
export const serverApi: AxiosInstance = axios.create({
  // Spring backend URL - przywracamy port i usuwamy /api
  baseURL: "https://localhost:443",
  // Disable SSL certificate verification for development (remove in production)
  // httpsAgent: new (require("https").Agent)({ rejectUnauthorized: false }),
});

// Add request interceptors
browserApi.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
);

serverApi.interceptors.request.use(
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
        let message: string;
        // Handle specific error cases
        switch (error.response.status) {
          case 401:
            // Unauthorized - redirect to login
            window.location.href = ROUTES.LOGIN;
            break;
          case 403:
            toast.error("Nie dla twoich oczu");
            break;
          case 404:
            toast.error("To czego szukasz nie istnieje");
            break;
          case 500:
            toast.error("Niefortunny zbieg okoliczności doprowadził do wystąpienia błędu");
            break;
          default:
            // Handle other error cases
            message =
              error.response.data && typeof error.response.data === "object" && "message" in error.response.data
                ? (error.response.data.message as string)
                : "Wystąpił nieoczekiwany błąd";
            toast.error(message);
        }
      } else if (error.request) {
        // Network error
        toast.error("Nie udało się nawiązać kontaktu z serwerem");
      }
    }
    return Promise.reject(error);
  }
);

// Add response interceptors for server API
serverApi.interceptors.response.use(
  (response) => response,
  (error) => {
    // Server-side error handling doesn't show UI notifications
    return Promise.reject(error);
  }
);

// Patch the global axios instance based on environment
if (typeof window !== "undefined") {
  // Browser environment
  console.log("Applying browser-side Axios patch (original logic)...");
  Object.keys(browserApi.defaults).forEach((key) => {
    if (key !== "headers") {
      (axios.defaults as Record<string, unknown>)[key] = (browserApi.defaults as Record<string, unknown>)[key];
    }
  });
  axios.interceptors.request = browserApi.interceptors.request;
  axios.interceptors.response = browserApi.interceptors.response;
  console.log("Browser-side Axios patch applied (original logic).");
} else {
  // Server environment
  console.log("Applying server-side Axios patch (original logic)...");
  Object.keys(serverApi.defaults).forEach((key) => {
    if (key !== "headers") {
      (axios.defaults as Record<string, unknown>)[key] = (serverApi.defaults as Record<string, unknown>)[key];
    }
  });
  axios.interceptors.request = serverApi.interceptors.request;
  axios.interceptors.response = serverApi.interceptors.response;
  console.log("Server-side Axios patch applied (original logic).");
}
