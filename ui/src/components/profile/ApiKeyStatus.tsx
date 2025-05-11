import { Badge } from "@/components/ui/badge";

interface ApiKeyStatusProps {
  hasApiKey: boolean;
}

export function ApiKeyStatus({ hasApiKey }: Readonly<ApiKeyStatusProps>) {
  return (
    <div className="flex items-center justify-center mb-4" data-testid="api-key-status">
      {hasApiKey ? (
        <Badge variant="default" className="bg-green-500 hover:bg-green-600" data-testid="api-key-status-badge-active">
          Klucz leży bezpiecznie w naszych archiwach
        </Badge>
      ) : (
        <Badge variant="secondary" data-testid="api-key-status-badge-inactive">
          Jeszcze nie powierzono nam klucza API
        </Badge>
      )}
    </div>
  );
}
