import { FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Textarea } from "@/components/ui/textarea";
import { useFormContext } from "react-hook-form";
import type { NewSceneFormData } from "@/lib/hooks/useNewScene.ts";

interface SceneDescriptionInputProps {
  disabled?: boolean;
}

export function SceneDescriptionInput({ disabled = false }: Readonly<SceneDescriptionInputProps>) {
  const form = useFormContext<NewSceneFormData>();

  return (
    <FormField
      control={form.control}
      name="description"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Opis sceny</FormLabel>
          <FormControl>
            <Textarea
              placeholder='"Nie jesteśmy produktem naszych okoliczności. Jesteśmy produktem naszych decyzji."'
              className="resize-none min-h-24"
              maxLength={500}
              disabled={disabled}
              {...field}
              data-testid="scene-description-input"
            />
          </FormControl>
          <FormDescription>Maksymalnie 500 znaków. Opisz co uczestnicy mają omówić.</FormDescription>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}
