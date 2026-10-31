import { cx } from "../utils";

/**
 * 4 столбика, которые «дышат» — порт EqBars.kt.
 * Анимация всегда идёт, пока показан элемент; фазы как в оригинале.
 */
export function EqBars({
  playing = true,
  className,
  size = 14,
}: {
  playing?: boolean;
  className?: string;
  size?: number;
}) {
  return (
    <span
      className={cx("eq", playing && "on", className)}
      style={{ height: size, width: size }}
      aria-hidden
    >
      <i style={{ animationDelay: "0s" }} />
      <i style={{ animationDelay: "-0.45s" }} />
      <i style={{ animationDelay: "-0.18s" }} />
      <i style={{ animationDelay: "-0.62s" }} />
    </span>
  );
}
