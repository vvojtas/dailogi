import { useFormContext } from "react-hook-form";
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import type { CharacterFormData } from "@/lib/validation/characterSchema";

interface CharacterNameFieldProps {
  disabled: boolean;
}

export function CharacterNameField({ disabled }: CharacterNameFieldProps) {
  const { control } = useFormContext<CharacterFormData>();

  return (
    <FormField
      control={control}
      name="name"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Imię postaci</FormLabel>
          <FormControl>
            <Input
              placeholder="Kogo przedstawiasz?"
              {...field}
              disabled={disabled}
              data-testid="character-name-input"
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}
