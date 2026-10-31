import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { imgUrl, music, type Track } from "../api";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { MenuItem, MenuPopover, TrackRow } from "../components/TrackRow";
import {
  ChevronLeft,
  LibraryIcon,
  MoreIcon,
  PlayIcon,
  SearchIcon,
  ShareIcon,
  WaveIcon,
} from "../components/icons";

type ArtistData = {
  name?: string;
  cover?: string;
  tracks?: Track[];
  albums?: Track[];
  bio?: string;
  mine?: boolean;
  artist?: { name?: string; photo?: string; mine?: boolean };
};

export function ArtistPage() {
  const { source = "", id = "" } = useParams();
  const [params] = useSearchParams();
  const name = params.get("name") || "";
  const [data, setData] = useState<ArtistData | null>(null);
  const [error, setError] = useState("");
  const [added, setAdded] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const moreRef = useRef<HTMLButtonElement>(null);
  const play = usePlayer((s) => s.play);
  const toast = useToast((s) => s.show);
  const navigate = useNavigate();

  useEffect(() => {
    let alive = true;
    setData(null);
    setError("");
    setAdded(false);
    music
      .artist(source, id, name, 200)
      .then((res) => {
        if (alive) setData(res as ArtistData);
      })
      .catch((e) => {
        if (alive) setError(e instanceof Error ? e.message : "Артист не загрузился");
      });
    return () => {
      alive = false;
    };
  }, [source, id, name]);

  const tracks = data?.tracks || [];
  const title = data?.name || data?.artist?.name || name || "Артист";
  const cover = data?.cover || data?.artist?.photo;
  const inLibrary = Boolean(data?.artist?.mine ?? data?.mine) || added;

  const addToLibrary = async () => {
    try {
      await music.artistAdd({ source, id, name: title, cover });
      setAdded(true);
      toast("Артист в библиотеке");
    } catch (e) {
      toast(e instanceof Error ? e.message : "Не получилось");
    }
  };

  const share = async () => {
    const link = `${window.location.origin}/wonder/artist/${source}/${id}`;
    try {
      await navigator.clipboard.writeText(link);
      toast("Ссылка скопирована");
    } catch {
      toast("Не получилось скопировать");
    }
  };

  const startWave = async () => {
    try {
      const res = await music.artist(source, id, title, 60);
      const list = res.tracks || res.items || [];
      if (!list.length) {
        toast("Треков пока нет");
        return;
      }
      play(list, 0, "artist");
      toast("Волна артиста запущена");
    } catch (e) {
      toast(e instanceof Error ? e.message : "Волна не запустилась");
    }
  };

  return (
    <div className="page page-enter" style={{ paddingTop: 0 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "18px 0 12px" }}>
        <button className="icon-btn" onClick={() => navigate(-1)} aria-label="Назад">
          <ChevronLeft />
        </button>
        <h1 style={{ fontSize: 26, flex: 1 }}>Артист</h1>
        <button className="icon-btn" onClick={() => navigate("/search")} aria-label="Поиск">
          <SearchIcon />
        </button>
        <button
          ref={moreRef}
          className="icon-btn"
          aria-label="Меню артиста"
          aria-haspopup="menu"
          aria-expanded={menuOpen}
          onClick={() => setMenuOpen((v) => !v)}
        >
          <MoreIcon />
        </button>
      </div>

      {/* меню вне шапки: у неё backdrop-filter, который ломает position: fixed */}
      {menuOpen ? (
        <MenuPopover
          anchor={moreRef.current}
          onClose={() => setMenuOpen(false)}
          label="Меню артиста"
        >
          {inLibrary ? null : (
            <MenuItem icon={<LibraryIcon />} onClick={() => void addToLibrary()}>
              В библиотеку
            </MenuItem>
          )}
          <MenuItem icon={<ShareIcon />} onClick={() => void share()}>
            Поделиться
          </MenuItem>
          <MenuItem icon={<WaveIcon />} onClick={() => void startWave()}>
            Волна артиста
          </MenuItem>
        </MenuPopover>
      ) : null}

      <div
        className="artist-hero"
        style={
          cover
            ? { backgroundImage: `url(${imgUrl(cover, 1200)})` }
            : undefined
        }
      >
        <div className="artist-hero-fade" />
        <h1 className="artist-hero-name">{title}</h1>
      </div>

      <div style={{ textAlign: "center", color: "var(--text-2)", margin: "18px 0 20px" }}>
        {tracks.length ? `${tracks.length} треков` : error || "треков пока нет"}
      </div>

      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 10 }}>
        <button
          className="btn btn-primary"
          style={{ minWidth: 240, height: 56, fontSize: 17 }}
          disabled={!tracks.length}
          onClick={() => play(tracks, 0, "artist")}
        >
          <PlayIcon /> Слушать
        </button>
        <button
          className="btn btn-ghost"
          style={{ minWidth: 240, height: 52 }}
          disabled={inLibrary}
          onClick={() => void addToLibrary()}
        >
          {inLibrary ? "Уже в библиотеке" : "Добавить в библиотеку"}
        </button>
      </div>

      <div className="eyebrow" style={{ margin: "30px 0 12px" }}>
        Треки
      </div>

      {!data && !error ? (
        <div className="center-spin">
          <div className="spinner" />
        </div>
      ) : null}

      {tracks.length ? (
        <div className="tracklist">
          {tracks.map((t, i) => (
            <TrackRow
              key={`${t.source}-${t.id}-${i}`}
              track={t}
              index={i}
              queue={tracks}
              onToast={toast}
            />
          ))}
        </div>
      ) : null}
    </div>
  );
}
