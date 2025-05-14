import { useState, useRef } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { uploadAvatar, deleteAvatar } from "@/dailogi-api/avatars/avatars";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { DailogiError } from "@/lib/errors/DailogiError";

interface AvatarUploaderProps {
  initialAvatarUrl?: string;
  characterId?: number;
  onAvatarChange: (file: File | null) => void;
}

interface ValidationError {
  type: "size" | "dimensions" | "format";
  message: string;
}

export function AvatarUploader({ initialAvatarUrl, characterId, onAvatarChange }: AvatarUploaderProps) {
  const [previewUrl, setPreviewUrl] = useState<string | null>(initialAvatarUrl || null);
  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const validateFile = (file: File): Promise<ValidationError | null> => {
    return new Promise((resolve) => {
      // Check format
      if (!["image/png", "image/jpeg"].includes(file.type)) {
        resolve({
          type: "format",
          message: "Portret musi być w formacie PNG lub JPEG",
        });
        return;
      }

      // Check size (max 1MB)
      if (file.size > 1024 * 1024) {
        resolve({
          type: "size",
          message: "Portret jest zbyt duży (maksymalnie 1MB)",
        });
        return;
      }

      // Check dimensions
      const img = new Image();
      img.src = URL.createObjectURL(file);
      img.onload = () => {
        URL.revokeObjectURL(img.src);
        if (img.width !== 256 || img.height !== 256) {
          resolve({
            type: "dimensions",
            message: "Portret musi mieć wymiary dokładnie 256x256 pikseli",
          });
        } else {
          resolve(null);
        }
      };
      img.onerror = () => {
        URL.revokeObjectURL(img.src);
        resolve({
          type: "format",
          message: "Nie udało się wczytać portretu",
        });
      };
    });
  };

  const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const validationError = await validateFile(file);
    if (validationError) {
      toast.error(validationError.message);
      onAvatarChange(null);
      return;
    }

    setPreviewUrl(URL.createObjectURL(file));
    onAvatarChange(file);

    if (characterId) {
      setIsUploading(true);
      try {
        const response = await uploadAvatar(characterId, { file });
        setPreviewUrl(response.data.avatar_url || null);
        toast.success("Portret został pomyślnie uwieczniony");
      } catch (err) {
        // Check if error was already handled by the global error handler
        if (err instanceof DailogiError && err.displayed) {
          // Error was already displayed to the user, do nothing
          console.error("Error uploading avatar:", err);
        } else {
          // Show a generic error message
          toast.error("Nie udało się uwiecznić portretu");
        }
      } finally {
        setIsUploading(false);
      }
    }
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  const handleRemove = async () => {
    // If characterId doesn't exist, perform local removal
    if (!characterId) {
      setPreviewUrl(null);
      onAvatarChange(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
      toast.info("Portret został zdematerializowany");
      return;
    }

    setIsUploading(true);
    try {
      await deleteAvatar(characterId);
      setPreviewUrl(null);
      onAvatarChange(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
      toast.success("Portret został zdematerializowany");
    } catch (err) {
      if (err instanceof DailogiError && err.displayed) {
        console.error("Error when dematerializing portrait:", err);
      } else {
        toast.error("Portret oparł się próbom dematerializacji. Spróbuj ponownie później.");
        console.error("Error when dematerializing portrait:", err);
      }
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div className="flex flex-col items-center space-y-4">
      <Avatar className="w-32 h-32">
        <AvatarImage src={previewUrl || undefined} alt="Portret postaci" />
        <AvatarFallback>{previewUrl ? "..." : "Brak"}</AvatarFallback>
      </Avatar>

      <div className="flex space-x-2">
        <Button type="button" variant="outline" onClick={handleClick} disabled={isUploading}>
          {isUploading ? (
            <>
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              Trwa wgrywanie...
            </>
          ) : previewUrl ? (
            "Zmień portret"
          ) : (
            "Wybierz portret"
          )}
        </Button>
        {previewUrl && (
          <Button type="button" variant="destructive" onClick={handleRemove} disabled={isUploading}>
            {isUploading ? <Loader2 className="h-4 w-4 animate-spin" /> : "Zdejmij portret"}
          </Button>
        )}
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/png,image/jpeg"
        onChange={handleFileChange}
        className="hidden"
      />
    </div>
  );
}
