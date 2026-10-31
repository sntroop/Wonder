import { useEffect, useState } from "react";
import { tintOf } from "./tint";

/** Цвет текущей обложки (null = ч/б или ещё не посчитали). */
export function useArtworkTint(art?: string | null): string | null {
  const [color, setColor] = useState<string | null>(null);

  useEffect(() => {
    let alive = true;
    if (!art) {
      setColor(null);
      return;
    }
    tintOf(art).then((c) => {
      if (alive) setColor(c);
    });
    return () => {
      alive = false;
    };
  }, [art]);

  return color;
}
