import { useState, useEffect } from "react";
import { toast } from "sonner";
import { checkApiKeyStatus, setApiKey, deleteApiKey } from "@/dailogi-api/api-keys/api-keys";
import type { ApiKeyRequest } from "@/dailogi-api/model/apiKeyRequest";
import { DailogiError } from "@/lib/errors/DailogiError";
import axios from "axios";

interface ProfileApiKeyState {
  apiKey: string;
  hasApiKey: boolean;
  loading: boolean;
}

export function useApiKey() {
  const [state, setState] = useState<ProfileApiKeyState>({
    apiKey: "",
    hasApiKey: false,
    loading: true,
  });

  const loadStatus = async () => {
    setState((prev) => ({ ...prev, loading: true }));

    try {
      const response = await checkApiKeyStatus();
      setState((prev) => ({
        ...prev,
        hasApiKey: response.data.has_api_key,
        loading: false,
      }));
    } catch (error) {
      console.error("Error checking API key status:", error);

      setState((prev) => ({
        ...prev,
        loading: false,
      }));

      // Skip showing error message if it was already displayed
      if (error instanceof DailogiError && error.displayed) {
        return;
      }
      toast.error("Wszystkie klucze nam się rozsypały - nie wiadomo czy twój jest wśród nich");
    }
  };

  const saveKey = async (apiKey: string) => {
    console.log("Save API key start", apiKey); //TODO: remove
    if (!apiKey.trim()) {
      toast.error("OpenApi zdradzili nam sekret - klucz musi posiadać znaki");
      return;
    }

    setState((prev) => ({ ...prev, loading: true }));

    try {
      console.log("Saving API key:", apiKey); //TODO: remove
      const request: ApiKeyRequest = { api_key: apiKey.trim() };
      const response = await setApiKey(request);
      console.log("API key saved successfully"); //TODO: remove
      setState((prev) => ({
        ...prev,
        hasApiKey: response.data.has_api_key,
        loading: false,
        apiKey: "",
      }));
      console.log("API key saved - state updated"); //TODO: remove
      toast.success("Klucz API zachowany w naszych archiwach");
    } catch (error) {
      console.error("Error saving API key:", error);

      setState((prev) => ({
        ...prev,
        loading: false,
      }));

      // Skip showing error message if it was already displayed
      if (error instanceof DailogiError && error.displayed) {
        return;
      }

      // Extract status code and data properly
      let status: number | undefined;
      let errorMessage = "Klucz zbłądził gdzieś w drodze do archiwum, spróbuj ponownie";

      if (error instanceof DailogiError) {
        status = error.status;
        errorMessage = error.errorData?.message ?? errorMessage;
      } else if (axios.isAxiosError(error)) {
        status = error.response?.status;
        errorMessage = error.response?.data?.message ?? errorMessage;
      }

      // Use the extracted status
      if (status === 400) {
        toast.error("Nasi spece mówią, że poprawne klucze wyglądają inaczej");
      } else if (status === 401) {
        toast.error("Przedstaw sę najpierw, później załatwimy sprawę kluczy");
      } else {
        toast.error(errorMessage);
      }
    }
  };

  const deleteKey = async () => {
    setState((prev) => ({ ...prev, loading: true }));

    try {
      const response = await deleteApiKey();

      setState((prev) => ({
        ...prev,
        hasApiKey: response.data.has_api_key,
        loading: false,
        apiKey: "",
      }));

      toast.success("Archiwa zostały oczyszczone z resztek Twojego klucza");
    } catch (error) {
      console.error("Error deleting API key:", error);
      setState((prev) => ({
        ...prev,
        loading: false,
      }));

      // Skip showing error message if it was already displayed
      if (error instanceof DailogiError && error.displayed) {
        return;
      }

      toast.error("Klucz utkwił głęboko w archiwum, opiera się usunięciu");
    }
  };

  // Automatycznie pobierz status po zamontowaniu komponentu
  useEffect(() => {
    loadStatus();
  }, []);

  return {
    ...state,
    setApiKey: (value: string) => setState((prev) => ({ ...prev, apiKey: value })),
    loadStatus,
    saveKey,
    deleteKey,
  };
}
