import { useEffect, useState } from "react";
import { feedback, music, social, streamUrl } from "../api";
import { useAddToPlaylist } from "./AddToPlaylist";
import { CommentsPanel, LyricsPanel, RemixesPanel } from "./TrackPanels";
import { FxDialog } from "./FxDialog";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { downloadTrack, fmtTime } from "../utils";
import { Cover } from "./Cover";
import {
  ChevronLeft,
  CloseIcon,
  DislikeIcon,
  DownloadIcon,
  FxIcon,
  HeartIcon,
  NextIcon,
  PauseIcon,
  PlayIcon,
  PlaylistIcon,
  PlusIcon,
  CommentIcon,
  MuteIcon,
  QueueIcon,
  VolumeIcon,
  PrevIcon,
  RemixIcon,
  ShareIcon,
  TextIcon,
  WaveIcon,
} from "./icons";

function useSeek(onSeek: (value: number) => void, max: number) {
  const [el, setEl] = useState<HTMLDivElement | null>(null);
  const [drag, setDrag] = useState(false);

  const at = (clientX: number) => {
    if (!el) return 0;
    const rect = el.getBoundingClientRect();
    return Math.max(0, Math.min(1, (clientX - rect.left) / rect.width)) * max;
  };

  return {
    ref: setEl,
    onPointerDown: (e: React.PointerEvent) => {
      (e.target as HTMLElement).setPointerCapture?.(e.pointerId);
      setDrag(true);
      onSeek(at(e.clientX));
    },
    onPointerMove: (e: React.PointerEvent) => {
      if (drag) onSeek(at(e.clientX));
    },
    onPointerUp: () => setDrag(false),
    onPointerCancel: () => setDrag(false),
  };
}

/** Хватает ли места, чтобы показать текст справа от обложки. */
function useWide(min = 1100) {
  const [wide, setWide] = useState(
    () => typeof window !== "undefined" && window.matchMedia(`(min-width: ${min}px)`).matches,
  );
  useEffect(() => {
    const mq = window.matchMedia(`(min-width: ${min}px)`);
    const onChange = () => setWide(mq.matches);
    onChange();
    mq.addEventListener("change", onChange);
    return () => mq.removeEventListener("change", onChange);
  }, [min]);
  return wide;
}

