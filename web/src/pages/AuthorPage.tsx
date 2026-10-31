import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { author, imgUrl, profile, type Profile } from "../api";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { TrackRow } from "../components/TrackRow";
import { ChevronLeft, PlayIcon } from "../components/icons";

/** Публичный профиль автора: аватар, статистика, подписка и его треки. */
export function AuthorPage() {
  const { id = "" } = useParams();
  const uid = Number(id) || 0;
  const [data, setData] = useState<Profile | null>(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();
  const toast = useToast((s) => s.show);
  const play = usePlayer((s) => s.play);

  useEffect(() => {
    let alive = true;
    setData(null);
    setError("");
    profile
      .get(uid)
      .then((p) => {
        if (alive) setData(p);
      })
      .catch((e) => {
        if (alive) setError(e instanceof Error ? e.message : "Профиль не найден");
      });
    return () => {
      alive = false;
    };
  }, [uid]);

  const tracks = data?.tracks || [];
  const name = data?.name || "Автор";
  const avatar = data?.avatar ? imgUrl(data.avatar, 240) : "";
  const initial = name.replace("@", "").slice(0, 1).toUpperCase() || "W";
  const stats = data?.stats || {};
  const subs = Number(stats.followers || 0);

  const subscribe = async () => {
    if (!data || busy) return;
    setBusy(true);
    try {
      const res = await author.sub(uid, !data.subscribed);
      setData({ ...data, subscribed: res.subscribed, stats: { ...stats, followers: res.subs } });
      toast(res.subscribed ? "Вы подписаны" : "Подписка отменена");
    } catch (e) {
      toast(e instanceof Error ? e.message : "Не получилось");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="page page-enter st-page">
      <div className="st-head">
        <button className="icon-btn" onClick={() => navigate(-1)} title="Назад">
          <ChevronLeft />
        </button>
        <h1 className="page-title st-title">Автор</h1>
      </div>

      {error ? <div className="empty">{error}</div> : null}

      {data ? (
        <>
          <div className="profile-head st-profile-head">
            <div className="st-avatar-wrap">
              <div className="profile-avatar">
                {avatar ? <img src={avatar} alt="" /> : initial}
              </div>
            </div>

            <div className="st-profile-id">
              <div className="profile-handle">{name}</div>
              <div className="profile-role">{data.bio || "автор Wonder"}</div>
              <div className="hint">
                {tracks.length} треков · {subs} подписчиков
              </div>
            </div>

            <div className="st-head-actions">
              {data.self ? (
                <button className="btn btn-ghost st-btn-sm" onClick={() => navigate("/profile")}>
                  Мой профиль
                </button>
              ) : (
                <button
                  className={"btn st-btn-sm " + (data.subscribed ? "btn-ghost" : "btn-primary")}
                  disabled={busy}
                  onClick={() => void subscribe()}
                >
                  {busy ? "…" : data.subscribed ? "Вы подписаны" : "Подписаться"}
                </button>
              )}
            </div>
          </div>

          {tracks.length ? (
            <>
              <div className="st-actions" style={{ marginTop: 18 }}>
                <button className="btn btn-primary st-btn-sm" onClick={() => play(tracks, 0, "artist")}>
                  <PlayIcon /> Слушать всё
                </button>
              </div>
              <div className="tracklist" style={{ marginTop: 10 }}>
                {tracks.map((t, i) => (
                  <TrackRow key={`${t.source}-${t.id}-${i}`} track={t} index={i} queue={tracks} onToast={toast} />
                ))}
              </div>
            </>
          ) : (
            <div className="empty" style={{ marginTop: 24 }}>
              Пока нет опубликованных треков
            </div>
          )}
        </>
      ) : !error ? (
        <div className="center-spin">
          <div className="spinner" />
        </div>
      ) : null}
    </div>
  );
}
