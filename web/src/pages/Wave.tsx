import { useCallback, useEffect, useState } from "react";
import { feedback, music, type Track } from "../api";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { Cover } from "../components/Cover";
import { EqBackground } from "../components/EqBackground";
import { useArtworkTint } from "../tintHook";
import { DislikeIcon, PauseIcon, PlayIcon, PlusIcon } from "../components/icons";

export function Wave() {
  const [items, setItems] = useState<Track[] | null>(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const play = usePlayer((s) => s.play);
  const current = usePlayer((s) => s.current());
  const playing = usePlayer((s) => s.playing);
  const toggle = usePlayer((s) => s.toggle);
  const next = usePlayer((s) => s.next);
  const toast = useToast((s) => s.show);

  const load = useCallback(async (refresh = false) => {
    setBusy(true);
    setError("");
    try {
      const res = await music.wave(40, refresh);
      setItems(res.items || []);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Волна не отвечает");
    } finally {
      setBusy(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const start = () => {
    if (!items?.length) {
      toast("Волна пустая — добавь треки в библиотеку");
      return;
    }
    play(items, 0, "wave");
  };

  const active = Boolean(current);
  const shown = current ?? items?.[0];
  const tint = useArtworkTint(shown?.artwork);

  return (
    <div className="page page-enter">
      <div className="wave-stage">
        <div className="wave-art">
          <EqBackground track={shown} color={tint} playing={playing} frac={0.36} />
          <div className="cover">
            <Cover src={shown?.artwork} size={700} />
          </div>
        </div>

        <h1 className="wave-title">{shown ? shown.title : "Моя волна"}</h1>
        <div className="wave-artist">
          {shown
            ? shown.artist
            : busy
              ? "собираю треки…"
              : error
                ? error
                : items && !items.length
                  ? "волна пустая — добавь треки в библиотеку"
                  : "персональное радио"}
        </div>

        <div className="wave-actions">
          <button className="btn btn-primary" onClick={active ? toggle : start} disabled={busy}>
            {active && playing ? <PauseIcon /> : <PlayIcon />}
            {active && playing ? "Пауза" : "Слушать"}
          </button>
          <button
            className="round-add"
            title="Не нравится"
            onClick={() => {
              if (shown) void feedback(shown, "dislike").catch(() => {});
              if (active) next();
              toast("Понял, дальше");
            }}
          >
            <DislikeIcon />
          </button>
          <button
            className="round-add"
            title="Ещё волна"
            onClick={() => {
              void load(true);
              toast("Собираю другую волну");
            }}
          >
            <PlusIcon />
          </button>
        </div>

      </div>
    </div>
  );
}
