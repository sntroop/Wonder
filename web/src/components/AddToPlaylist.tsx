import { useEffect, useMemo, useState } from "react";
import { create } from "zustand";
import { playlists, type AppPlaylist, type Track } from "../api";
import { useToast } from "../toast";
import { Cover } from "./Cover";
import { CheckIcon, CloseIcon, PlaylistIcon, PlusIcon } from "./icons";

type AddState = {
  track: Track | null;
  open: (track: Track) => void;
  close: () => void;
};

/** Один диалог «добавить в плейлист» на всё приложение. */
export const useAddToPlaylist = create<AddState>((set) => ({
  track: null,
  open: (track) => set({ track }),
  close: () => set({ track: null }),
}));

export function AddToPlaylistDialog() {
  const track = useAddToPlaylist((s) => s.track);
  const close = useAddToPlaylist((s) => s.close);
  const toast = useToast((s) => s.show);
  const [items, setItems] = useState<AppPlaylist[] | null>(null);
  const [busy, setBusy] = useState<string>("");
  const [adding, setAdding] = useState(false);
  const [title, setTitle] = useState("");
  const [createOpen, setCreateOpen] = useState(false);

  useEffect(() => {
    if (!track) return;
    setItems(null);
    setCreateOpen(false);
    setTitle("");
    let alive = true;
    playlists
      .mine()
      .then((r) => alive && setItems(r.items || []))
      .catch(() => alive && setItems([]));
    return () => {
      alive = false;
    };
  }, [track]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") close();
    };
    if (track) window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [track, close]);

  const sorted = useMemo(
    () => (items || []).slice().sort((a, b) => (b.count || 0) - (a.count || 0)),
    [items],
  );

  if (!track) return null;

  const addTo = async (p: AppPlaylist) => {
    if (adding) return;
    setAdding(true);
    setBusy(String(p.id));
    try {
      const res = await playlists.add(p.id, track);
      if (res.ok) {
        toast(`Добавлено в «${p.title}»`);
        close();
      } else {
        toast("Не получилось");
      }
    } catch (e) {
      toast(e instanceof Error ? e.message : "Не получилось");
    } finally {
      setAdding(false);
      setBusy("");
    }
  };

  const create = async () => {
    const name = title.trim();
    if (!name || adding) return;
    setAdding(true);
    try {
      const p = await playlists.create(name);
      await playlists.add(p.id, track);
      toast(`Создан «${name}»`);
      close();
    } catch (e) {
      toast(e instanceof Error ? e.message : "Не получилось");
    } finally {
      setAdding(false);
    }
  };

  return (
    <div
      className="modal-backdrop"
      onClick={(e) => {
        if (e.target === e.currentTarget) close();
      }}
    >
      <div className="modal-card">
        <div className="modal-head">
          <div>
            <div className="caption">Добавить трек</div>
            <h3>{track.title}</h3>
            <div className="hint">{track.artist}</div>
          </div>
          <button className="icon-btn" onClick={close} title="Закрыть">
            <CloseIcon />
          </button>
        </div>

        <div className="modal-body">
          {createOpen ? (
            <div className="create-pl">
              <input
                className="field"
                autoFocus
                value={title}
                maxLength={120}
                placeholder="Название плейлиста"
                onChange={(e) => setTitle(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") void create();
                }}
              />
              <button className="btn btn-primary" disabled={!title.trim() || adding} onClick={create}>
                {adding ? "Создаю…" : "Создать и добавить"}
              </button>
            </div>
          ) : (
            <button className="ap-new" onClick={() => setCreateOpen(true)}>
              <span className="ap-icon">
                <PlusIcon />
              </span>
              <span>Новый плейлист</span>
            </button>
          )}

          {items === null ? (
            <div className="ap-list">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="skeleton" style={{ height: 60 }} />
              ))}
            </div>
          ) : sorted.length ? (
            <div className="ap-list">
              {sorted.map((p) => (
                <button
                  key={p.id}
                  className="ap-row"
                  disabled={adding}
                  onClick={() => addTo(p)}
                >
                  <span className="ap-cover">
                    {p.cover ? (
                      <Cover src={p.cover} size={120} />
                    ) : (
                      <PlaylistIcon />
                    )}
                  </span>
                  <span className="ap-info">
                    <b>{p.title}</b>
                    <span>
                      {p.count || 0} треков
                      {p.description ? ` · ${p.description}` : ""}
                    </span>
                  </span>
                  {busy === String(p.id) ? (
                    <span className="spinner small" />
                  ) : (
                    <CheckIcon className="ap-check" />
                  )}
                </button>
              ))}
            </div>
          ) : (
            <div className="empty" style={{ padding: "22px 0" }}>
              Плейлистов ещё нет — создай первый.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
