import { useEffect, useId, useState, type FormEvent } from "react";
import { createPortal } from "react-dom";
import { playlists, type AppPlaylist } from "../api";
import { useToast } from "../toast";
import { CloseIcon } from "./icons";

type Props = {
  open: boolean;
  onClose: () => void;
  playlist?: AppPlaylist | null;
  onSaved: (playlist: AppPlaylist) => void;
};

/**
 * Диалог создания/редактирования своего плейлиста.
 *
 * Живёт поверх приложения и переиспользует общие классы модалки
 * (`.modal-backdrop`, `.modal-card`, `.modal-head`, `input.field`),
 * поэтому визуально совпадает с диалогом «В плейлист».
 */
export function PlaylistDialog({ open, onClose, playlist, onSaved }: Props) {
  const toast = useToast((s) => s.show);
  const headingId = useId();
  const titleId = useId();
  const descId = useId();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isPublic, setIsPublic] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const editing = Boolean(playlist);

  useEffect(() => {
    if (!open) return;
    setTitle(playlist?.title || "");
    setDescription(playlist?.description || "");
    setIsPublic(readPublic(playlist));
    setSaving(false);
    setError("");
  }, [open, playlist?.id]);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape" && !saving) onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, saving, onClose]);

  if (!open) return null;

  const close = () => {
    if (saving) return;
    onClose();
  };

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    const name = title.trim();
    if (!name || saving) return;
    setSaving(true);
    setError("");
    try {
      if (playlist) {
        const res = await playlists.update(
          playlist.id,
          name,
          description.trim(),
          isPublic ? 1 : 0,
        );
        if (!res.ok) throw new Error("Не удалось сохранить изменения");
        toast("Сохранено");
        onSaved({ ...playlist, title: name, description: description.trim() });
      } else {
        const created = await playlists.create(
          name,
          description.trim(),
          isPublic ? 1 : 0,
        );
        toast("Плейлист создан");
        onSaved(created);
      }
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Не получилось");
    } finally {
      setSaving(false);
    }
  };

  return createPortal(
    <div
      className="modal-backdrop"
      onClick={(e) => {
        if (e.target === e.currentTarget) close();
      }}
    >
      <div
        className="modal-card pl-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby={headingId}
      >
        <div className="modal-head">
          <div>
            <div className="caption">{editing ? "Редактирование" : "Новый"}</div>
            <h3 id={headingId}>
              {editing ? "Редактировать плейлист" : "Создать плейлист"}
            </h3>
          </div>
          <button
            className="icon-btn"
            onClick={close}
            disabled={saving}
            title="Закрыть"
          >
            <CloseIcon />
          </button>
        </div>

        <div className="modal-body">
          <form className="pl-form" onSubmit={submit}>
            <div className="pl-field">
              <div className="pl-label-row">
                <label className="pl-label" htmlFor={titleId}>
                  Название
                </label>
                <span className="pl-count">
                  {title.length}/120
                </span>
              </div>
              <input
                id={titleId}
                className="field"
                autoFocus
                value={title}
                maxLength={120}
                placeholder="Например, «Ночная поездка»"
                onChange={(e) => setTitle(e.target.value)}
              />
            </div>

            <div className="pl-field">
              <div className="pl-label-row">
                <label className="pl-label" htmlFor={descId}>
                  Описание
                </label>
                <span className="pl-count">
                  {description.length}/400
                </span>
              </div>
              <textarea
                id={descId}
                className="pl-textarea"
                value={description}
                maxLength={400}
                rows={4}
                placeholder="Пара слов о подборке — необязательно"
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            <label className="pl-switch">
              <input
                type="checkbox"
                checked={isPublic}
                onChange={(e) => setIsPublic(e.target.checked)}
              />
              <span className="pl-switch-track" aria-hidden />
              <span className="pl-switch-text">
                <b>Публичный</b>
                <span>
                  {isPublic
                    ? "Виден всем в разделе плейлистов"
                    : "Виден только вам"}
                </span>
              </span>
            </label>

            {error ? (
              <div className="pl-form-error" role="alert">
                {error}
              </div>
            ) : null}

            <div className="pl-form-actions">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={close}
                disabled={saving}
              >
                Отмена
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={saving || !title.trim()}
              >
                {saving
                  ? "Сохраняю…"
                  : editing
                    ? "Сохранить"
                    : "Создать"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>,
    document.body,
  );
}

/** Публичность не описана в AppPlaylist — читаем поля, если сервер их прислал. */
function readPublic(playlist?: AppPlaylist | null): boolean {
  if (!playlist) return true;
  const extra = playlist as AppPlaylist & {
    public?: number | boolean;
    is_public?: boolean;
  };
  if (typeof extra.public !== "undefined") return Boolean(Number(extra.public));
  if (typeof extra.is_public !== "undefined") return Boolean(extra.is_public);
  return true;
}
