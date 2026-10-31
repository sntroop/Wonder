import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useLocation, useNavigate } from "react-router-dom";
import { feedback, music, streamUrl, type Track } from "../api";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { fmtTime } from "../utils";
import { useAddToPlaylist } from "./AddToPlaylist";
import { Cover } from "./Cover";
import { EqBars } from "./EqBars";
import {
  ArtistIcon,
  ChevronLeft,
  DislikeIcon,
  DownloadIcon,
  HeartIcon,
  MoreIcon,
  NextIcon,
  PlaylistIcon,
  QueueIcon,
  ShareIcon,
} from "./icons";

type Props = {
  track: Track;
  index?: number;
  queue?: Track[];
  onToast?: (msg: string) => void;
};

export function TrackRow({ track, index = 0, queue, onToast }: Props) {
  const current = usePlayer((s) => s.current());
  const playing = usePlayer((s) => s.playing);
  const play = usePlayer((s) => s.play);
  const toggle = usePlayer((s) => s.toggle);
  const isCurrent = current?.source === track.source && current?.id === track.id;
  const [liked, setLiked] = useState(false);
  const [liking, setLiking] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const moreRef = useRef<HTMLButtonElement>(null);
  const toast = useToast((s) => s.show);
  const navigate = useNavigate();
  const showToast = onToast ?? toast;

  const handlePlay = () => {
    if (isCurrent) {
      toggle();
      return;
    }
    play(queue?.length ? queue : [track], queue?.length ? index : 0, "app");
  };

  const toggleLike = async () => {
    if (liking) return;
    setLiking(true);
    try {
      const res = await music.like(track);
      setLiked(Boolean(res.liked));
      showToast(res.liked ? "В любимых" : "Убрано из любимых");
    } catch {
      showToast("Не получилось");
    } finally {
      setLiking(false);
    }
  };

  const like = async (e: React.MouseEvent) => {
    e.stopPropagation();
    await toggleLike();
  };

  return (
    <div
      className={`track has-more${isCurrent ? " playing" : ""}`}
      onClick={handlePlay}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          handlePlay();
        }
      }}
    >
      <div className="track-cover">
        <Cover src={track.artwork} size={160} />
        {isCurrent ? (
          <span className="eq-overlay">
            <EqBars playing={playing} size={18} />
          </span>
        ) : null}
      </div>
      <div className="track-info">
        <div className="track-title">
          {track.title}
          {track.source !== "soundcloud" ? (
            <span className="badge">
              {track.source === "vk" ? "VK" : track.source.toUpperCase()}
            </span>
          ) : null}
        </div>
        <div className="track-artist">{track.artist}</div>
      </div>
      <div className="track-dur">{fmtTime(track.duration)}</div>
      <button className="track-like" title="В любимые" aria-label="В любимые" onClick={like}>
        <HeartIcon fill={liked ? "currentColor" : "none"} />
      </button>
      <button
        ref={moreRef}
        type="button"
        className="track-more"
        title="Действия с треком"
        aria-label="Действия с треком"
        aria-haspopup="menu"
        aria-expanded={menuOpen}
        onClick={(e) => {
          e.stopPropagation();
          setMenuOpen((v) => !v);
        }}
        onKeyDown={(e) => e.stopPropagation()}
      >
        <MoreIcon />
      </button>

      {menuOpen ? (
        <TrackMenu
          track={track}
          liked={liked}
          liking={liking}
          anchor={moreRef.current}
          onLike={toggleLike}
          onClose={() => setMenuOpen(false)}
          onToast={showToast}
          onArtist={() => {
            if (track.source === "own") {
              if (track.owner && !track.mine) navigate(`/user/${track.owner}`);
              else navigate("/profile");
              return;
            }
            const q = track.artist ? `?name=${encodeURIComponent(track.artist)}` : "";
            navigate(
              `/artist/${encodeURIComponent(track.source)}/${encodeURIComponent(
                track.id,
              )}${q}`,
            );
          }}
        />
      ) : null}
    </div>
  );
}

