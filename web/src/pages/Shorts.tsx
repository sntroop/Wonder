import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { feedback, music, type Short } from "../api";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { Cover } from "../components/Cover";
import { EqBackground } from "../components/EqBackground";
import { useArtworkTint } from "../tintHook";
import { CommentsPanel } from "../components/TrackPanels";
import {
  ChevronLeft,
  DislikeIcon,
  HeartIcon,
  PauseIcon,
  PlayIcon,
  PlusIcon,
  TextIcon,
} from "../components/icons";

export function Shorts() {
  const [items, setItems] = useState<Short[] | null>(null);
  const [index, setIndex] = useState(0);
  const [comments, setComments] = useState(false);
  const [auto, setAuto] = useState(false);
  const [full, setFull] = useState(false);
  const setSheetOpen = usePlayer((s) => s.setSheetOpen);
  const lastNav = useRef(0);
  const touchY = useRef(0);
  const [error, setError] = useState("");
  const play = usePlayer((s) => s.play);
  const toggle = usePlayer((s) => s.toggle);
  const seek = usePlayer((s) => s.seek);
  const time = usePlayer((s) => s.time);
  const playing = usePlayer((s) => s.playing);
  const current = usePlayer((s) => s.current());
  const toast = useToast((s) => s.show);
  const navigate = useNavigate();

  useEffect(() => {
    let alive = true;
    music
      .shorts(20)
      .then((res) => {
        if (alive) setItems(res.items || []);
      })
      .catch((e) => {
        if (alive) {
          setError(e instanceof Error ? e.message : "Шортсы не загрузились");
          setItems([]);
        }
      });
    return () => {
      alive = false;
    };
  }, []);

  const short = items?.[index];
  const tint = useArtworkTint(short?.track?.artwork);
  const isCurrent = Boolean(
    short && current && short.track.source === current.source && short.track.id === current.id,
  );

  const go = (delta: number) => {
    if (!items?.length) return;
    setIndex((prev) => (prev + delta + items.length) % items.length);
  };

  /** «Не нравится»: сильный сигнал вкуса + сразу убираем из ленты. */
  const dislike = () => {
    if (!short || !items) return;
    void feedback(short.track, "dislike").catch(() => {});
    const rest = items.filter(
      (x) => !(x.track.source === short.track.source && x.track.id === short.track.id),
    );
    setItems(rest);
    setIndex(rest.length ? Math.min(index, rest.length - 1) : 0);
    toast("Больше не покажем");
  };

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "ArrowDown" || e.key === "ArrowRight") go(1);
      if (e.key === "ArrowUp" || e.key === "ArrowLeft") go(-1);
      if (e.key === "Escape") navigate("/");
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  });

  /** включить сам шортс (15-секундный отрезок) */
  const startShort = () => {
    if (!short) return;
    setFull(false);
    setAuto(true);
    if (!isCurrent) play([short.track], 0, "shorts");
    else {
      seek(short.start);
      if (!playing) toggle();
    }
  };

  /** открыть полный трек: с начала, без авто-перелистывания, с листом плеера */
  const playFull = () => {
    if (!short) return;
    setAuto(false);
    setFull(true);
    if (!isCurrent) play([short.track], 0, "shorts");
    window.setTimeout(() => seek(0), 260);
    setSheetOpen(true);
  };

  const onMainAction = () => {
    if (!isCurrent) startShort();
    else if (full) toggle();
    else playFull();
  };

  const mainLabel = !isCurrent
    ? "Включить"
    : full
      ? playing
        ? "Пауза"
        : "Продолжить"
      : "Слушать целиком";

  // шорт стартует сам при открытии и при листании
  useEffect(() => {
    if (!short) return;
    setFull(false);
    setAuto(true);
    play([short.track], 0, "shorts");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [short?.track.source, short?.track.id]);

  // шорт играет свой 15-секундный отрезок: перематываем на start
  useEffect(() => {
    if (!auto || !short || !isCurrent) return;
    const id = window.setTimeout(() => seek(short.start), 260);
    return () => window.clearTimeout(id);
  }, [auto, index, isCurrent, seek, short]);

  // дошли до end — листаем дальше
  useEffect(() => {
    if (!auto) return;
    const unsub = usePlayer.subscribe((st) => {
      const cur = st.queue[st.index];
      if (!cur || !short || cur.source !== short.track.source || cur.id !== short.track.id) return;
      if (st.playing && st.time >= short.end - 0.2) {
        const now = Date.now();
        if (now - lastNav.current < 700) return;
        lastNav.current = now;
        if (items?.length) {
          const nextIdx = (index + 1) % items.length;
          setIndex(nextIdx);
          play([items[nextIdx].track], 0, "shorts");
        }
      }
    });
    return unsub;
  }, [auto, short, items]);

  const wheelNav = (e: React.WheelEvent) => {
    if (Math.abs(e.deltaY) < 24) return;
    const now = Date.now();
    if (now - lastNav.current < 380) return;
    lastNav.current = now;
    go(e.deltaY > 0 ? 1 : -1);
  };

  return (
    <div
      className="shorts-page"
      onWheel={wheelNav}
      onTouchStart={(e) => {
        touchY.current = e.touches[0]?.clientY ?? 0;
      }}
      onTouchEnd={(e) => {
        const dy = (e.changedTouches[0]?.clientY ?? 0) - touchY.current;
        if (Math.abs(dy) < 60) return;
        if (Date.now() - lastNav.current < 380) return;
        lastNav.current = Date.now();
        go(dy < 0 ? 1 : -1);
      }}
    >
      {short ? (
        <div className="shorts-bg">
          <Cover src={short.track.artwork} size={300} />
        </div>
      ) : null}

      <div className="shorts-top">
        <h1>Шортсы</h1>
        <button className="icon-btn" onClick={() => navigate("/")} title="Закрыть">
          <ChevronLeft style={{ transform: "rotate(-90deg)" }} />
        </button>
      </div>

      <div className="shorts-body">
        {error ? <div className="empty">{error}</div> : null}

        {!items ? (
          <div className="spinner" />
        ) : short ? (
          <>
            <div className="shorts-art">
              <EqBackground
                track={short.track}
                color={tint}
                playing
                frac={0.5}
              />
              <div className="shorts-card">
                <Cover src={short.track.artwork} size={700} />
              </div>
              {isCurrent && short.end > short.start ? (
                <div className="shorts-progress">
                  <i
                    style={{
                      width: `${Math.max(
                        0,
                        Math.min(100, ((time - short.start) / (short.end - short.start)) * 100),
                      )}%`,
                    }}
                  />
                </div>
              ) : null}
              <div className="shorts-side">
                <button
                  title="В любимые"
                  onClick={() => {
                    music
                      .like(short.track)
                      .then(() => toast("В любимых"))
                      .catch(() => toast("Не получилось"));
                  }}
                >
                  <HeartIcon />
                </button>
                <button title="Не нравится" onClick={dislike}>
                  <DislikeIcon />
                </button>
                <button title="Комментарии" onClick={() => setComments(true)}>
                  <TextIcon />
                </button>
              </div>
            </div>

            <h2 className="shorts-title">
              {short.track.artist} · {short.track.title}
            </h2>
            <div className="shorts-artist">{short.track.artist}</div>

            <div className="shorts-actions">
              <button className="btn btn-primary" onClick={onMainAction}>
                {isCurrent && playing && full ? <PauseIcon /> : <PlayIcon />}
                {mainLabel}
              </button>
              <button
                className="round-add"
                title="В библиотеку"
                onClick={() => {
                  music
                    .add(short.track)
                    .then(() => toast("Добавлено в библиотеку"))
                    .catch(() => toast("Не получилось"));
                }}
              >
                <PlusIcon />
              </button>
            </div>
          </>
        ) : (
          <div className="empty">
            <h3>Пока нет шортсов</h3>
            <div>Слушай музыку — сервер найдёт яркие моменты</div>
          </div>
        )}
      </div>

      <div className="shorts-hint">
        <div className="line">
          <i style={{ width: `${items?.length ? ((index + 1) / items.length) * 100 : 0}%` }} />
        </div>
        листай вверх — следующий
      </div>

      {comments && short ? (
        <div
          className="sheet-backdrop"
          onClick={(e) => {
            if (e.target === e.currentTarget) setComments(false);
          }}
        >
          <div className="sheet sheet-standalone">
            <CommentsPanel
              track={short.track}
              pos={0}
              onBack={() => setComments(false)}
            />
          </div>
        </div>
      ) : null}
    </div>
  );
}
