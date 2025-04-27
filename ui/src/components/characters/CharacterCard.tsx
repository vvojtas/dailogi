import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import type { CharacterDTO } from "@/dailogi-api/model";
import { CharacterAvatar } from "@/components/characters/CharacterAvatar";
import { Loader2, Pencil, Trash2 } from "lucide-react";
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
      <Card className="overflow-hidden">
        <CardHeader className="relative">
          <div className="absolute right-4 top-4 flex gap-2">
            {isOwner && (
              <>
                <Button variant="ghost" size="icon" onClick={handleEdit} disabled={isDeleting} title="Edit character">
                  <Pencil className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={handleDelete}
                  disabled={isDeleting}
                  title="Delete character"
                >
                  {isDeleting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Trash2 className="h-4 w-4" />}
                </Button>
              </>
            )}
          </div>
          <div className="flex flex-row items-center gap-4">
            <CharacterAvatar
              hasAvatar={character.has_avatar}
              avatarUrl={character.avatar_url}
              characterName={character.name}
              className="h-16 w-16"
            />
            <CardTitle>{character.name}</CardTitle>
          </div>
        </CardHeader>
        <CardContent>
          <p className="line-clamp-3 text-sm text-muted-foreground">
            {character.description || "No description available."}
          </p>
        </CardContent>
        <CardFooter>
          <Button variant="secondary" className="w-full" onClick={handleViewDetails} disabled={isDeleting}>
            View Details
          </Button>
        </CardFooter>
      </Card>

      <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Character</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete &quot;{character.name}&quot;? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleConfirmDelete}>Delete</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
