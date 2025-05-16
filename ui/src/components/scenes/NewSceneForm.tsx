import { useEffect } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";
import { Card, CardContent } from "@/components/ui/card";
import { SceneDescriptionInput } from "./SceneDescriptionInput.tsx";
import { CharacterSelectionList } from "./CharacterSelectionList.tsx";
import { StartSceneButton } from "./StartSceneButton.tsx";
import { SceneResult } from "./SceneResult.tsx";
import { useNewScene } from "@/lib/hooks/useNewScene";
import { newSceneFormSchema, type NewSceneFormData } from "@/lib/validation/sceneSchema";
import { useHydration } from "@/lib/hooks/useHydration";
import { getAllAvailableCharacters } from "@/dailogi-api/characters/characters";
import { getLLMs } from "@/dailogi-api/llm/llm";
import { toast } from "sonner";
import type { CharacterDropdownDTO } from "@/dailogi-api/model/characterDropdownDTO";
import { handleSceneApiError } from "@/lib/utils/errorHandlers/sceneErrors";

export default function NewSceneForm() {
  const isHydrated = useHydration();
  const { characters, setCharacters, llms, setLlms, phase, isLoading, hasError, startScene, dialogueEvents } =
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

  return (
    <div className="max-w-3xl mx-auto">
      <Card>
        <CardContent className="py-6">
          <FormProvider {...form}>
            <Form {...form}>
              <form data-testid="new-scene-form">
                <SceneDescriptionInput disabled={isLoading || !isHydrated || phase !== "config"} phase={phase} />

                <CharacterSelectionList
                  characters={characters}
                  llms={llms}
                  disabled={isLoading || !isHydrated || phase !== "config"}
                  phase={phase}
                />

                {phase === "config" && (
                  <div className="flex justify-end mt-6">
                    <StartSceneButton
                      onClick={form.handleSubmit(handleStartScene)}
                      disabled={isLoading || !isHydrated || !form.formState.isValid}
                      isLoading={isLoading}
                    />
                  </div>
                )}

                {(phase === "loading" || phase === "result") && (
                  <>
                    <SceneResult dialogueEvents={dialogueEvents} characters={characters} llms={llms} />
                  </>
                )}

                {hasError && (
                  <div className="text-destructive mt-4 text-sm">
                    Wystąpił błąd podczas generowania dialogu. Zobacz powiadomienia, aby poznać szczegóły.
                  </div>
                )}
              </form>
            </Form>
          </FormProvider>
        </CardContent>
      </Card>
    </div>
  );
}
