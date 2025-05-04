import { useFormContext } from "react-hook-form";
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { CircleSlash } from "lucide-react";
import type { CharacterFormData } from "@/lib/validation/characterSchema";
import type { Llmdto } from "@/dailogi-api/model";

interface CharacterLlmFieldProps {
  llms: Llmdto[];
  disabled: boolean;
}

export function CharacterLlmField({ llms, disabled }: CharacterLlmFieldProps) {
  const { control } = useFormContext<CharacterFormData>();

  return (
    <FormField
      control={control}
      name="default_llm_id"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Model językowy</FormLabel>
          <Select onValueChange={field.onChange} defaultValue={field.value} disabled={disabled}>
            <FormControl>
              <SelectTrigger className="min-w-[180px]!" data-testid="character-llm-select">
                <SelectValue placeholder="Wybierz najstosowniejszy LLM" />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              <SelectItem value="undefined">
                <CircleSlash className="h-4 w-4 mr-2" /> Brak modelu
              </SelectItem>
              {llms.map((llm) => (
                <SelectItem key={llm.id} value={llm.id.toString()}>
                  {llm.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}
