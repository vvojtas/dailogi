import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import type { CharacterDTO } from "@/dailogi-api/model";
import { CharacterAvatar } from "@/components/characters/CharacterAvatar";
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
import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { useLlms } from "@/lib/hooks/useLlms";

interface CharacterCardProps {
  character: CharacterDTO;
  isOwner: boolean;
  isDeleting: boolean;
  onEdit: (characterId: number) => void;
  onDelete: (character: CharacterDTO) => void;
  onViewDetails: (characterId: number) => void;
}

export function CharacterCard({ character, isOwner, isDeleting, onEdit, onDelete, onViewDetails }: CharacterCardProps) {
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const { getLlmName, isLoading: isLoadingLlms } = useLlms();

  const handleEdit = () => {
    onEdit(character.id);
  };

  const handleDelete = () => {
    setIsDeleteDialogOpen(true);
  };

  const handleConfirmDelete = () => {
    setIsDeleteDialogOpen(false);
    onDelete(character);
  };

  const handleViewDetails = () => {
    onViewDetails(character.id);
  };

  return (
    <>
      <Card
        className={`overflow-hidden ${character.is_global ? "bg-secondary/50 border-secondary" : ""} ${!isOwner ? "bg-muted/25 border-muted-foreground/20" : ""}`}
      >
        <CardHeader className="relative">
          <div className="absolute right-4 top-0 flex gap-2">
            {character.is_global ? (
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                <BookOpen className="h-3 w-3" />
                <span>Katalog główny</span>
              </div>
            ) : (
              isOwner && (
                <>
                  <Button variant="ghost" size="icon" onClick={handleEdit} disabled={isDeleting} title="Odmień postać">
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
              )
            )}
          </div>
          <div className="flex flex-row items-center gap-4">
            <CharacterAvatar
              hasAvatar={character.has_avatar}
              avatarUrl={character.avatar_url}
              characterName={character.name}
              className="h-16 w-16"
            />
            <div className="flex flex-col gap-1">
              <CardTitle>{character.name}</CardTitle>
              <div className="flex flex-wrap items-center gap-1">
                {character.default_llm_id && (
                  <>
                    {isLoadingLlms ? (
                      <Loader2 className="h-3 w-3 animate-spin text-muted-foreground" />
                    ) : (
                      <Badge variant="secondary" className="text-xs">
                        {getLlmName(character.default_llm_id)}
                      </Badge>
                    )}
                  </>
                )}
              </div>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <p className="h-24 overflow-y-auto text-sm text-muted-foreground scrollbar-thin scrollbar-thumb-secondary scrollbar-track-transparent">
            {character.short_description || "Ta postać owiana jest tajemnicą."}
          </p>
        </CardContent>
        <CardFooter>
          <Button variant="secondary" className="w-full" onClick={handleViewDetails} disabled={isDeleting}>
            Sprawdź profil
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
