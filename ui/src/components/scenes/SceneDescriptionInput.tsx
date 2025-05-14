import { FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Textarea } from "@/components/ui/textarea";
import { useFormContext } from "react-hook-form";
import type { NewSceneFormData } from "@/lib/hooks/useNewScene";
import type { FormPhase } from "@/lib/hooks/useNewScene";

interface SceneDescriptionInputProps {
  disabled?: boolean;
  phase?: FormPhase;
}

export function SceneDescriptionInput({ disabled = false, phase = "config" }: Readonly<SceneDescriptionInputProps>) {
  const form = useFormContext<NewSceneFormData>();
  const isDisplayPhase = phase === "loading" || phase === "result";

  return (
    <FormField
      control={form.control}
      name="description"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Opis sceny</FormLabel>
          <FormControl>
            {isDisplayPhase ? (
              <div
                className="min-h-24 p-3 border rounded-md bg-muted/50 whitespace-pre-line"
                data-testid="scene-description-display"
              >
                {field.value || "Brak opisu sceny"}
              </div>
            ) : (
              <Textarea
                placeholder='"Nie jesteśmy produktem naszych okoliczności. Jesteśmy produktem naszych decyzji."'
                className="resize-none min-h-24"
                maxLength={1000}
                disabled={disabled}
                {...field}
                data-testid="scene-description-input"
              />
            )}
          </FormControl>
          {!isDisplayPhase && <FormDescription>Nakreśl temat rozmowy oraz okoliczności sceny</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  );
}
