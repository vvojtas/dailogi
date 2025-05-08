import axios, { type AxiosInstance } from "axios";

// Astro-to-Spring instance (for server-side usage)
export const serverApi: AxiosInstance = axios.create({
  // Spring backend URL
  baseURL: process.env.SPRING_BACKEND_BASE_URL,
});

// Add request interceptors
serverApi.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
);

// Add response interceptors for server API
serverApi.interceptors.response.use(
  (response) => response,
  (error) => {
    // Server-side error handling doesn't show UI notifications
    return Promise.reject(error);
  }
);

// Patch the global axios instance for server environment
if (typeof window === "undefined") {
  console.log("Applying server-side Axios patch...");
  Object.keys(serverApi.defaults).forEach((key) => {
    if (key !== "headers") {
      (axios.defaults as Record<string, unknown>)[key] = (serverApi.defaults as Record<string, unknown>)[key];
    }
  });
  axios.interceptors.request = serverApi.interceptors.request;
  axios.interceptors.response = serverApi.interceptors.response;
  console.log("Server-side Axios patch applied.");
}

export default serverApi;
