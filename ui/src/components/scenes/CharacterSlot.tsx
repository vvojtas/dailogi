import { FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { CharacterAvatar } from "@/components/characters/CharacterAvatar";
import { Button } from "@/components/ui/button";
import { CircleSlash, X, Armchair, BookOpen } from "lucide-react";
import type { CharacterOption, LLMOption } from "@/lib/hooks/useNewScene";
import { useFormContext } from "react-hook-form";
import { useEffect, useState, useCallback } from "react";
import { getCharacter } from "@/dailogi-api/characters/characters";
import { toast } from "sonner";
import { DailogiError } from "@/lib/errors/DailogiError";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import type { CharacterDTO } from "@/dailogi-api/model/characterDTO";

interface CharacterSlotProps {
  index: number;
  characters: CharacterOption[];
  llms: LLMOption[];
  disabled?: boolean;
}

// Custom hook to manage character selection and details
function useCharacterSlot(index: number, characters: CharacterOption[]) {
  const form = useFormContext();
  const characterId = form.watch(`configs.${index}.characterId`);
  const llmId = form.watch(`configs.${index}.llmId`);

  // Find selected character from available options
  const selectedCharacter = characters.find((c) => c.id === characterId);

  // State to store full character details including short_description
  const [characterDetails, setCharacterDetails] = useState<CharacterDTO | null>(null);

  // Function to fetch character details
  const fetchCharacterDetails = useCallback(
    async (id: number) => {
      if (!id || (characterDetails && characterDetails.id === id)) return; // Avoid refetching if details are already loaded

      try {
        const response = await getCharacter(id);
        const details = response.data;
        setCharacterDetails(details);

        // Set default LLM if available
        if (details?.default_llm_id) {
          form.setValue(`configs.${index}.llmId`, details.default_llm_id, {
            shouldValidate: true,
            shouldDirty: true,
            shouldTouch: true,
          });
        }
      } catch (error) {
        console.error(`[CharacterSlot ${index}] Failed to fetch character details for ${id}:`, error);
        if (!(error instanceof DailogiError && error.displayed)) {
          toast.error("Nie udało się załadować szczegółów postaci.");
        }
      }
    },
    [form, index, characterDetails] // Add characterDetails as a dependency
  );

  // Fetch character details whenever characterId changes
  useEffect(() => {
    if (characterId) {
      fetchCharacterDetails(characterId);
    } else {
      setCharacterDetails(null);
    }
  }, [characterId, fetchCharacterDetails]);

  // Function to clear the selection
  const handleClearSelection = useCallback(() => {
    form.setValue(`configs.${index}.characterId`, undefined, {
      shouldValidate: true,
      shouldDirty: true,
      shouldTouch: true,
    });
    form.setValue(`configs.${index}.llmId`, undefined, {
      shouldValidate: true,
      shouldDirty: true,
      shouldTouch: true,
    });
    setCharacterDetails(null);
  }, [form, index]);

  // Function to handle character selection
  const handleCharacterChange = useCallback(
    (value: string) => {
      if (value === "undefined") {
        // Must clear both character and LLM when character is removed
        form.setValue(`configs.${index}.characterId`, undefined, {
          shouldValidate: true,
          shouldDirty: true,
          shouldTouch: true,
        });
        form.setValue(`configs.${index}.llmId`, undefined, {
          shouldValidate: true,
          shouldDirty: true,
          shouldTouch: true,
        });
      } else {
        form.setValue(`configs.${index}.characterId`, parseInt(value, 10), {
          shouldValidate: true,
          shouldDirty: true,
          shouldTouch: true,
        });
      }
    },
    [form, index]
  );

  // Function to handle LLM selection
  const handleLlmChange = useCallback(
    (value: string) => {
      if (value === "undefined") {
        form.setValue(`configs.${index}.llmId`, undefined, {
          shouldValidate: true,
          shouldDirty: true,
          shouldTouch: true,
        });
      } else {
        form.setValue(`configs.${index}.llmId`, parseInt(value, 10), {
          shouldValidate: true,
          shouldDirty: true,
          shouldTouch: true,
        });
      }
    },
    [form, index]
  );

  // Get description for tooltip
  const tooltipDescription = characterDetails?.short_description || "Brak dancyh";

  return {
    characterId,
    llmId,
    selectedCharacter,
    characterDetails,
    tooltipDescription,
    handleClearSelection,
    handleCharacterChange,
    handleLlmChange,
  };
}

export function CharacterSlot({ index, characters, llms, disabled = false }: Readonly<CharacterSlotProps>) {
  const form = useFormContext();
  const {
    characterId,
    llmId,
    selectedCharacter,
    characterDetails,
    tooltipDescription,
    handleClearSelection,
    handleCharacterChange,
    handleLlmChange,
  } = useCharacterSlot(index, characters);

  // For debugging and to make the linter happy
  useEffect(() => {
    if (process.env.NODE_ENV === "development") {
      console.debug(`CharacterSlot ${index}: LLM ID = ${llmId}`);
    }
  }, [index, llmId]);

  return (
    <div
      className={`relative p-4 border rounded-md space-y-4 ${
        selectedCharacter?.is_global ? "bg-secondary/30 border-secondary" : "bg-card"
      }`}
      data-testid={`character-slot-${index}`}
    >
      {/* Slot header with character number and clear button */}
      <div className="flex items-center justify-end mb-2 h-6">
        {characterId && (
          <Button
            variant="ghost"
            size="icon"
            className="h-6 w-6 rounded-full"
            onClick={handleClearSelection}
            type="button"
            disabled={disabled}
          >
            <X className="h-3 w-3" />
            <span className="sr-only">Wyczyść wybór</span>
          </Button>
        )}
      </div>

      {/* Selected character name and avatar (or placeholders) */}
      <div className="text-center">
        {selectedCharacter ? (
          <>
            <TooltipProvider>
              <Tooltip key={`tooltip-${selectedCharacter.id}-${characterDetails?.id ?? "loading"}`}>
                <TooltipTrigger asChild>
                  <div className="cursor-default">
                    <h3 className="font-medium text-base">{selectedCharacter.name}</h3>
                    <div className="flex justify-center my-4">
                      <CharacterAvatar
                        key={`avatar-${selectedCharacter.id}-${!!selectedCharacter.avatarUrl}`}
                        hasAvatar={!!selectedCharacter.avatarUrl}
                        avatarUrl={selectedCharacter.avatarUrl ?? undefined}
                        characterName={selectedCharacter.name}
                        className="h-20 w-20"
                      />
                    </div>
                  </div>
                </TooltipTrigger>
                <TooltipContent side="bottom" sideOffset={10}>
                  <p>{tooltipDescription}</p>
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          </>
        ) : (
          <>
            <h3 className="font-medium text-base text-muted-foreground">Wakat</h3>
            <div className="flex justify-center my-4">
              <div className="h-20 w-20 rounded-full bg-muted flex items-center justify-center border">
                <Armchair className="h-8 w-8 text-muted-foreground" />
              </div>
            </div>
          </>
        )}
      </div>

      {/* Character selection field */}
      <FormField
        control={form.control}
        name={`configs.${index}.characterId`}
        render={({ field }) => (
          <FormItem>
            <FormLabel>Wybierz postać</FormLabel>
            <Select
              key={`character-select-${index}-${characterId}`}
              value={field.value?.toString() || ""}
              onValueChange={(value) => {
                field.onChange(value === "undefined" ? undefined : parseInt(value, 10));
                handleCharacterChange(value);
              }}
              disabled={disabled}
            >
              <FormControl>
                <SelectTrigger>
                  <SelectValue placeholder="Zaangażuj postać..." />
                </SelectTrigger>
              </FormControl>
              <SelectContent>
                <SelectItem key="undefined" value="undefined">
                  <CircleSlash className="h-4 w-4 mr-2" /> Odwołaj
                </SelectItem>
                {characters.map((character) => (
                  <SelectItem
                    key={character.id}
                    value={character.id.toString()}
                    className={`relative ${character.is_global ? "bg-secondary/80" : ""}`}
                  >
                    <div className="flex items-center gap-2 pr-6">
                      <CharacterAvatar
                        hasAvatar={!!character.avatarUrl}
                        avatarUrl={character.avatarUrl ?? undefined}
                        characterName={character.name}
                        className="h-6 w-6"
                      />
                      <span>{character.name}</span>
                    </div>
                    {character.is_global && (
                      <div className="absolute right-2 top-1/2 -translate-y-1/2 text-xs text-muted-foreground">
                        <BookOpen className="h-3 w-3" />
                      </div>
                    )}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <FormMessage />
          </FormItem>
        )}
      />

      {/* LLM selection field */}
      <FormField
        control={form.control}
        name={`configs.${index}.llmId`}
        render={({ field }) => (
          <FormItem>
            <FormLabel>Wybierz model LLM</FormLabel>
            <Select
              value={field.value?.toString() || ""}
              onValueChange={(value) => {
                field.onChange(value === "undefined" ? undefined : parseInt(value, 10));
                handleLlmChange(value);
              }}
              disabled={disabled || !characterId}
            >
              <FormControl>
                <SelectTrigger>
                  <SelectValue placeholder="LLM u steru" />
                </SelectTrigger>
              </FormControl>
              <SelectContent>
                <SelectItem value="undefined">
                  <CircleSlash className="h-4 w-4 mr-2" /> Brak modelu
                </SelectItem>
                {llms.map((llm) => (
                  <SelectItem
                    key={llm.id}
                    value={llm.id.toString()}
                    className={selectedCharacter?.default_llm_id === llm.id ? "font-medium bg-secondary/20" : ""}
                  >
                    {llm.name}
                    {selectedCharacter?.default_llm_id === llm.id && (
                      <span className="ml-2 text-xs text-muted-foreground">(Domyślny)</span>
                    )}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <FormMessage />
          </FormItem>
        )}
      />
    </div>
  );
}
