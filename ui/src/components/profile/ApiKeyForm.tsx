import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { useHydration } from "@/lib/hooks/useHydration";

interface ApiKeyFormProps {
  apiKey: string;
  hasApiKey: boolean;
  loading: boolean;
  onSave: (apiKey: string) => Promise<void>;
  onDelete: () => Promise<void>;
  onInputChange: (value: string) => void;
}

export function ApiKeyForm({ apiKey, hasApiKey, loading, onSave, onDelete, onInputChange }: Readonly<ApiKeyFormProps>) {
  const isHydrated = useHydration();
  const [showConfirmDelete, setShowConfirmDelete] = useState(false);

  const handleSave = async () => {
    await onSave(apiKey);
  };

  const handleDelete = async () => {
    if (!showConfirmDelete) {
      setShowConfirmDelete(true);
      return;
    }

    await onDelete();
    setShowConfirmDelete(false);
  };

  const isDisabled = loading || !isHydrated;
  const saveDisabled = isDisabled || apiKey.trim().length === 0;
  const deleteDisabled = isDisabled || !hasApiKey;

  return (
    <Card>
      <CardContent className="pt-6 space-y-4">
        <div className="space-y-2">
          <Input
            type={hasApiKey ? "password" : "text"}
            placeholder={
              hasApiKey ? "Twój klucz jest zapisany i zabezpieczony" : "Wprowadź sekretny klucz do otwarcia OpenRouter"
            }
            value={apiKey}
            onChange={(e) => onInputChange(e.target.value)}
            disabled={isDisabled}
            data-testid="api-key-input"
          />
        </div>
      </CardContent>
      <CardFooter className="flex justify-end gap-2">
        <Button
          variant="outline"
          onClick={handleDelete}
          disabled={deleteDisabled}
          data-testid="delete-api-key-button"
          className={showConfirmDelete ? "bg-destructive text-destructive-foreground hover:bg-destructive/90" : ""}
        >
          {showConfirmDelete ? "Potwierdź wymazanie" : "Zlikwiduj klucz"}
        </Button>
        <Button onClick={handleSave} disabled={saveDisabled} data-testid="save-api-key-button">
          {loading ? "Zapisywanie..." : hasApiKey ? "Podmień klucz" : "Uwiecznij klucz"}
        </Button>
      </CardFooter>
    </Card>
  );
}
