import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

interface CharacterAvatarProps {
  hasAvatar: boolean;
  avatarUrl?: string;
  characterName: string;
  className?: string;
}

export function CharacterAvatar({ hasAvatar, avatarUrl, characterName, className = "" }: CharacterAvatarProps) {
  // Get initials from character name
  const initials = characterName
    .split(" ")
    .map((n) => n[0])
    .join("")
    .toUpperCase()
    .slice(0, 3);

  return (
    <Avatar className={className}>
      {hasAvatar && avatarUrl && <AvatarImage src={avatarUrl} alt={characterName} />}
      <AvatarFallback>{initials}</AvatarFallback>
    </Avatar>
  );
}
