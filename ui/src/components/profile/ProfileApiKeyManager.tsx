import { ApiKeyStatus } from "@/components/profile/ApiKeyStatus";
import { ApiKeyForm } from "@/components/profile/ApiKeyForm";
import { useApiKey } from "@/lib/hooks/useApiKey";
import { useAuthStore } from "@/lib/stores/auth.store";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { InfoIcon } from "lucide-react";

export function ProfileApiKeyManager() {
  const { apiKey, hasApiKey, loading, setApiKey, saveKey, deleteKey } = useApiKey();
  const user = useAuthStore((state) => state.getUser());
  const username = user?.name ?? "";

  return (
    <div data-testid="profile-api-key-manager">
      <h1 className="text-3xl font-bold text-center mb-8" data-testid="profile-welcome-header">
        Witaj <span className="italic">{username}</span>!
      </h1>

      <div className="mb-8">
        <div className="mb-2">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-semibold">Klucz OpenRouter API</h2>
              <Tooltip>
                <TooltipTrigger asChild>
                  <InfoIcon className="h-5 w-5 text-muted-foreground hover:text-primary cursor-help" />
                </TooltipTrigger>
                <TooltipContent className="max-w-xs">
                  <p className="mb-2">
                    Twój klucz umożliwia interakcje z zaawansowanymi modelami językowymi - zarejestruj go, aby umożliwić
                    generowanie dialogów w d-ai-logi.
                  </p>
                  <p>
                    Nie zapomnij ustawić limitów, żeby zabezpieczyć swoje środki. Sprawdź cenniki OpenRoutera, niektóre
                    z wykorzystywanych modeli są darmowe. [Choć jeszcze nie są wspierane w d-ai-logi]
                  </p>
                </TooltipContent>
              </Tooltip>
            </div>
            <div className="flex items-center mt-5">
              <ApiKeyStatus hasApiKey={hasApiKey} />
            </div>
          </div>
        </div>

        <ApiKeyForm
          apiKey={apiKey}
          hasApiKey={hasApiKey}
          loading={loading}
          onSave={saveKey}
          onDelete={deleteKey}
          onInputChange={setApiKey}
        />
      </div>
    </div>
  );
}
