import { useEffect, useMemo, useState } from "react";
import { imgUrl, interests, type InterestItem } from "../../api";

/** genre_art уже отдаётся как /api/... — imgUrl добавил бы /api второй раз. */
function artUrl(path: string): string {
  if (path.startsWith("/api/")) return `/wonder${path}`;
  return imgUrl(path, 300) || "";
}
import { useToast } from "../../toast";
import { CheckIcon, SearchIcon } from "../icons";
import { Dialog } from "./Dialog";

/** Каталог жанров + любимые артисты: семена «Моей волны» (как InterestPicker в Android). */
export function InterestsDialog({ onClose }: { onClose: () => void }) {
  const toast = useToast((s) => s.show);
  const [groups, setGroups] = useState<Array<{ key: string; title: string; items: InterestItem[] }> | null>(null);
  const [maxPick, setMaxPick] = useState(8);
  const [picked, setPicked] = useState<string[]>([]);
  const [artists, setArtists] = useState("");
  const [q, setQ] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    let alive = true;
    Promise.allSettled([interests.catalog(), interests.prefs()]).then(([cat, pref]) => {
      if (!alive) return;
      if (cat.status === "fulfilled") {
        setGroups(cat.value.groups || []);
        setMaxPick(cat.value.max || 8);
      } else {
        setError(cat.reason instanceof Error ? cat.reason.message : "Каталог не загрузился");
      }
      if (pref.status === "fulfilled") {
        setPicked(pref.value.genres || []);
        setArtists((pref.value.artists || []).join(", "));
        setSaved((pref.value.genres || []).length > 0);
      }
    });
    return () => {
      alive = false;
    };
  }, []);

  const toggle = (id: string) => {
    setPicked((prev) => {
      if (prev.includes(id)) return prev.filter((x) => x !== id);
      if (prev.length >= maxPick) {
        toast(`Можно выбрать не больше ${maxPick}`);
        return prev;
      }
      return [...prev, id];
    });
  };

  const filtered = useMemo(() => {
    if (!groups) return [];
    const needle = q.trim().toLowerCase();
    return groups
      .map((g) => ({
        ...g,
        items: needle ? g.items.filter((i) => i.title.toLowerCase().includes(needle)) : g.items,
      }))
      .filter((g) => g.items.length > 0);
  }, [groups, q]);

  const save = async () => {
    const list = artists
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean)
      .slice(0, 20);
    if (!picked.length && !list.length) {
      toast("Выбери хотя бы один жанр");
      return;
    }
    setBusy(true);
    try {
      await interests.save(picked, list);
      setSaved(true);
      toast("Волна настроена");
      onClose();
    } catch (e) {
      toast(e instanceof Error ? e.message : "Не сохранилось");
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog
      caption="Для «Моей волны»"
      title="Интересы"
      className="int-dialog"
      onClose={onClose}
      closeDisabled={busy}
    >
      <div className="st-form">
        <div className="int-search">
          <SearchIcon />
          <input
            className="field"
            value={q}
            placeholder="Найти жанр"
            onChange={(e) => setQ(e.target.value)}
          />
          <span className="int-counter">
            {picked.length}/{maxPick}
          </span>
        </div>

        {error ? <div className="st-alert">{error}</div> : null}

        {groups === null ? (
          <div className="center-spin">
            <div className="spinner" />
          </div>
        ) : (
          <div className="int-groups">
            {filtered.map((g) => (
              <section className="int-group" key={g.key}>
                <h4>{g.title}</h4>
                <div className="int-grid">
                  {g.items.map((item) => {
                    const on = picked.includes(item.id);
                    return (
                      <button
                        key={item.id}
                        className={"int-card" + (on ? " on" : "")}
                        onClick={() => toggle(item.id)}
                        title={item.title}
                      >
                        <span className="int-card-img">
                          <img src={artUrl(item.img)} alt="" loading="lazy" />
                          {on ? (
                            <span className="int-check">
                              <CheckIcon />
                            </span>
                          ) : null}
                        </span>
                        <span className="int-card-title">{item.title}</span>
                      </button>
                    );
                  })}
                </div>
              </section>
            ))}
            {!filtered.length ? <div className="empty">Ничего не нашлось</div> : null}
          </div>
        )}

        <div className="st-field">
          <label className="st-label" htmlFor="int-artists">
            Любимые артисты — через запятую
          </label>
          <input
            id="int-artists"
            className="field"
            value={artists}
            placeholder="Miyagi, Баста, Kizaru"
            onChange={(e) => setArtists(e.target.value)}
          />
          <div className="st-note">Помогут собрать волну, пока вкус ещё не накопился</div>
        </div>

        <div className="st-actions">
          <button className="btn btn-primary st-btn-sm" disabled={busy} onClick={() => void save()}>
            {busy ? "Сохраняю…" : saved ? "Обновить" : "Начать слушать"}
          </button>
          <button className="btn btn-ghost st-btn-sm" onClick={onClose}>
            Пропустить
          </button>
        </div>
      </div>
    </Dialog>
  );
}