export function PlayerSheet() {
  const open = usePlayer((s) => s.sheetOpen);
  const setOpen = usePlayer((s) => s.setSheetOpen);
  const track = usePlayer((s) => s.current());
  const playing = usePlayer((s) => s.playing);
  const time = usePlayer((s) => s.time);
  const duration = usePlayer((s) => s.duration);
  const buffered = usePlayer((s) => s.buffered);
  const loading = usePlayer((s) => s.loading);
  const playError = usePlayer((s) => s.error);
  const toggle = usePlayer((s) => s.toggle);
  const next = usePlayer((s) => s.next);
  const prev = usePlayer((s) => s.prev);
  const seek = usePlayer((s) => s.seek);
  const toast = useToast((s) => s.show);
  const volume = usePlayer((s) => s.volume);
  const muted = usePlayer((s) => s.muted);
  const setVolume = usePlayer((s) => s.setVolume);
  const toggleMute = usePlayer((s) => s.toggleMute);
  const setQueueOpen = usePlayer((s) => s.setQueueOpen);
  const queueOpen = usePlayer((s) => s.queueOpen);
  const [liked, setLiked] = useState(false);
  const [likeReady, setLikeReady] = useState(false);
  const [disliked, setDisliked] = useState(false);
  const [fxOpen, setFxOpen] = useState(false);
  const [view, setView] = useState<"main" | "lyrics" | "remixes" | "comments">("main");
  const openAdd = useAddToPlaylist((s) => s.open);
  const wide = useWide(1100);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setOpen(false);
    };
    if (open) window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, setOpen]);

  useEffect(() => {
    setView("main");
  }, [track?.source, track?.id]);

  useEffect(() => {
    if (!open) setView("main");
  }, [open]);

  useEffect(() => {
    if (!track) return;
    let alive = true;
    setLiked(false);
    setLikeReady(false);
    setDisliked(false);
    social
      .get(track)
      .then((r) => {
        if (alive) {
          setLiked(Boolean(r.liked));
          setLikeReady(true);
        }
      })
      .catch(() => {
        if (alive) setLikeReady(true);
      });
    return () => {
      alive = false;
    };
  }, [track?.source, track?.id]);

  const likeTrack = () => {
    if (!track) return;
    music
      .like(track)
      .then((r) => {
        setLiked(Boolean(r.liked));
        if (r.liked) setDisliked(false);
        toast(r.liked ? "В любимых" : "Убрано из любимых");
      })
      .catch(() => toast("Не получилось"));
  };

  const dislikeTrack = () => {
    if (!track) return;
    feedback(track, "dislike")
      .then(() => {
        setDisliked(true);
        setLiked(false);
        toast("Понял, такое реже");
      })
      .catch(() => toast("Не получилось"));
  };

  const total = duration || (track?.duration ?? 0);
  const handlers = useSeek(seek, total);

  if (!open || !track) return null;
  const pct = total ? Math.min(100, (time / total) * 100) : 0;
  const buf = total ? Math.min(100, (buffered / total) * 100) : 0;
  // текст песни на широком экране живёт справа от плеера, а не поверх него;
  // на узком — прежняя панель на весь лист
  const sideLyrics = wide && view === "lyrics";
  const overlayPanel = view !== "main" && !sideLyrics;

  return (
    <div
      className="sheet-backdrop"
      onClick={(e) => {
        if (e.target === e.currentTarget) setOpen(false);
      }}
    >
      <div
        className={
          "sheet" +
          (overlayPanel ? " panel-open" : "") +
          (sideLyrics ? " with-side" : "")
        }
      >
        <div className="sheet-main">
          <div className="sheet-head">
            <button className="icon-btn" onClick={() => setOpen(false)} title="Свернуть">
              <ChevronLeft style={{ transform: "rotate(-90deg)" }} />
            </button>
            <div className="caption">Сейчас играет</div>
            <button
              className="icon-btn"
              title="Поделиться"
              onClick={() => {
                const url = `${location.origin}/wonder/?t=${track.source}:${track.id}`;
                navigator.clipboard?.writeText(url).then(
                  () => toast("Ссылка скопирована"),
                  () => toast(url),
                );
              }}
            >
              <ShareIcon />
            </button>
          </div>

          <div className="sheet-art">
            <Cover src={track.artwork} size={800} />
            <div className="sheet-nav">
              <button onClick={prev} title="Назад">
                <PrevIcon />
              </button>
              <button onClick={next} title="Дальше">
                <NextIcon />
              </button>
            </div>
          </div>

          <h2 className="sheet-title">{track.title}</h2>
          <div className="sheet-artist">
            {track.artist}
            <ChevronLeft style={{ transform: "rotate(180deg)" }} />
          </div>

          <div className="sheet-actions">
            <button
              className="sheet-action"
              onClick={() => {
                music
                  .wave(40, true)
                  .then((r) => {
                    if (r.items?.length) {
                      usePlayer.getState().play(r.items, 0);
                      toast("Волна запущена");
                    } else toast("Волна пустая");
                  })
                  .catch(() => toast("Волна не отвечает"));
              }}
            >
              <WaveIcon />
              Волна
            </button>
            <button className="sheet-action" onClick={() => openAdd(track)}>
              <PlaylistIcon />
              Плейлист
            </button>
            <button className="sheet-action" onClick={() => setView("remixes")}>
              <RemixIcon />
              Ремиксы
            </button>
            <button
              className={"sheet-action" + (view === "lyrics" ? " on" : "")}
              onClick={() => setView(view === "lyrics" ? "main" : "lyrics")}
            >
              <TextIcon />
              Текст
            </button>
            <button className="sheet-action" onClick={() => setView("comments")}>
              <CommentIcon />
              Комменты
            </button>
            <button
              className="sheet-action"
              onClick={() => {
                downloadTrack(streamUrl(track), track.artist, track.title);
                toast("Скачиваю…");
              }}
            >
              <DownloadIcon />
              Скачать
            </button>
          </div>

          <div className="sheet-progress" {...handlers}>
            <div className="bar">
              <div className="buf" style={{ width: `${buf}%` }} />
              <div className="fill" style={{ width: `${pct}%` }} />
              <div className="knob" style={{ left: `${pct}%` }} />
            </div>
          </div>
          {playError ? <div className="sheet-error">{playError}</div> : null}
          <div className="sheet-times">
            <span>{fmtTime(time)}</span>
            <span>{loading ? "…" : fmtTime(total)}</span>
          </div>

          {view === "lyrics" && !sideLyrics ? (
            <LyricsPanel track={track} onBack={() => setView("main")} />
          ) : null}
          {view === "remixes" ? (
            <RemixesPanel
              track={track}
              onBack={() => setView("main")}
              onClose={() => setOpen(false)}
            />
          ) : null}
          {view === "comments" ? (
            <CommentsPanel
              track={track}
              pos={time}
              onBack={() => setView("main")}
            />
          ) : null}

          <div className="sheet-extra">
            <button className="sheet-vol-btn" onClick={toggleMute} title={muted ? "Включить звук" : "Выключить звук"}>
              {muted || volume === 0 ? <MuteIcon /> : <VolumeIcon />}
            </button>
            <input
              className="vol-slider"
              type="range"
              min={0}
              max={1}
              step={0.01}
              value={muted ? 0 : volume}
              onChange={(e) => setVolume(Number(e.target.value))}
              aria-label="Громкость"
            />
            <button
              className={"sheet-act" + (liked ? " on" : "")}
              onClick={likeTrack}
              title={liked ? "Убрать из любимых" : "В любимые"}
              aria-pressed={liked}
              disabled={!likeReady}
            >
              <HeartIcon fill={liked ? "currentColor" : "none"} />
            </button>
            <button
              className={"sheet-act" + (disliked ? " on" : "")}
              onClick={dislikeTrack}
              title="Не нравится"
              aria-pressed={disliked}
            >
              <DislikeIcon />
            </button>
            <button className="sheet-act" onClick={() => setFxOpen(true)} title="Эквалайзер">
              <FxIcon />
            </button>
            <button
              className={"sheet-extra-btn" + (queueOpen ? " on" : "")}
              onClick={() => setQueueOpen(!queueOpen)}
              title="Очередь"
            >
              <QueueIcon />
              <span className="hide-sm">Очередь</span>
            </button>
          </div>

          <div className="sheet-controls">
            <button className="side" onClick={prev}>
              <PrevIcon />
            </button>
            <button className="big" onClick={toggle}>
              {playing ? <PauseIcon /> : <PlayIcon />}
            </button>
            <button className="side" onClick={next}>
              <NextIcon />
            </button>
            <button
              className="side"
              title="В библиотеку"
              onClick={() => {
                music
                  .add(track)
                  .then(() => toast("Добавлено"))
                  .catch(() => toast("Не получилось"));
              }}
            >
              <PlusIcon />
            </button>
          </div>
        </div>

        {sideLyrics ? (
          <LyricsPanel side track={track} onBack={() => setView("main")} />
        ) : null}

        {fxOpen ? <FxDialog onClose={() => setFxOpen(false)} /> : null}
      </div>
    </div>
  );
}
