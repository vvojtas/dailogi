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
import { useNewScene, type FormPhase } from "@/lib/hooks/useNewScene";
import { newSceneFormSchema, type NewSceneFormData } from "@/lib/validation/sceneSchema";
import { useHydration } from "@/lib/hooks/useHydration";
import { getAllAvailableCharacters } from "@/dailogi-api/characters/characters";
import { getLLMs } from "@/dailogi-api/llm/llm";
import { toast } from "sonner";
import type { CharacterDropdownDTO } from "@/dailogi-api/model/characterDropdownDTO";
import { handleSceneApiError } from "@/lib/utils/errorHandlers/sceneErrors";

export default function NewSceneForm() {
  const isHydrated = useHydration();
  const { characters, setCharacters, llms, setLlms, phase, isLoading, error, startScene, dialogueEvents } =
    useNewScene();

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
        setCharacters(charactersData);

        // Fetch LLMs
        const llmsResponse = await getLLMs();
        const llmsData = llmsResponse.data;
        setLlms(Array.isArray(llmsData) ? llmsData : []);
      } catch (error) {
        console.error("[NewSceneForm] Loading data failed:", error);

        const errorMsg = handleSceneApiError(error);
        if (errorMsg) {
          toast.error(errorMsg);
        }
      }
    };

    if (isHydrated) {
      fetchData();
    }
  }, [isHydrated, setCharacters, setLlms]);

  const handleStartScene = (formData: NewSceneFormData) => {
    console.log("Starting scene with form data:", formData);
    startScene(formData);
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
            <SceneResult dialogueEvents={dialogueEvents} characters={characters} />
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
