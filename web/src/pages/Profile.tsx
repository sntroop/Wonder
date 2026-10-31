import { useEffect, useRef, useState, type ChangeEvent } from "react";
import { useNavigate } from "react-router-dom";
import {
  imgUrl,
  profile as profileApi,
  type Profile as ProfileData,
} from "../api";
import { useAuth } from "../store";
import { useToast } from "../toast";
import { TrackRow } from "../components/TrackRow";
import { ChevronLeft, PlusIcon } from "../components/icons";

export function Profile() {
  const navigate = useNavigate();
  const { user, me, logout, refresh } = useAuth();
  const toast = useToast((s) => s.show);

  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(user?.name || "");
  const [bio, setBio] = useState("");
  const [editError, setEditError] = useState("");
  const [saving, setSaving] = useState(false);
  const [avatarBusy, setAvatarBusy] = useState(false);
  const [avatarStamp, setAvatarStamp] = useState(0);

  const fileRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    let alive = true;
    profileApi
      .get()
      .then((p) => {
        if (!alive) return;
        setProfile(p);
        setName(p.name || "");
        setBio(p.bio || "");
      })
      .catch(() => undefined)
      .finally(() => {
        if (alive) setLoading(false);
      });
    return () => {
      alive = false;
    };
  }, []);

  const startEdit = () => {
    setName(profile?.name || user?.name || "");
    setBio(profile?.bio || "");
    setEditError("");
    setEditing(true);
  };

  const saveProfile = async () => {
    const next = name.trim();
    if (!next || saving) return;
    setSaving(true);
    setEditError("");
    try {
      const res = await profileApi.update(next, bio.trim());
      setProfile((cur) => ({
        ...(cur || res),
        ...res,
        tracks: cur?.tracks ?? res.tracks,
      }));
      setEditing(false);
      toast("Профиль обновлён");
      void refresh();
    } catch (e) {
      setEditError(
        e instanceof Error ? e.message : "Не удалось сохранить профиль",
      );
    } finally {
      setSaving(false);
    }
  };

  const onAvatar = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] || null;
    e.target.value = "";
    if (!file) return;
    if (file.size > 8 * 1024 * 1024) {
      toast("Аватар больше 8 МБ");
      return;
    }
    setAvatarBusy(true);
    try {
      await profileApi.avatar(file);
      const fresh = await profileApi.get();
      setProfile(fresh);
      setAvatarStamp(Date.now());
      toast("Аватар обновлён");
      void refresh();
    } catch (err) {
      toast(
        err instanceof Error ? err.message : "Не удалось обновить аватар",
      );
    } finally {
      setAvatarBusy(false);
    }
  };

  const displayName = profile?.name || user?.name || "Гость";
  const initial = displayName.replace("@", "").slice(0, 1).toUpperCase() || "W";
  const avatar = profile?.avatar || "";
  const avatarSrc = avatar
    ? `${imgUrl(avatar) || ""}${avatarStamp ? `?v=${avatarStamp}` : ""}`
    : "";
  const stats = profile?.stats;
  const subKey =
    stats && typeof stats.subs === "number"
      ? "subs"
      : stats && typeof stats.followers === "number"
        ? "followers"
        : "";
  const statTiles = [
    { key: "tracks", label: "треков" },
    { key: "plays", label: "прослушиваний" },
    { key: "likes", label: "лайков" },
    ...(subKey ? [{ key: subKey, label: "подписчиков" }] : []),
  ];
  const tracks = profile?.tracks || [];
  const vk = me?.vk;

  return (
    <div className="page st-page">
      <h1 className="page-title">Профиль</h1>

      <div className="profile-head st-profile-head">
        <div className="st-avatar-wrap">
          <div className="profile-avatar">
            {avatarSrc ? (
              <img src={avatarSrc} alt="" />
            ) : (
              initial
            )}
          </div>
          <button
            className="st-avatar-edit"
            title="Сменить аватар"
            disabled={avatarBusy}
            onClick={() => fileRef.current?.click()}
          >
            {avatarBusy ? <span className="spinner small" /> : <PlusIcon />}
          </button>
          <input
            ref={fileRef}
            className="st-hidden"
            type="file"
            accept="image/*"
            onChange={(e) => void onAvatar(e)}
          />
        </div>

        <div className="st-profile-id">
          <div className="profile-handle">{displayName}</div>
          <div className="profile-role">
            {profile?.bio || "твой профиль автора"}
          </div>
          {user?.username ? (
            <div className="hint">@{user.username}</div>
          ) : null}
        </div>

        <div className="st-head-actions">
          <button
            className="btn btn-primary st-btn-sm"
            onClick={startEdit}
          >
            Редактировать
          </button>
          <button
            className="btn btn-ghost st-btn-sm"
            onClick={() => navigate("/settings")}
          >
            Настроить
          </button>
        </div>
      </div>

      {editing ? (
        <div className="st-edit">
          <label className="st-field">
            <span className="st-label">Имя</span>
            <input
              className="field"
              value={name}
              maxLength={60}
              placeholder="Как тебя зовут"
              disabled={saving}
              onChange={(e) => setName(e.target.value)}
            />
          </label>
          <label className="st-field">
            <span className="st-label">О себе</span>
            <textarea
              className="st-textarea"
              value={bio}
              maxLength={300}
              placeholder="Пара слов о себе"
              disabled={saving}
              onChange={(e) => setBio(e.target.value)}
            />
          </label>
          {editError ? <div className="st-alert">{editError}</div> : null}
          <div className="st-edit-actions">
            <button
              className="btn btn-primary st-btn-sm"
              disabled={saving || !name.trim()}
              onClick={() => void saveProfile()}
            >
              {saving ? "Сохраняю…" : "Сохранить"}
            </button>
            <button
              className="btn btn-ghost st-btn-sm"
              disabled={saving}
              onClick={() => {
                setEditing(false);
                setEditError("");
              }}
            >
              Отмена
            </button>
          </div>
        </div>
      ) : null}

      <div className="stats st-stats">
        {statTiles.map((tile) => (
          <div className="stat" key={tile.key}>
            <b>{stats?.[tile.key] ?? "—"}</b>
            <span>{tile.label}</span>
          </div>
        ))}
      </div>

      <div className="st-section">
        <div className="eyebrow st-eyebrow">Свои публикации</div>
        {loading ? (
          <div className="st-stack">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="skeleton" style={{ height: 58 }} />
            ))}
          </div>
        ) : tracks.length ? (
          tracks.map((track, index) => (
            <TrackRow
              key={`${track.source}:${track.id}`}
              track={track}
              index={index}
              queue={tracks}
              onToast={toast}
            />
          ))
        ) : (
          <div className="empty st-empty">Ты пока не выкладывал треки</div>
        )}
      </div>

      <div className="st-section">
        <div className="list">
          <div className="list-row">
            <div className="grow">
              <div>VK Музыка</div>
              <div className="hint">
                {vk?.ready
                  ? "подключена на сервере"
                  : vk?.relay
                    ? "работает через телефон"
                    : vk?.has_token
                      ? "токен есть, VK не отвечает"
                      : "не подключена"}
              </div>
            </div>
            <span className="val">
              {vk?.ready || vk?.relay ? "активна" : "выкл"}
            </span>
            <ChevronLeft style={{ transform: "rotate(180deg)" }} />
          </div>

          <div className="list-row">
            <div className="grow">
              <div>SoundCloud</div>
              <div className="hint">
                {me?.sc?.connected ? "подключён" : "не подключён"}
              </div>
            </div>
            <span className="val">
              {me?.sc?.connected ? "активен" : "выкл"}
            </span>
            <ChevronLeft style={{ transform: "rotate(180deg)" }} />
          </div>

          <div className="list-row">
            <div className="grow">
              <div>Вкус</div>
              <div className="hint">
                {me?.taste
                  ? `${me.taste.features} признаков · ${me.taste.events} событий`
                  : "данных пока нет"}
              </div>
            </div>
            <span className="val">
              {me?.taste ? `${Math.round(me.taste.evidence * 100)}%` : "—"}
            </span>
          </div>

          <button
            className="list-row st-row-btn st-danger"
            onClick={logout}
          >
            <span className="grow">Выйти из аккаунта</span>
          </button>
        </div>
      </div>
    </div>
  );
}
