import { Loader2 } from "lucide-react";

export function LoadingIndicator() {
  return (
    <div className="flex flex-col items-center justify-center py-12">
      <Loader2 className="h-12 w-12 animate-spin text-primary mb-4" />
      <p className="text-lg font-medium">Trwa generowanie sceny...</p>
      <p className="text-sm text-muted-foreground mt-2">To może potrwać kilka chwil. Prosimy o cierpliwość.</p>
    </div>
  );
}
