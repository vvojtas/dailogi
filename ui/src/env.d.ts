/// <reference types="astro/client" />

declare namespace App {
  interface Locals {
    /**
     * Whether the current user is authenticated
     */
    isLoggedIn: boolean;
    /**
     * The current user's ID, if authenticated
     */
    userId?: string;
    /**
     * JWT token for API requests, if authenticated
     */
    token?: string;
  }
}

// Extend ImportMetaEnv to add custom environment variables
interface ImportMetaEnv {
  readonly SPRING_BACKEND_BASE_URL: string;
  // more env variables...
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
