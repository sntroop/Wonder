import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { music, profile, type Track } from "../api";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { TrackRow } from "../components/TrackRow";
import { ChevronLeft, PlayIcon } from "../components/icons";

export function LibraryMusic() {
  const [tracks, setTracks] = useState<Track[] | null>(null);
  const [error, setError] = useState("");
  const play = usePlayer((s) => s.play);
  const toast = useToast((s) => s.show);
  const navigate = useNavigate();

  useEffect(() => {
    let alive = true;
    Promise.allSettled([music.myAudio(500), profile.get()])
      .then(([vk, own]) => {
        if (!alive) return;
        const items: Track[] = [];
        if (vk.status === "fulfilled") items.push(...(vk.value.items || []));
        if (own.status === "fulfilled") items.push(...(own.value.tracks || []));
        const seen = new Set<string>();
        const unique = items.filter((t) => {
          const key = `${t.source}:${t.id}`;
          if (seen.has(key)) return false;
          seen.add(key);
          return true;
        });
        setTracks(unique);
        if (vk.status === "rejected" && !unique.length) {
          setError(vk.reason instanceof Error ? vk.reason.message : "Не загрузилось");
        }
      });
    return () => {
      alive = false;
    };
  }, []);

  const minutes = Math.round((tracks || []).reduce((a, t) => a + (t.duration || 0), 0) / 60);

  return (
    <div className="page page-enter">
      <div style={{ display: "flex", alignItems: "center", gap: 14, padding: "18px 0 4px" }}>
        <button className="icon-btn" onClick={() => navigate(-1)}>
          <ChevronLeft />
        </button>
        <h1 style={{ fontSize: 28 }}>Моя музыка</h1>
      </div>
      <div className="page-sub">
        {tracks ? `${tracks.length} треков · ${minutes} мин` : "загружаю…"}
      </div>

      {tracks?.length ? (
        <div style={{ display: "flex", gap: 10, marginBottom: 16 }}>
          <button className="btn btn-primary" onClick={() => play(tracks, 0, "my")}>
            <PlayIcon /> Слушать всё
          </button>
        </div>
      ) : null}

      {error ? <div className="empty">{error}</div> : null}

      {tracks === null ? (
        <div className="center-spin">
          <div className="spinner" />
        </div>
      ) : tracks.length ? (
        <div className="tracklist">
          {tracks.map((t, i) => (
            <TrackRow key={`${t.source}-${t.id}-${i}`} track={t} index={i} queue={tracks} onToast={toast} />
          ))}
        </div>
      ) : (
        <div className="empty">
          <h3>Пока пусто</h3>
          <div>Добавляй треки лайком или плюсом — они появятся здесь</div>
        </div>
      )}
    </div>
  );
}
