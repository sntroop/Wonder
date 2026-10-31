import { useState } from "react";
import { imgUrl } from "../api";
import { NoteIcon } from "./icons";
import { cx } from "../utils";

type Props = {
  src?: string | null;
  size?: number;
  className?: string;
  rounded?: boolean;
};

/** Обложка с заглушкой: внешние URL идут через прокси бэкенда. */
export function Cover({ src, size = 400, className, rounded }: Props) {
  const [failed, setFailed] = useState(false);
  const url = imgUrl(src, size);
  if (!url || failed) {
    return (
      <div className={cx(className, "cover-fallback")} data-rounded={rounded}>
        <NoteIcon />
      </div>
    );
  }
  return (
    <img
      className={className}
      src={url}
      alt=""
      loading="lazy"
      decoding="async"
      onError={() => setFailed(true)}
    />
  );
}
