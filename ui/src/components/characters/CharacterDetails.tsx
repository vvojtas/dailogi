import { useState } from "react";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { BookOpen, Loader2, Pencil, Trash2 } from "lucide-react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import type { CharacterDTO, Llmdto } from "@/dailogi-api/model";
import { CharacterAvatar } from "@/components/characters/CharacterAvatar";
import { navigate } from "@/lib/client/navigate";
import { ROUTES } from "@/lib/config/routes";

interface CharacterDetailsProps {
  character: CharacterDTO;
  llms: Llmdto[];
  isDeleting: boolean;
  onEdit: () => void;
  onDelete: (character: CharacterDTO) => void;
}

export function CharacterDetails({ character, llms, isDeleting, onEdit, onDelete }: CharacterDetailsProps) {
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const handleDelete = () => {
    setIsDeleteDialogOpen(true);
  };

  const handleConfirmDelete = () => {
    setIsDeleteDialogOpen(false);
    onDelete(character);
  };

  const getLlmName = (id: number) => {
    const llm = llms.find((l) => l.id === id);
    return llm?.name || "Nieznany model";
  };

  return (
    <>
      <Card className={character.is_global ? "bg-secondary/50 border-secondary" : ""}>
        <CardHeader className="relative">
          <div className="absolute right-4 top-4 flex gap-2">
            {character.is_global ? (
              <div className="flex items-center gap-1 text-muted-foreground">
                <BookOpen className="h-4 w-4" />
                <span>Katalog główny</span>
              </div>
            ) : (
              <>
                <Button variant="ghost" size="icon" onClick={onEdit} disabled={isDeleting} title="Odmień postać">
                  <Pencil className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={handleDelete}
                  disabled={isDeleting}
                  title="Zlikwiduj postać"
                >
                  {isDeleting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Trash2 className="h-4 w-4" />}
                </Button>
              </>
            )}
          </div>

          <div className="flex flex-row items-center gap-6">
            <CharacterAvatar
              hasAvatar={character.has_avatar}
              avatarUrl={character.avatar_url}
              characterName={character.name}
              className="h-24 w-24"
            />
            <div className="flex flex-col gap-2">
              <CardTitle className="text-2xl">{character.name}</CardTitle>
              <div className="flex flex-wrap items-center gap-2">
                {character.default_llm_id && <Badge variant="secondary">{getLlmName(character.default_llm_id)}</Badge>}
              </div>
              <p className="text-sm text-muted-foreground">{character.short_description}</p>
            </div>
          </div>
        </CardHeader>

        <CardContent>
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold mb-2">Biografia</h3>
              <div className="max-h-96 overflow-y-auto scrollbar-thin scrollbar-thumb-secondary scrollbar-track-transparent">
                <p className="text-muted-foreground whitespace-pre-wrap">
                  {character.description || "Ta postać owiana jest tajemnicą."}
                </p>
              </div>
            </div>
          </div>
        </CardContent>

        <CardFooter className="flex justify-end">
          <Button variant="outline" onClick={() => navigate(ROUTES.CHARACTERS)}>
            Powrót do galerii
          </Button>
        </CardFooter>
      </Card>

      <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Zlikwiduj postać</AlertDialogTitle>
            <AlertDialogDescription>
              Czy na pewno chcesz zlikwidować postać &quot;{character.name}&quot;? Nie da się jej później uratować.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Oszczędź</AlertDialogCancel>
            <AlertDialogAction onClick={handleConfirmDelete}>Zlikwiduj</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
