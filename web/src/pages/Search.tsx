import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { music, type Artist, type Playlist, type SearchAll, type Track } from "../api";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { TrackRow } from "../components/TrackRow";
import { PlaylistCard } from "../components/Cards";
import { ArtistRow } from "./Library";
import { Cover } from "../components/Cover";
import { PlayIcon, SearchIcon } from "../components/icons";

type Tab = "tracks" | "artists" | "playlists";

export function Search() {
  const [params, setParams] = useSearchParams();
  const q = (params.get("q") || "").trim();
  const [value, setValue] = useState(q);
  const [tab, setTab] = useState<Tab>("tracks");
  const [data, setData] = useState<SearchAll | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const play = usePlayer((s) => s.play);
  const toast = useToast((s) => s.show);

  useEffect(() => {
    setValue(q);
  }, [q]);

  useEffect(() => {
    if (!q) {
      setData(null);
      return;
    }
    let alive = true;
    setLoading(true);
    setError("");
    music
      .searchAll(q, 30)
      .then((res) => alive && setData(res))
      .catch((e) => alive && setError(e instanceof Error ? e.message : "Поиск не ответил"))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [q]);

  const tracks: Track[] = (data?.tracks as Track[]) || [];
  const artists: Artist[] = (data?.artists as Artist[]) || [];
  const playlists: Playlist[] = (data?.playlists as Playlist[]) || [];

  return (
    <div className="page page-enter">
      <h1 className="page-title">Поиск</h1>
      <form
        className="search-field"
        onSubmit={(e) => {
          e.preventDefault();
          const next = value.trim();
          if (next) setParams({ q: next });
        }}
      >
        <SearchIcon />
        <input
          value={value}
          onChange={(e) => setValue(e.target.value)}
          placeholder="Треки, артисты, альбомы, плейлисты"
          autoFocus
        />
      </form>

      {q ? (
        <div className="chips">
          <button className={`chip${tab === "tracks" ? " active" : ""}`} onClick={() => setTab("tracks")}>
            Треки
          </button>
          <button className={`chip${tab === "artists" ? " active" : ""}`} onClick={() => setTab("artists")}>
            Артисты
          </button>
          <button
            className={`chip${tab === "playlists" ? " active" : ""}`}
            onClick={() => setTab("playlists")}
          >
            Плейлисты
          </button>
        </div>
      ) : null}

      {loading ? (
        <div className="center-spin">
          <div className="spinner" />
        </div>
      ) : null}

      {error ? (
        <div className="empty">
          <h3>Ошибка поиска</h3>
          <div>{error}</div>
        </div>
      ) : null}

      {!loading && !error && q && tab === "tracks" ? (
        tracks.length ? (
          <>
            <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 10 }}>
              <button className="btn btn-ghost" onClick={() => play(tracks, 0, "search")}>
                <PlayIcon /> Играть всё
              </button>
            </div>
            <div className="tracklist">
              {tracks.map((t, i) => (
                <TrackRow key={`${t.source}-${t.id}-${i}`} track={t} index={i} queue={tracks} onToast={toast} />
              ))}
            </div>
          </>
        ) : (
          <div className="empty">
            <h3>Ничего не нашлось</h3>
            <div>Попробуй другое название или артиста</div>
          </div>
        )
      ) : null}

      {!loading && !error && q && tab === "artists" ? (
        artists.length ? (
          <div>
            {artists.map((a) => (
              <ArtistRow key={`${a.source}-${a.id}-${a.name}`} artist={a} />
            ))}
          </div>
        ) : (
          <div className="empty">
            <h3>Артисты не найдены</h3>
          </div>
        )
      ) : null}

      {!loading && !error && q && tab === "playlists" ? (
        playlists.length ? (
          <div className="grid-cards">
            {playlists.map((p, i) => (
              <PlaylistCard key={`${p.owner_id}-${p.id}-${i}`} playlist={p} />
            ))}
          </div>
        ) : (
          <div className="empty">
            <h3>Плейлисты не найдены</h3>
          </div>
        )
      ) : null}

      {!q ? (
        <div className="empty" style={{ paddingTop: 40 }}>
          <h3>Что послушаем?</h3>
          <div>Введи название трека, артиста или плейлиста</div>
        </div>
      ) : null}
    </div>
  );
}
