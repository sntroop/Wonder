import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import {
  ApiError,
  music,
  playlists,
  type AppPlaylist,
  type Track,
} from "../api";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { Cover } from "../components/Cover";
import { PlaylistDialog } from "../components/PlaylistDialog";
import { TrackRow } from "../components/TrackRow";
import {
  ChevronLeft,
  CloseIcon,
  PlayIcon,
  PlaylistIcon,
} from "../components/icons";

export function PlaylistPage() {
  const { owner = "", id = "" } = useParams();
  const [params] = useSearchParams();
  const accessKey = params.get("access_key") || "";
  const isOwn = owner === "w";

  const navigate = useNavigate();
  const play = usePlayer((s) => s.play);
  const toast = useToast((s) => s.show);

  const [data, setData] = useState<AppPlaylist | null>(null);
  const [tracks, setTracks] = useState<Track[] | null>(null);
  const [error, setError] = useState("");
  const [status, setStatus] = useState(0);
  const [editOpen, setEditOpen] = useState(false);
  const [removing, setRemoving] = useState("");
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    let alive = true;
    setData(null);
    setTracks(null);
    setError("");
    setStatus(0);
    setEditOpen(false);
    setRemoving("");

    (async () => {
      try {
        if (isOwn) {
          const p = await playlists.get(id);
          if (!alive) return;
          setData(p);
          setTracks(p.tracks || []);
        } else {
          const res = await music.playlist(owner, id, accessKey, 300);
          if (!alive) return;
          setTracks(res.items || []);
        }
      } catch (e) {
        if (!alive) return;
        setTracks([]);
        setStatus(e instanceof ApiError ? e.status : 0);
        setError(e instanceof Error ? e.message : "Плейлист не открылся");
      }
    })();

    return () => {
      alive = false;
    };
  }, [owner, id, accessKey, isOwn]);

  const list = tracks || [];
  const count = list.length;
  const total = list.reduce((acc, t) => acc + (t.duration || 0), 0);
  const minutes = Math.round(total / 60);

  const isMine = isOwn && Boolean(data?.is_mine);
  const cover = (isOwn ? data?.cover : "") || list[0]?.artwork || "";
  const author = isOwn ? data?.author || "" : list[0]?.author || "";
  const title = isOwn ? data?.title || "Плейлист" : "Плейлист";
  const description = isOwn ? data?.description || "" : "";

  const removeTrack = async (t: Track, key: string) => {
    if (!data || removing) return;
    setRemoving(key);
    try {
      const res = await playlists.remove(data.id, t);
      if (!res.ok) throw new Error("Не удалось убрать трек");
      setTracks((prev) =>
        (prev || []).filter(
          (x) => !(x.source === t.source && x.id === t.id),
        ),
      );
      setData((prev) =>
        prev
          ? { ...prev, count: Math.max(0, (prev.count || 0) - 1) }
          : prev,
      );
      toast("Удалён из плейлиста");
    } catch (e) {
      toast(e instanceof Error ? e.message : "Не получилось");
    } finally {
      setRemoving("");
    }
  };

  const destroy = async () => {
    if (!data || deleting) return;
    if (!window.confirm(`Удалить плейлист «${title}»?`)) return;
    setDeleting(true);
    try {
      const res = await playlists.destroy(data.id);
      if (!res.ok) throw new Error("Не удалось удалить плейлист");
      toast("Плейлист удалён");
      navigate("/playlists");
    } catch (e) {
      toast(e instanceof Error ? e.message : "Не получилось");
      setDeleting(false);
    }
  };

  return (
    <div className="page page-enter">
      <button className="pl-back" onClick={() => navigate(-1)}>
        <ChevronLeft /> Назад
      </button>

      {!tracks && !error ? (
        <>
          <div className="pl-skeleton-hero" aria-hidden>
            <div className="skeleton pl-skeleton-cover" />
            <div className="pl-skeleton-body">
              <div className="skeleton" style={{ height: 14, width: 90 }} />
              <div className="skeleton" style={{ height: 42, width: "58%" }} />
              <div className="skeleton" style={{ height: 14, width: 220 }} />
              <div className="skeleton" style={{ height: 46, width: 280 }} />
            </div>
          </div>
          <div className="pl-rows" aria-hidden>
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="skeleton" style={{ height: 64 }} />
            ))}
          </div>
        </>
      ) : error ? (
        <div className="empty pl-empty">
          <h3>
            {status === 404 ? "Плейлист не найден" : "Плейлист не открылся"}
          </h3>
          <div>{error}</div>
          <button
            className="btn btn-ghost"
            onClick={() => navigate("/playlists")}
          >
            К плейлистам
          </button>
        </div>
      ) : (
        <>
          <div className="pl-hero">
            <div className="pl-hero-cover">
              {cover ? (
                <Cover src={cover} size={700} />
              ) : (
                <div className="pl-hero-empty">
                  <PlaylistIcon />
                </div>
              )}
            </div>
            <div className="pl-hero-body">
              <div className="eyebrow">
                {isOwn ? "Плейлист" : "Плейлист VK"}
              </div>
              <h1 className="page-title pl-hero-title">{title}</h1>
              <div className="pl-hero-sub">
                {count} треков · {minutes} мин
              </div>
              {author ? (
                <div className="pl-hero-author">{author}</div>
              ) : null}
              {description ? (
                <p className="pl-hero-desc">{description}</p>
              ) : null}
              <div className="pl-actions">
                <button
                  className="btn btn-primary"
                  disabled={!count}
                  onClick={() => play(list, 0, "playlist")}
                >
                  <PlayIcon /> Слушать
                </button>
                {isMine ? (
                  <>
                    <button
                      className="btn btn-ghost"
                      onClick={() => setEditOpen(true)}
                    >
                      Изменить
                    </button>
                    <button
                      className="btn btn-ghost pl-danger"
                      disabled={deleting}
                      onClick={destroy}
                    >
                      {deleting ? "Удаляю…" : "Удалить"}
                    </button>
                  </>
                ) : null}
              </div>
            </div>
          </div>

          {count ? (
            <div className="pl-rows">
              {list.map((t, i) => {
                const key = `${t.source}-${t.id}-${i}`;
                if (!isMine) {
                  return (
                    <TrackRow
                      key={key}
                      track={t}
                      index={i}
                      queue={list}
                      onToast={toast}
                    />
                  );
                }
                return (
                  <div className="pl-row" key={key}>
                    <TrackRow
                      track={t}
                      index={i}
                      queue={list}
                      onToast={toast}
                    />
                    <button
                      className="pl-remove"
                      title="Убрать из плейлиста"
                      aria-label="Убрать из плейлиста"
                      disabled={Boolean(removing)}
                      onClick={(e) => {
                        e.stopPropagation();
                        void removeTrack(t, key);
                      }}
                    >
                      {removing === key ? (
                        <span className="spinner small" />
                      ) : (
                        <CloseIcon />
                      )}
                    </button>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="empty pl-empty">
              <h3>В плейлисте пока пусто</h3>
              <div className="hint">
                Добавить трек можно из меню «...» в любой строке — «В плейлист».
              </div>
            </div>
          )}
        </>
      )}

      {isMine ? (
        <PlaylistDialog
          open={editOpen}
          onClose={() => setEditOpen(false)}
          playlist={data}
          onSaved={(p) =>
            setData((prev) => (prev ? { ...prev, ...p } : p))
          }
        />
      ) : null}
    </div>
  );
}
