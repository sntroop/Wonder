import { useEffect, type ReactNode } from "react";
import { createPortal } from "react-dom";
import { CloseIcon } from "../icons";

type Props = {
  caption: string;
  title: string;
  onClose: () => void;
  closeDisabled?: boolean;
  wide?: boolean;
  className?: string;
  children: ReactNode;
};

/** Общая оболочка диалогов настроек: заголовок, крестик, Escape и клик по фону. */
export function Dialog({
  caption,
  title,
  onClose,
  closeDisabled = false,
  wide = false,
  className = "",
  children,
}: Props) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape" && !closeDisabled) onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [closeDisabled, onClose]);

  return createPortal(
    <div
      className="modal-backdrop"
      onClick={(e) => {
        if (e.target === e.currentTarget && !closeDisabled) onClose();
      }}
    >
      <div
        className={`modal-card st-dialog${wide ? " st-dialog-wide" : ""}${className ? " " + className : ""}`}
        role="dialog"
        aria-modal="true"
        aria-label={title}
      >
        <div className="modal-head">
          <div>
            <div className="caption">{caption}</div>
            <h3>{title}</h3>
          </div>
          <button
            className="icon-btn"
            onClick={onClose}
            disabled={closeDisabled}
            title="Закрыть"
          >
            <CloseIcon />
          </button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
    </div>,
    document.body,
  );
}
