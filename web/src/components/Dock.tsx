import { useLayoutEffect, useRef, useState } from "react";
import { NavLink, useLocation } from "react-router-dom";
import { usePlayer } from "../store";
import { Cover } from "./Cover";
import {
  CloseIcon,
  HomeIcon,
  LibraryIcon,
  NextIcon,
  PauseIcon,
  PlayIcon,
  QueueIcon,
  SearchIcon,
  ShortsIcon,
  WaveIcon,
} from "./icons";

const tabs = [
  { to: "/", label: "Главная", icon: HomeIcon, end: true },
  { to: "/search", label: "Поиск", icon: SearchIcon },
  { to: "/shorts", label: "Шортсы", icon: ShortsIcon },
  { to: "/wave", label: "Моя волна", icon: WaveIcon },
  { to: "/library", label: "Библиотека", icon: LibraryIcon },
];

function activeIndex(pathname: string): number {
  if (pathname.startsWith("/search")) return 1;
  if (pathname.startsWith("/shorts")) return 2;
  if (pathname.startsWith("/wave")) return 3;
  if (pathname.startsWith("/artist")) return 1;
  if (pathname.startsWith("/library") || pathname.startsWith("/playlist")) return 4;
  return 0;
}

export function Dock() {
  const track = usePlayer((s) => s.current());
  const playing = usePlayer((s) => s.playing);
  const toggle = usePlayer((s) => s.toggle);
  const next = usePlayer((s) => s.next);
  const stop = usePlayer((s) => s.stop);
  const setSheetOpen = usePlayer((s) => s.setSheetOpen);
  const setQueueOpen = usePlayer((s) => s.setQueueOpen);
  const queueOpen = usePlayer((s) => s.queueOpen);
  const { pathname } = useLocation();
  const index = activeIndex(pathname);

  const navRef = useRef<HTMLElement | null>(null);
  const tabRefs = useRef<Array<HTMLAnchorElement | null>>([]);
  const [bubble, setBubble] = useState<{ left: number; top: number } | null>(null);

  // Белый кружок ставим по фактическому центру иконки активной вкладки:
  // раньше позиция считалась как (index + 0.5) * 20% от ширины дока, а это
  // не учитывало padding навбара и высоту иконки -> кружок уезжал влево и вверх.
  useLayoutEffect(() => {
    const nav = navRef.current;
    const tab = tabRefs.current[index];
    if (!nav || !tab) return;
    const place = () => {
      const icon = (tab.querySelector("svg") as SVGElement | null) || tab;
      const n = nav.getBoundingClientRect();
      const r = icon.getBoundingClientRect();
      // left/top у absolute считаются от padding-box, а getBoundingClientRect
      // отдаёт border-box — компенсируем рамку навбара (1px), иначе кружок
      // уезжает ровно на её толщину.
      setBubble({
        left: r.left - n.left - nav.clientLeft + r.width / 2,
        top: r.top - n.top - nav.clientTop + r.height / 2,
      });
    };
    place();
    const ro = new ResizeObserver(place);
    ro.observe(nav);
    ro.observe(tab);
    window.addEventListener("resize", place);
    return () => {
      ro.disconnect();
      window.removeEventListener("resize", place);
    };
  }, [index]);

  return (
    <div className="dock">
      {track ? (
        <div className="mini" onClick={() => setSheetOpen(true)}>
          <div className="mini-cover">
            <Cover src={track.artwork} size={120} />
          </div>
          <div className="mini-info">
            <div className="mini-title">{track.title}</div>
            <div className="mini-artist">{track.artist}</div>
          </div>
          <div className="mini-controls" onClick={(e) => e.stopPropagation()}>
            <button
              className="mini-btn"
              title="Очередь"
              onClick={() => setQueueOpen(!queueOpen)}
              style={queueOpen ? { color: "#fff", background: "rgba(255,255,255,.12)" } : undefined}
            >
              <QueueIcon />
            </button>
            <button className="mini-btn" onClick={toggle} title={playing ? "Пауза" : "Играть"}>
              {playing ? <PauseIcon /> : <PlayIcon />}
            </button>
            <button className="mini-btn" onClick={next} title="Дальше">
              <NextIcon />
            </button>
            <button className="mini-btn" onClick={stop} title="Закрыть">
              <CloseIcon />
            </button>
          </div>
        </div>
      ) : null}

      <nav className="navbar" ref={navRef}>
        <span
          className="nav-bubble"
          style={bubble ? { left: bubble.left, top: bubble.top } : { opacity: 0 }}
        />
        {tabs.map(({ to, label, icon: Icon, end }, i) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={i === index ? "nav-tab active" : "nav-tab"}
            ref={(el) => {
              tabRefs.current[i] = el;
            }}
          >
            <Icon />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