/* ── поповер-меню ─────────────────────────────────────────────────────── */

type PopoverProps = {
  anchor: HTMLElement | null;
  onClose: () => void;
  label: string;
  children: React.ReactNode;
};

/**
 * Меню, привязанное к кнопке: position: fixed по getBoundingClientRect,
 * отступ 12px, переворот вверх у нижней кромки, прижатие к вьюпорту.
 * Закрывается по клику мимо, Escape, скроллу и смене маршрута.
 */
export function MenuPopover({ anchor, onClose, label, children }: PopoverProps) {
  const ref = useRef<HTMLDivElement>(null);
  const [pos, setPos] = useState({ top: 0, left: 0 });
  const [ready, setReady] = useState(false);
  const { pathname } = useLocation();
  const lastPath = useRef(pathname);

  useLayoutEffect(() => {
    if (!anchor) return;
    const place = () => {
      const el = ref.current;
      if (!el) return;
      const r = anchor.getBoundingClientRect();
      const w = el.offsetWidth;
      const h = el.offsetHeight;
      const gap = 12;
      const pad = 8;
      let left = r.right - w;
      left = Math.max(pad, Math.min(left, window.innerWidth - w - pad));
      let top = r.bottom + gap;
      if (top + h > window.innerHeight - pad) top = r.top - gap - h;
      top = Math.max(pad, Math.min(top, window.innerHeight - h - pad));
      setPos({ top, left });
      setReady(true);
    };
    place();
    window.addEventListener("resize", place);
    return () => window.removeEventListener("resize", place);
  }, [anchor]);

  useEffect(() => {
    const raf = requestAnimationFrame(() => {
      const first = ref.current?.querySelector<HTMLElement>('[role="menuitem"]');
      first?.focus({ preventScroll: true });
    });
    return () => cancelAnimationFrame(raf);
  }, []);

  useEffect(() => {
    const onDown = (e: PointerEvent) => {
      const target = e.target as Node | null;
      if (!target) return;
      if (ref.current?.contains(target)) return;
      if (anchor?.contains(target)) return;
      onClose();
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key !== "Escape") return;
      e.preventDefault();
      e.stopPropagation();
      onClose();
      anchor?.focus({ preventScroll: true });
    };
    const onScroll = (e: Event) => {
      const target = e.target;
      if (target instanceof Node && ref.current?.contains(target)) return;
      onClose();
    };
    document.addEventListener("pointerdown", onDown, true);
    document.addEventListener("keydown", onKey, true);
    window.addEventListener("scroll", onScroll, true);
    return () => {
      document.removeEventListener("pointerdown", onDown, true);
      document.removeEventListener("keydown", onKey, true);
      window.removeEventListener("scroll", onScroll, true);
    };
  }, [anchor, onClose]);

  useEffect(() => {
    if (lastPath.current === pathname) return;
    lastPath.current = pathname;
    onClose();
  }, [pathname, onClose]);

  return createPortal(
    <div
      ref={ref}
      className="tm-menu"
      role="menu"
      aria-label={label}
      style={{
        top: pos.top,
        left: pos.left,
        visibility: ready ? "visible" : "hidden",
      }}
      onClick={(e) => {
        e.stopPropagation();
        onClose();
      }}
      onKeyDown={(e) => {
        e.stopPropagation();
        const items = Array.from(
          ref.current?.querySelectorAll<HTMLElement>('[role="menuitem"]') || [],
        );
        const at = items.indexOf(document.activeElement as HTMLElement);
        if (e.key === "ArrowDown") {
          e.preventDefault();
          items[(at + 1) % items.length]?.focus();
        } else if (e.key === "ArrowUp") {
          e.preventDefault();
          items[(at - 1 + items.length) % items.length]?.focus();
        } else if (e.key === "Home") {
          e.preventDefault();
          items[0]?.focus();
        } else if (e.key === "End") {
          e.preventDefault();
          items[items.length - 1]?.focus();
        } else if (e.key === "Tab") {
          onClose();
        }
      }}
    >
      {children}
    </div>,
    document.body,
  );
}

