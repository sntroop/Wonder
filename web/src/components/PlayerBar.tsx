import { useCallback, useRef, useState } from "react";
import type { Track } from "../api";
import { music } from "../api";
import { usePlayer } from "../store";
import { cx, fmtTime } from "../utils";
import { Cover } from "./Cover";
import {
  HeartIcon,
  MuteIcon,
  NextIcon,
  PauseIcon,
  PlayIcon,
  PrevIcon,
  QueueIcon,
  VolumeIcon,
} from "./icons";

function useSeek(onSeek: (value: number) => void, max: number) {
  const ref = useRef<HTMLDivElement>(null);
  const [dragging, setDragging] = useState(false);

  const valueAt = useCallback(
    (clientX: number) => {
      const el = ref.current;
      if (!el) return 0;
      const rect = el.getBoundingClientRect();
      const ratio = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
      return ratio * max;
    },
    [max],
  );

  const handlers = {
    ref,
    onPointerDown: (e: React.PointerEvent) => {
      (e.target as HTMLElement).setPointerCapture?.(e.pointerId);
      setDragging(true);
      onSeek(valueAt(e.clientX));
    },
    onPointerMove: (e: React.PointerEvent) => {
      if (dragging) onSeek(valueAt(e.clientX));
    },
    onPointerUp: () => setDragging(false),
    onPointerCancel: () => setDragging(false),
  };

  return { handlers, dragging };
}

export function PlayerBar() {
  const { queue, index, playing, time, duration, buffered, volume, muted, loading, error } =
    usePlayer();
  const toggle = usePlayer((s) => s.toggle);
  const next = usePlayer((s) => s.next);
  const prev = usePlayer((s) => s.prev);
  const seek = usePlayer((s) => s.seek);
  const setVolume = usePlayer((s) => s.setVolume);
  const toggleMute = usePlayer((s) => s.toggleMute);
  const setQueueOpen = usePlayer((s) => s.setQueueOpen);
  const queueOpen = usePlayer((s) => s.queueOpen);

  const track: Track | null = index >= 0 ? queue[index] : null;
  const total = duration || track?.duration || 0;
  const { handlers } = useSeek(seek, total);
  const [liked, setLiked] = useState(false);

  const like = async () => {
    if (!track) return;
    try {
      const res = await music.like(track);
      setLiked(Boolean(res.liked));
    } catch {
      /* молча: плеер не должен падать из-за лайка */
    }
  };

  const pct = total ? Math.min(100, (time / total) * 100) : 0;
  const bufPct = total ? Math.min(100, (buffered / total) * 100) : 0;

  return (
    <footer className="playerbar">
      <div className="np">
        <div className="np-cover">
          <Cover src={track?.artwork} size={120} />
        </div>
        <div className="np-info">
          <div className="np-title">{track?.title || "Ничего не играет"}</div>
          <div className="np-artist">
            {error ? <span style={{ color: "#ff9db4" }}>{error}</span> : track?.artist || "Выбери трек"}
          </div>
        </div>
        {track ? (
          <button
            className="ctrl"
            title="В любимые"
            onClick={like}
            style={liked ? { color: "#ff7ba8" } : undefined}
          >
            <HeartIcon fill={liked ? "currentColor" : "none"} />
          </button>
        ) : null}
      </div>

      <div className="center-controls">
        <div className="controls">
          <button className="ctrl" onClick={prev} title="Назад">
            <PrevIcon />
          </button>
          <button
            className={cx("ctrl", "play")}
            onClick={toggle}
            title={playing ? "Пауза" : "Играть"}
            disabled={!track}
            style={!track ? { opacity: 0.4 } : undefined}
          >
            {playing ? <PauseIcon /> : <PlayIcon />}
          </button>
          <button className="ctrl" onClick={next} title="Дальше">
            <NextIcon />
          </button>
        </div>

        <div className="progress" {...handlers}>
          <div className="progress-track">
            <div className="progress-buffer" style={{ width: `${bufPct}%` }} />
            <div className="progress-fill" style={{ width: `${pct}%` }} />
            <div className="progress-knob" style={{ left: `${pct}%` }} />
          </div>
        </div>
        <div className="time">
          <span>{fmtTime(time)}</span>
          <span>{loading ? "буферизация…" : fmtTime(total)}</span>
        </div>
      </div>

      <div className="right-controls">
        <button className="ctrl" onClick={toggleMute} title="Звук">
          {muted || volume === 0 ? <MuteIcon /> : <VolumeIcon />}
        </button>
        <div
          className="volume"
          onClick={(e) => {
            const rect = e.currentTarget.getBoundingClientRect();
            setVolume((e.clientX - rect.left) / rect.width);
          }}
        >
          <div
            className="volume-fill"
            style={{ width: `${(muted ? 0 : volume) * 100}%` }}
          />
        </div>
        <button
          className="ctrl"
          onClick={() => setQueueOpen(!queueOpen)}
          title="Очередь"
          style={queueOpen ? { color: "var(--blue)" } : undefined}
        >
          <QueueIcon />
        </button>
      </div>
    </footer>
  );
}
