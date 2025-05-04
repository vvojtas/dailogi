import { useState, useEffect } from "react";

/**
 * Hook to track component hydration state
 * Useful for disabling form elements until hydration completes
 */
export function useHydration() {
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    setIsHydrated(true);
  }, []);

  return isHydrated;
}
