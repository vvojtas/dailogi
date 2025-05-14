import { FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Select, SelectContent, SelectItem, SelectTrigger } from "@/components/ui/select";
import { CharacterAvatar } from "@/components/characters/CharacterAvatar";
import { Button } from "@/components/ui/button";
import { X, Armchair, BookOpen, Star } from "lucide-react";
import { useFormContext } from "react-hook-form";
import { useEffect, useState, useCallback } from "react";
import { getCharacter } from "@/dailogi-api/characters/characters";
import { toast } from "sonner";
import { DailogiError } from "@/lib/errors/DailogiError";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import type { CharacterDTO } from "@/dailogi-api/model/characterDTO";
import type { CharacterDropdownDTO } from "@/dailogi-api/model/characterDropdownDTO";
import type { Llmdto } from "@/dailogi-api/model/llmdto";

// Custom component for truncating select trigger content
interface TruncatedSelectTriggerProps {
  placeholder: string;
  selectedName?: string;
  disabled?: boolean;
}

function TruncatedSelectTrigger({ placeholder, selectedName, disabled }: TruncatedSelectTriggerProps) {
  return (
    <SelectTrigger className="truncate w-full max-w-full" disabled={disabled}>
      {selectedName ? (
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger asChild>
              <span className="truncate block max-w-[90%]">{selectedName}</span>
            </TooltipTrigger>
            <TooltipContent side="top">{selectedName}</TooltipContent>
          </Tooltip>
        </TooltipProvider>
      ) : (
        <span>{placeholder}</span>
      )}
    </SelectTrigger>
  );
}

interface CharacterSlotProps {
  index: number;
  characters: CharacterDropdownDTO[];
  llms: Llmdto[];
  disabled?: boolean;
}

// Custom hook to manage character selection and details
function useCharacterSlot(index: number, characters: CharacterDropdownDTO[]) {
  const form = useFormContext();
  const { setValue } = form;
  const characterId = form.watch(`configs.${index}.characterId`);
  const llmId = form.watch(`configs.${index}.llmId`);

  // Find selected character from available options
  const selectedCharacter = characters.find((c) => c.id === characterId);

  // State to store full character details including short_description
  const [characterDetails, setCharacterDetails] = useState<CharacterDTO | null>(null);

  // Function to fetch character details - stabilized with minimal dependencies
  const stableFetchCharacterDetails = useCallback(
    async (id: number) => {
      if (!id) return;

      setCharacterDetails(null); // Reset details for loading state

      try {
        const response = await getCharacter(id);
        const details = response.data;
        setCharacterDetails(details);

        // Set default LLM if available
        if (details?.default_llm_id) {
          setValue(`configs.${index}.llmId`, details.default_llm_id, {
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
    [setValue, index] // Only stable dependencies
  );

  // Effect to handle character selection and details fetching
  useEffect(() => {
    if (characterId) {
      // Only fetch if details are not already loaded for this character
      if (!characterDetails || characterDetails.id !== characterId) {
        stableFetchCharacterDetails(characterId);
      }
    } else {
      // When character is cleared
      setCharacterDetails(null);
      setValue(`configs.${index}.llmId`, undefined, {
        shouldValidate: true,
        shouldDirty: true,
        shouldTouch: true,
      });
    }
  }, [characterId, characterDetails, stableFetchCharacterDetails, setValue, index]);

  // Function to clear the selection
  const handleClearSelection = useCallback(() => {
    setValue(`configs.${index}.characterId`, undefined, {
      shouldValidate: true,
      shouldDirty: true,
      shouldTouch: true,
    });
    setValue(`configs.${index}.llmId`, undefined, {
      shouldValidate: true,
      shouldDirty: true,
      shouldTouch: true,
    });
    setCharacterDetails(null);
  }, [setValue, index]);

  // Get description for tooltip
  const tooltipDescription = characterDetails?.short_description || "Brak danych";

  return {
    characterId,
    llmId,
    selectedCharacter,
    characterDetails,
    tooltipDescription,
    handleClearSelection,
  };
}

export function CharacterSlot({ index, characters, llms, disabled = false }: Readonly<CharacterSlotProps>) {
  const form = useFormContext();
  const { characterId, llmId, selectedCharacter, characterDetails, tooltipDescription, handleClearSelection } =
    useCharacterSlot(index, characters);

  // Get selected LLM name
  const selectedLlm = llms.find((llm) => llm.id === llmId);

  // For debugging and to make the linter happy
  useEffect(() => {
    if (process.env.NODE_ENV === "development") {
      console.debug(`CharacterSlot ${index}: Character ID = ${characterId}, LLM ID = ${llmId}`);
    }
  }, [index, characterId, llmId]);

  return (
    <div
      className={`relative p-4 border rounded-md space-y-4 overflow-hidden ${
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
                        key={`avatar-${selectedCharacter.id}-${!!selectedCharacter.avatar_url}`}
                        hasAvatar={selectedCharacter.has_avatar}
                        avatarUrl={selectedCharacter.avatar_url}
                        characterName={selectedCharacter.name}
                        className="h-20 w-20"
                      />
                    </div>
                  </div>
                </TooltipTrigger>
                <TooltipContent side="bottom" sideOffset={10} className="max-w-[250px]">
                  <p className="line-clamp-4">{tooltipDescription}</p>
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
          <FormItem className="text-center">
            <FormLabel className="flex justify-center">Wybierz postać</FormLabel>
            <Select
              key={`character-select-${index}-${characterId}`}
              value={field.value?.toString() || ""}
              onValueChange={(value) => {
                field.onChange(value === "undefined" ? undefined : parseInt(value, 10));
              }}
              disabled={disabled}
            >
              <FormControl>
                <TruncatedSelectTrigger
                  placeholder="Zaangażuj postać..."
                  selectedName={selectedCharacter?.name}
                  disabled={disabled}
                />
              </FormControl>
              <SelectContent className="max-w-[300px]">
                {characters.map((character) => (
                  <SelectItem
                    key={character.id}
                    value={character.id.toString()}
                    className={`relative ${character.is_global ? "bg-secondary/80" : ""}`}
                  >
                    <div className="flex items-center gap-2 pr-6 w-full">
                      <CharacterAvatar
                        hasAvatar={character.has_avatar}
                        avatarUrl={character.avatar_url}
                        characterName={character.name}
                        className="h-6 w-6 shrink-0"
                      />
                      <span className="truncate">{character.name}</span>
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
          <FormItem className="text-center">
            <FormLabel className="flex justify-center">Wybierz model LLM</FormLabel>
            <Select
              value={field.value?.toString() ?? ""}
              onValueChange={(value) => {
                field.onChange(value === "undefined" ? undefined : parseInt(value, 10));
              }}
              disabled={disabled || !characterId}
            >
              <FormControl>
                <TruncatedSelectTrigger
                  placeholder="LLM u steru"
                  selectedName={selectedLlm?.name}
                  disabled={disabled || !characterId}
                />
              </FormControl>
              <SelectContent className="max-w-[300px]">
                {llms.map((llm) => (
                  <SelectItem
                    key={llm.id}
                    value={llm.id.toString()}
                    className={`relative ${characterDetails?.default_llm_id === llm.id ? "font-medium bg-secondary/20" : ""}`}
                  >
                    <div className="flex items-center w-full pr-6">
                      <span className="truncate">{llm.name}</span>
                    </div>
                    {characterDetails?.default_llm_id === llm.id && (
                      <div className="absolute right-2 top-1/2 -translate-y-1/2">
                        <Star className="h-3 w-3 text-yellow-500" />
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
    </div>
  );
}
