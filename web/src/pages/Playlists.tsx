import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  music,
  playlists,
  type AppPlaylist,
  type Playlist,
} from "../api";
import { PlaylistCard } from "../components/Cards";
import { Cover } from "../components/Cover";
import { SectionHead } from "../components/TrackRow";
import { PlaylistDialog } from "../components/PlaylistDialog";
import { PlayIcon, PlaylistIcon, PlusIcon } from "../components/icons";

export function Playlists() {
  const navigate = useNavigate();
  const [mine, setMine] = useState<AppPlaylist[] | null>(null);
  const [mineError, setMineError] = useState("");
  const [vk, setVk] = useState<Playlist[] | null>(null);
  const [vkError, setVkError] = useState("");
  const [mineTick, setMineTick] = useState(0);
  const [vkTick, setVkTick] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);

  useEffect(() => {
    let alive = true;
    setMine(null);
    setMineError("");
    playlists
      .mine()
      .then((res) => {
        if (alive) setMine(res.items || []);
      })
      .catch((e) => {
        if (!alive) return;
        setMine([]);
        setMineError(
          e instanceof Error ? e.message : "Плейлисты не загрузились",
        );
      });
    return () => {
      alive = false;
    };
  }, [mineTick]);

  useEffect(() => {
    let alive = true;
    setVk(null);
    setVkError("");
    music
      .myPlaylists(50)
      .then((res) => {
        if (alive) setVk(res.items || []);
      })
      .catch((e) => {
        if (!alive) return;
        setVk([]);
        setVkError(e instanceof Error ? e.message : "Плейлисты VK не загрузились");
      });
    return () => {
      alive = false;
    };
  }, [vkTick]);

  const addSaved = (p: AppPlaylist) => {
    setMineError("");
    setMine((prev) => {
      const rest = (prev || []).filter((x) => x.id !== p.id);
      return [p, ...rest];
    });
  };

  return (
    <div className="page page-enter">
      <div className="pl-head">
        <div>
          <h1 className="page-title">Плейлисты</h1>
          <div className="page-sub">Свои подборки и плейлисты людей</div>
        </div>
        <button
          className="btn btn-primary pl-create"
          onClick={() => setCreateOpen(true)}
        >
          <PlusIcon /> Создать плейлист
        </button>
      </div>

      <section className="section pl-section">
        <SectionHead title="Мои плейлисты" />

        {mine === null ? (
          <div className="grid-cards pl-grid" aria-hidden>
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="skeleton" style={{ height: 210 }} />
            ))}
          </div>
        ) : mineError ? (
          <div className="empty pl-empty">
            <h3>Плейлисты не загрузились</h3>
            <div>{mineError}</div>
            <button
              className="btn btn-ghost"
              onClick={() => setMineTick((n) => n + 1)}
            >
              Повторить
            </button>
          </div>
        ) : mine.length ? (
          <div className="grid-cards pl-grid">
            {mine.map((p) => (
              <AppPlaylistCard key={p.id} playlist={p} />
            ))}
          </div>
        ) : (
          <div className="empty pl-empty">
            <h3>Своих плейлистов пока нет</h3>
            <div className="hint">
              Соберите подборку — она появится здесь и в вашем профиле.
            </div>
            <button
              className="btn btn-primary"
              onClick={() => setCreateOpen(true)}
            >
              <PlusIcon /> Создать
            </button>
          </div>
        )}
      </section>

      {vk && vk.length ? (
        <section className="section pl-section">
          <SectionHead title="Из VK" />
          <div className="grid-cards pl-grid">
            {vk.map((p, i) => (
              <PlaylistCard key={`${p.owner_id}-${p.id}-${i}`} playlist={p} />
            ))}
          </div>
        </section>
      ) : null}

      {vkError ? (
        <div className="pl-notice">
          <span>Плейлисты VK не загрузились: {vkError}</span>
          <button onClick={() => setVkTick((n) => n + 1)}>Повторить</button>
        </div>
      ) : null}

      <PlaylistDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onSaved={addSaved}
      />
    </div>
  );
}

function AppPlaylistCard({ playlist }: { playlist: AppPlaylist }) {
  const navigate = useNavigate();
  const meta = [
    `${playlist.count || 0} треков`,
    playlist.author || "",
  ]
    .filter(Boolean)
    .join(" · ");

  return (
    <div
      className="card"
      onClick={() => navigate(`/playlist/w/${playlist.id}`)}
      title={playlist.title}
    >
      <div className="card-cover">
        {playlist.cover ? (
          <Cover src={playlist.cover} size={400} />
        ) : (
          <div className="pl-card-empty">
            <PlaylistIcon />
          </div>
        )}
        <div className="card-play">
          <PlayIcon />
        </div>
      </div>
      <div className="card-title">{playlist.title}</div>
      <div className="card-sub">{meta}</div>
    </div>
  );
}
