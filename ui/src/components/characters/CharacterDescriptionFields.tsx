import { useFormContext } from "react-hook-form";
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import type { CharacterFormData } from "@/lib/validation/characterSchema";

interface CharacterDescriptionFieldsProps {
  disabled: boolean;
}

export function CharacterDescriptionFields({ disabled }: Readonly<CharacterDescriptionFieldsProps>) {
  const { control } = useFormContext<CharacterFormData>();

  return (
    <>
      <FormField
        control={control}
        name="short_description"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Krótki opis</FormLabel>
            <FormControl>
              <Input
                placeholder="Sama esencja jestestwa"
                {...field}
                disabled={disabled}
                data-testid="character-short-desc-input"
              />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />

      <FormField
        control={control}
        name="description"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Biografia</FormLabel>
            <FormControl>
              <Textarea
                placeholder="Kim ona jest?"
                className="min-h-[150px] scrollbar-thin scrollbar-thumb-secondary scrollbar-track-transparent"
                {...field}
                disabled={disabled}
                data-testid="character-bio-input"
              />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />
    </>
  );
}