export function MenuItem({
  icon,
  onClick,
  danger,
  children,
}: {
  icon?: React.ReactNode;
  onClick: () => void;
  danger?: boolean;
  children: React.ReactNode;
}) {
  return (
    <button
      type="button"
      role="menuitem"
      className={`tm-item${danger ? " tm-danger" : ""}`}
      onClick={onClick}
    >
      {icon ? <span className="tm-ico">{icon}</span> : null}
      <span className="tm-label">{children}</span>
    </button>
  );
}

function TrackMenu({
  track,
  liked,
  liking,
  anchor,
  onLike,
  onClose,
  onToast,
  onArtist,
}: {
  track: Track;
  liked: boolean;
  liking: boolean;
  anchor: HTMLElement | null;
  onLike: () => void;
  onClose: () => void;
  onToast: (msg: string) => void;
  onArtist: () => void;
}) {
  const playNext = usePlayer((s) => s.playNext);
  const enqueue = usePlayer((s) => s.enqueue);

  const share = async () => {
    const link = `${window.location.origin}/wonder/?t=${track.source}:${track.id}`;
    try {
      await navigator.clipboard.writeText(link);
      onToast("Ссылка скопирована");
    } catch {
      onToast("Не получилось скопировать");
    }
  };

  return (
    <MenuPopover
      anchor={anchor}
      onClose={onClose}
      label={`Действия: ${track.title}`}
    >
      <MenuItem
        icon={<PlaylistIcon />}
        onClick={() => useAddToPlaylist.getState().open(track)}
      >
        В плейлист
      </MenuItem>
      <MenuItem icon={<NextIcon />} onClick={() => playNext(track)}>
        Играть следующим
      </MenuItem>
      <MenuItem icon={<QueueIcon />} onClick={() => enqueue(track)}>
        В очередь
      </MenuItem>
      <MenuItem icon={<ArtistIcon />} onClick={onArtist}>
        {track.source === "own" ? "Профиль автора" : "К артисту"}
      </MenuItem>
      <MenuItem icon={<ShareIcon />} onClick={() => void share()}>
        Поделиться
      </MenuItem>
      <MenuItem
        icon={<DownloadIcon />}
        onClick={() => window.open(streamUrl(track), "_blank")}
      >
        Скачать
      </MenuItem>
      <div className="tm-sep" role="separator" />
      <MenuItem
        icon={<HeartIcon fill={liked ? "currentColor" : "none"} />}
        onClick={() => {
          if (!liking) onLike();
        }}
      >
        {liked ? "Убрать из любимых" : "В любимые"}
      </MenuItem>
      <MenuItem
        icon={<DislikeIcon />}
        onClick={() => {
          feedback(track, "dislike")
            .then(() => onToast("Понял, такое реже"))
            .catch(() => onToast("Не получилось"));
        }}
      >
        Не нравится
      </MenuItem>
    </MenuPopover>
  );
}

export function SectionHead({
  title,
  action,
  onAction,
}: {
  title: string;
  action?: string;
  onAction?: () => void;
}) {
  return (
    <div className="section-head">
      <h3 className="section-title">{title}</h3>
      {action ? (
        <span className="section-more" onClick={onAction}>
          {action}
        </span>
      ) : null}
    </div>
  );
}

export function ScreenHead({ title, right }: { title: string; right?: React.ReactNode }) {
  const navigate = useNavigate();
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 14, padding: "18px 0 10px" }}>
      <button className="icon-btn" onClick={() => navigate(-1)}>
        <ChevronLeft />
      </button>
      <h1 style={{ fontSize: 26, flex: 1 }}>{title}</h1>
      {right}
    </div>
  );
}
