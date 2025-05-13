import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader2, Save } from "lucide-react";
import { toast } from "sonner";

const saveSceneSchema = z.object({
  name: z.string().min(1, "Nazwa jest wymagana").max(100, "Nazwa nie może przekraczać 100 znaków"),
});

type SaveSceneFormData = z.infer<typeof saveSceneSchema>;

interface SaveSceneFormProps {
  defaultName: string;
  onSave: (name: string) => void;
  disabled?: boolean;
}

export function SaveSceneForm({ defaultName, onSave, disabled = false }: Readonly<SaveSceneFormProps>) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const form = useForm<SaveSceneFormData>({
    resolver: zodResolver(saveSceneSchema),
    defaultValues: {
      name: defaultName,
    },
  });

  const handleSubmit = async (data: SaveSceneFormData) => {
    try {
      setIsSubmitting(true);
      onSave(data.name);
      toast.success("Scena została zapisana");
    } catch (error) {
      toast.error("Nie udało się zapisać sceny");
      console.error(error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mt-8 border-t pt-6">
      <h3 className="text-lg font-medium mb-4">Zapisz scenę</h3>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
          <FormField
            control={form.control}
            name="name"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Nazwa sceny</FormLabel>
                <FormControl>
                  <Input
                    placeholder="Wprowadź nazwę sceny..."
                    {...field}
                    disabled={disabled || isSubmitting}
                    data-testid="scene-name-input"
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <Button type="submit" disabled={disabled || isSubmitting} data-testid="save-scene-button">
            {isSubmitting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Zapisywanie...
              </>
            ) : (
              <>
                <Save className="mr-2 h-4 w-4" />
                Zapisz scenę
              </>
            )}
          </Button>
        </form>
      </Form>
    </div>
  );
}
