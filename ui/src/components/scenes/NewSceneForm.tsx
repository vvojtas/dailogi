import { useEffect } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";
import { Card, CardContent } from "@/components/ui/card";
import { SceneDescriptionInput } from "./SceneDescriptionInput.tsx";
import { CharacterSelectionList } from "./CharacterSelectionList.tsx";
import { StartSceneButton } from "./StartSceneButton.tsx";
import { LoadingIndicator } from "./LoadingIndicator.tsx";
import { SceneResult } from "./SceneResult.tsx";
import { SaveSceneForm } from "./SaveSceneForm.tsx";
import { useNewScene, newSceneFormSchema, type NewSceneFormData, type FormPhase } from "@/lib/hooks/useNewScene";
import { useHydration } from "@/lib/hooks/useHydration";
import { getAllAvailableCharacters } from "@/dailogi-api/characters/characters";
import { getLLMs } from "@/dailogi-api/llm/llm";
import { DailogiError } from "@/lib/errors/DailogiError";
import { toast } from "sonner";
import type { CharacterDropdownDTO } from "@/dailogi-api/model/characterDropdownDTO";
import type { CharacterOption } from "@/lib/hooks/useNewScene";

/**
 * Function to handle API errors and return appropriate Polish error messages
 */
function handleApiError(error: unknown): string | null {
  // If it's already a DailogiError and was displayed, don't show another toast
  if (error instanceof DailogiError && error.displayed) {
    return null;
  }

  // Get error code from response
  let errorCode: string | undefined;
  if (error instanceof DailogiError) {
    errorCode = error.errorData?.code;
  }

  // Default error message
  let errorMsg = "Nieokreślony błąd podczas tworzenia sceny";

  if (errorCode) {
    switch (errorCode) {
      case "CHARACTER_NOT_FOUND":
        errorMsg = "Postać przepadła i jest niedostępna";
        break;
      case "LLM_NOT_FOUND":
        errorMsg = "LLM u steru nie istnieje";
        break;
      case "VALIDATION_ERROR":
        errorMsg = "Nie wszystkie pola są wypełnione poprawnie";
        break;
      default:
        // Use message from errorData if available
        if (error instanceof DailogiError && error.errorData?.message) {
          errorMsg = error.errorData.message;
        }
    }
  }

  return errorMsg;
}

export default function NewSceneForm() {
  const isHydrated = useHydration();
  const { characters, setCharacters, llms, setLlms, phase, isLoading, error, startScene } = useNewScene();

  const form = useForm<NewSceneFormData>({
    resolver: zodResolver(newSceneFormSchema),
    defaultValues: {
      description: "",
      configs: [
        { characterId: undefined, llmId: undefined },
        { characterId: undefined, llmId: undefined },
        { characterId: undefined, llmId: undefined },
      ],
    },
  });

  // Fetch characters and LLMs on component mount
  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch characters
        const charactersResponse = await getAllAvailableCharacters();
        const charactersData: CharacterDropdownDTO[] = charactersResponse.data;

        const characterOptions: CharacterOption[] = Array.isArray(charactersData)
          ? charactersData.map((char) => ({
              id: char.id,
              name: char.name,
              avatarUrl: char.avatar_url || null,
              is_global: char.is_global,
            }))
          : [];
        setCharacters(characterOptions);

        // Fetch LLMs
        const llmsResponse = await getLLMs();
        const llmsData = llmsResponse.data;
        const llmOptions = Array.isArray(llmsData)
          ? llmsData.map((llm) => ({
              id: llm.id,
              name: llm.name,
            }))
          : [];
        setLlms(llmOptions);
      } catch (error) {
        console.error("[NewSceneForm] Loading data failed:", error);

        const errorMsg = handleApiError(error);
        if (errorMsg) {
          toast.error(errorMsg);
        }
      }
    };

    if (isHydrated) {
      fetchData();
    }
  }, [isHydrated, setCharacters, setLlms]);

  const handleStartScene = () => {
    startScene();
  };

  const renderPhaseContent = (phase: FormPhase) => {
    switch (phase) {
      case "config":
        return (
          <>
            <SceneDescriptionInput disabled={isLoading || !isHydrated} />
            <CharacterSelectionList characters={characters} llms={llms} disabled={isLoading || !isHydrated} />
            <div className="flex justify-end mt-6">
              <StartSceneButton
                onClick={form.handleSubmit(handleStartScene)}
                disabled={isLoading || !isHydrated || !form.formState.isValid}
                isLoading={isLoading}
              />
            </div>
          </>
        );
      case "loading":
        return <LoadingIndicator />;
      case "result":
        return (
          <>
            <SceneResult />
            <SaveSceneForm
              defaultName=""
              onSave={(name: string) => console.log("Saving scene with name:", name)}
              disabled={isLoading || !isHydrated}
            />
          </>
        );
      default:
        return null;
    }
  };

  return (
    <div className="max-w-3xl mx-auto">
      <Card>
        <CardContent className="py-6">
          <FormProvider {...form}>
            <Form {...form}>
              <form data-testid="new-scene-form">
                {renderPhaseContent(phase)}
                {error && <div className="text-destructive mt-4 text-sm">{error}</div>}
              </form>
            </Form>
          </FormProvider>
        </CardContent>
      </Card>
    </div>
  );
}
