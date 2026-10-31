import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { music, playlists as myLists, profile, type Artist, type Playlist, type Track } from "../api";
import { useToast } from "../toast";
import { ImportDialog } from "../components/settings/ImportDialog";
import { Cover } from "../components/Cover";
import {
  ChevronLeft,
  DownloadIcon,
  NoteIcon,
  PlaylistIcon,
  UserIcon,
  WaveIcon,
} from "../components/icons";

export function Library() {
  const [tracks, setTracks] = useState<Track[] | null>(null);
  const [artists, setArtists] = useState<Artist[] | null>(null);
  const [playlists, setPlaylists] = useState<Playlist[] | null>(null);
  const [appLists, setAppLists] = useState<number | null>(null);
  const [ownCount, setOwnCount] = useState(0);
  const [importOpen, setImportOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    let alive = true;
    Promise.allSettled([
      music.myAudio(500),
      music.artists(),
      music.myPlaylists(50),
      myLists.mine(),
      profile.get(),
    ]).then(([a, b, c, d, e]) => {
      if (!alive) return;
      if (a.status === "fulfilled") setTracks(a.value.items || []);
      else setTracks([]);
      if (b.status === "fulfilled") setArtists(b.value.items || []);
      else setArtists([]);
      if (c.status === "fulfilled") setPlaylists(c.value.items || []);
      else setPlaylists([]);
      setAppLists(d.status === "fulfilled" ? (d.value.items || []).length : 0);
      setOwnCount(e.status === "fulfilled" ? (e.value.tracks || []).length : 0);
    });
    return () => {
      alive = false;
    };
  }, []);

  const rows = [
    {
      key: "music",
      icon: NoteIcon,
      title: "Моя музыка",
      sub: tracks ? `${tracks.length + ownCount} треков` : "загружаю…",
      to: "/library/music",
    },
    {
      key: "artists",
      icon: UserIcon,
      title: "Артисты",
      sub: artists ? `${artists.length} артистов` : "загружаю…",
      to: "/library/artists",
    },
    {
      key: "playlists",
      icon: PlaylistIcon,
      title: "Плейлисты",
      sub:
        playlists && appLists !== null
          ? `${playlists.length + appLists} плейлистов`
          : "загружаю…",
      to: "/playlists",
    },
    {
      key: "wave",
      icon: WaveIcon,
      title: "Моя волна",
      sub: "Бесконечный поток по вкусу",
      to: "/wave",
    },
  ];

  return (
    <div className="page page-enter">
      <h1 className="page-title">Библиотека</h1>
      <div className="page-sub">Твоя музыка, артисты и плейлисты</div>

      {rows.map(({ key, icon: Icon, title, sub, to }) => (
        <button key={key} className="hub-card" onClick={() => navigate(to)}>
          <span className="hub-icon">
            <Icon />
          </span>
          <span className="hub-text">
            <b>{title}</b>
            <span>{sub}</span>
          </span>
          <ChevronLeft className="hub-chev" style={{ transform: "rotate(180deg)" }} />
        </button>
      ))}

      <button className="hub-card" onClick={() => setImportOpen(true)}>
        <span className="hub-icon">
          <DownloadIcon />
        </span>
        <span className="hub-text">
          <b>Перенос музыки</b>
          <span>Ссылка, скриншот, список треков или TikTok</span>
        </span>
        <ChevronLeft className="hub-chev" style={{ transform: "rotate(180deg)" }} />
      </button>

      <h3 className="section-title" style={{ margin: "34px 0 14px" }}>
        Ваши артисты
      </h3>

      {artists === null ? (
        <div className="center-spin">
          <div className="spinner" />
        </div>
      ) : artists.length ? (
        <div>
          {artists.slice(0, 6).map((a) => (
            <ArtistRow key={`${a.source}-${a.id}-${a.name}`} artist={a} />
          ))}
        </div>
      ) : (
        <div className="page-sub" style={{ maxWidth: 520 }}>
          Пока никого. Открой любого артиста и нажми «Добавить в библиотеку» — он появится здесь.
        </div>
      )}

      {importOpen ? <ImportDialog onClose={() => setImportOpen(false)} /> : null}
    </div>
  );
}

export function ArtistRow({ artist }: { artist: Artist }) {
  const navigate = useNavigate();
  return (
    <div
      className="artist-row"
      onClick={() =>
        navigate(
          `/artist/${artist.source}/${encodeURIComponent(artist.id)}?name=${encodeURIComponent(artist.name)}`,
        )
      }
    >
      <div className="artist-avatar">
        <Cover src={artist.photo} size={200} />
      </div>
      <div className="artist-meta">
        <b>{artist.name}</b>
        <span>Артист</span>
      </div>
      <ChevronLeft style={{ transform: "rotate(180deg)", color: "rgba(255,255,255,.4)" }} />
    </div>
  );
}
