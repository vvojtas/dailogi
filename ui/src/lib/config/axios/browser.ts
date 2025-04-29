import axios, { type AxiosInstance, type AxiosError } from "axios";
import { ROUTES } from "../routes";
import { toast } from "sonner";

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
