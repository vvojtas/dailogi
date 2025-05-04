import { useForm, FormProvider } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import type { Llmdto } from "@/dailogi-api/model";
import type { CharacterDTO } from "@/dailogi-api/model/characterDTO";
import { Form } from "@/components/ui/form";
import { AvatarUploader } from "@/components/characters/AvatarUploader";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { characterFormSchema, type CharacterFormData } from "@/lib/validation/characterSchema";
import { useHydration } from "@/lib/hooks/useHydration";
import { useCharacterForm } from "@/lib/hooks/useCharacterForm";
import { CharacterNameField } from "./CharacterNameField";
import { CharacterDescriptionFields } from "./CharacterDescriptionFields";
import { CharacterLlmField } from "./CharacterLlmField";
import { CharacterFormButtons } from "./CharacterFormButtons";

interface CharacterFormProps {
  llms: Llmdto[];
  initialData?: CharacterDTO | null;
  onSubmitSuccess: (character: CharacterDTO) => void;
  onCancel: () => void;
}

export function CharacterForm({ llms, initialData, onSubmitSuccess, onCancel }: CharacterFormProps) {
  const isHydrated = useHydration();
  const { isSubmitting, createNewCharacter, updateExistingCharacter } = useCharacterForm({
    onSuccess: onSubmitSuccess,
  });

  const form = useForm<CharacterFormData>({
    resolver: zodResolver(characterFormSchema),
    defaultValues: {
      name: initialData?.name || "",
      short_description: initialData?.short_description || "",
      description: initialData?.description || "",
      default_llm_id: initialData?.default_llm_id?.toString() || undefined,
    },
  });

  const onSubmit = async (data: CharacterFormData) => {
    if (initialData) {
      await updateExistingCharacter(initialData.id, data);
    } else {
      await createNewCharacter(data);
    }
  };

  const handleAvatarChange = (file: File | null) => {
    form.setValue("avatar", file || undefined);
  };

  const isDisabled = isSubmitting || !isHydrated;
  const isEditMode = !!initialData;

  return (
    <div className="max-w-2xl mx-auto">
      <Card>
        <FormProvider {...form}>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} data-testid="character-form">
              <CardHeader>
                <CardTitle>{isEditMode ? "Odmień postać" : "Powołaj nową postać"}</CardTitle>
              </CardHeader>

              <CardContent className="space-y-6">
                <div className="flex justify-center mb-6">
                  <AvatarUploader
                    initialAvatarUrl={initialData?.avatar_url}
                    characterId={initialData?.id}
                    onAvatarChange={handleAvatarChange}
                  />
                </div>

                <CharacterNameField disabled={isDisabled} />
                <CharacterDescriptionFields disabled={isDisabled} />
                <CharacterLlmField llms={llms} disabled={isDisabled} />
              </CardContent>

              <CardFooter className="flex justify-end">
                <CharacterFormButtons
                  isSubmitting={isSubmitting}
                  isHydrated={isHydrated}
                  isEditMode={isEditMode}
                  onCancel={onCancel}
                />
              </CardFooter>
            </form>
          </Form>
        </FormProvider>
      </Card>
    </div>
  );
}
