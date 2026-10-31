import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { music, type HomeBlock } from "../api";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { PlaylistCard, TrackCard } from "../components/Cards";
import { SectionHead, TrackRow } from "../components/TrackRow";
import { ChevronLeft, PauseIcon, PlayIcon, RefreshIcon } from "../components/icons";

function shuffle<T>(list: T[]): T[] {
  const copy = [...list];
  for (let i = copy.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}

export function Home() {
  const [blocks, setBlocks] = useState<HomeBlock[] | null>(null);
  const [error, setError] = useState("");
  const [refreshing, setRefreshing] = useState(false);
  const play = usePlayer((s) => s.play);
  const current = usePlayer((s) => s.current());
  const playing = usePlayer((s) => s.playing);
  const toggle = usePlayer((s) => s.toggle);
  const toast = useToast((s) => s.show);
  const navigate = useNavigate();

  const load = useCallback(async (refresh = false) => {
    setRefreshing(true);
    setError("");
    try {
      const res = await music.home(refresh);
      setBlocks(res.blocks || []);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Не удалось загрузить подборки");
    } finally {
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const waveActive = Boolean(current);
  const startWave = async () => {
    if (waveActive) {
      toggle();
      return;
    }
    try {
      const res = await music.wave(40, true);
      if (!res.items?.length) {
        toast("Добавь треки в библиотеку — волна соберётся сама");
        return;
      }
      play(shuffle(res.items), 0, "wave");
    } catch (e) {
      toast(e instanceof Error ? e.message : "Волна не отвечает");
    }
  };

  return (
    <div className="page page-enter">
      <button className="wave-hero" onClick={() => void startWave()} style={{ marginTop: 8 }}>
        <span className="wave-hero-play">
          {waveActive && playing ? <PauseIcon /> : <PlayIcon />}
        </span>
        <span className="wave-hero-text">
          <b>Моя волна</b>
          <span>
            {waveActive && current
              ? `${current.artist} — ${current.title}`
              : "VK Микс · персональное радио"}
          </span>
        </span>
        <ChevronLeft className="chev" style={{ transform: "rotate(180deg)", width: 18, height: 18 }} />
      </button>

      {error && !blocks ? (
        <div className="empty">
          <h3>Сервер не ответил</h3>
          <div>{error}</div>
          <button className="btn btn-ghost" style={{ marginTop: 18 }} onClick={() => void load()}>
            <RefreshIcon /> Повторить
          </button>
        </div>
      ) : null}

      {!blocks ? (
        <div className="section" style={{ marginTop: 30 }}>
          <div className="skeleton" style={{ height: 18, width: 190, marginBottom: 16 }} />
          <div className="rail">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="skeleton" style={{ height: 210 }} />
            ))}
          </div>
        </div>
      ) : null}

      {blocks?.map((block) => {
        if (block.kind === "playlists") {
          const items = block.playlists || [];
          if (!items.length) return null;
          return (
            <section className="section" key={block.key} style={{ marginTop: 30 }}>
              <SectionHead
                title={block.title}
                action="все"
                onAction={() => navigate("/playlists")}
              />
              <div className="rail">
                {items.map((p) => (
                  <PlaylistCard key={`${p.owner_id}-${p.id}`} playlist={p} />
                ))}
              </div>
            </section>
          );
        }

        const tracks = block.tracks || [];
        if (!tracks.length) return null;

        if (block.key === "mine") {
          return (
            <section className="section" key={block.key} style={{ marginTop: 30 }}>
              <SectionHead title={block.title} />
              <div className="group-card">
                {tracks.slice(0, 5).map((t, i) => (
                  <TrackRow
                    key={`${t.source}-${t.id}`}
                    track={t}
                    index={i}
                    queue={tracks}
                    onToast={toast}
                  />
                ))}
              </div>
              <button
                className="btn btn-ghost"
                style={{ width: "100%", marginTop: 12 }}
                onClick={() => navigate("/library")}
              >
                Перейти в мою музыку
              </button>
            </section>
          );
        }

        if (block.key === "recent") {
          return (
            <section className="section" key={block.key} style={{ marginTop: 30 }}>
              <SectionHead title={block.title} />
              <div className="rail">
                {tracks.slice(0, 10).map((t, i) => (
                  <TrackCard key={`${t.source}-${t.id}`} track={t} queue={tracks} index={i} />
                ))}
              </div>
            </section>
          );
        }

        return (
          <section className="section" key={block.key} style={{ marginTop: 30 }}>
            <SectionHead
              title={block.title}
              action="играть"
              onAction={() => play(tracks, 0, "home")}
            />
            <div className="rail">
              {tracks.map((t, i) => (
                <TrackCard key={`${t.source}-${t.id}`} track={t} queue={tracks} index={i} />
              ))}
            </div>
          </section>
        );
      })}

      {blocks && !blocks.length ? (
        <div className="empty">
          <h3>Пока пусто</h3>
          <div>Добавь треки в библиотеку — здесь появятся подборки</div>
        </div>
      ) : null}
    </div>
  );
}
