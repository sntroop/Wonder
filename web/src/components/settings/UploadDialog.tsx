import { useEffect, useRef, useState, type ChangeEvent } from "react";
import { useNavigate } from "react-router-dom";
import { upload, type Track } from "../../api";
import { useToast } from "../../toast";
import { fmtTime } from "../../utils";
import { Cover } from "../Cover";
import { DownloadIcon, PlusIcon } from "../icons";
import { Dialog } from "./Dialog";

const MAX_AUDIO = 40 * 1024 * 1024;
const MAX_COVER = 8 * 1024 * 1024;

function mb(bytes: number): string {
  return `${(bytes / (1024 * 1024)).toFixed(1)} МБ`;
}

type Props = { onClose: () => void };

/** Загрузка своего трека: аудио + необязательная обложка. */
export function UploadDialog({ onClose }: Props) {
  const navigate = useNavigate();
  const toast = useToast((s) => s.show);

  const [file, setFile] = useState<File | null>(null);
  const [cover, setCover] = useState<File | null>(null);
  const [coverUrl, setCoverUrl] = useState("");
  const [title, setTitle] = useState("");
  const [genre, setGenre] = useState("");
  const [duration, setDuration] = useState(0);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [published, setPublished] = useState<Track | null>(null);

  const aliveRef = useRef(true);

  useEffect(() => {
    aliveRef.current = true;
    return () => {
      aliveRef.current = false;
    };
  }, []);

  // длительность читаем в браузере, чтобы сервер не пересчитывал файл
  useEffect(() => {
    if (!file) {
      setDuration(0);
      return;
    }
    const url = URL.createObjectURL(file);
    const audio = new Audio();
    let alive = true;
    audio.preload = "metadata";
    audio.addEventListener("loadedmetadata", () => {
      if (!alive) return;
      const d = Number.isFinite(audio.duration) ? audio.duration : 0;
      setDuration(d);
    });
    audio.addEventListener("error", () => {
      if (alive) setDuration(0);
    });
    audio.src = url;
    audio.load();
    return () => {
      alive = false;
      audio.src = "";
      URL.revokeObjectURL(url);
    };
  }, [file]);

  useEffect(() => {
    if (!cover) {
      setCoverUrl("");
      return;
    }
    const url = URL.createObjectURL(cover);
    setCoverUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [cover]);

  const pickAudio = (e: ChangeEvent<HTMLInputElement>) => {
    const next = e.target.files?.[0] || null;
    e.target.value = "";
    setError("");
    if (!next) return;
    if (next.size > MAX_AUDIO) {
      setFile(null);
      setError("Трек больше 40 МБ — выбери файл поменьше");
      return;
    }
    setFile(next);
    const base = next.name.replace(/\.[^.]+$/, "").replace(/[_]+/g, " ").trim();
    if (base) {
      setTitle((cur) => cur || base.slice(0, 120));
    }
  };

  const pickCover = (e: ChangeEvent<HTMLInputElement>) => {
    const next = e.target.files?.[0] || null;
    e.target.value = "";
    setError("");
    if (!next) return;
    if (next.size > MAX_COVER) {
      setCover(null);
      setError("Обложка больше 8 МБ — выбери файл поменьше");
      return;
    }
    setCover(next);
  };

  const submit = async () => {
    if (busy) return;
    if (!file) {
      setError("Выбери аудиофайл");
      return;
    }
    if (file.size > MAX_AUDIO) {
      setError("Трек больше 40 МБ — выбери файл поменьше");
      return;
    }
    if (!title.trim()) {
      setError("Введи название трека");
      return;
    }
    setBusy(true);
    setError("");
    try {
      const res = await upload.track(
        file,
        {
          title: title.trim(),
          genre: genre.trim(),
          duration: Math.round(duration),
        },
        cover,
      );
      if (!aliveRef.current) return;
      setPublished(res.track);
      toast("Трек опубликован");
    } catch (e) {
      if (!aliveRef.current) return;
      setError(e instanceof Error ? e.message : "Не удалось загрузить трек");
    } finally {
      if (aliveRef.current) setBusy(false);
    }
  };

  const openProfile = () => {
    onClose();
    navigate("/profile");
  };

  return (
    <Dialog caption="Музыка" title="Свой трек" onClose={onClose}>
      {published ? (
        <div className="st-stack">
          <div className="st-preview">
            <div className="st-preview-cover">
              <Cover src={published.artwork} size={160} />
            </div>
            <div className="st-preview-info">
              <div className="st-preview-title">{published.title}</div>
              <div className="st-preview-sub">
                <span>{published.artist}</span>
                <span className="badge own">WONDER</span>
              </div>
            </div>
          </div>
          <div className="st-note">Трек опубликован и уже виден в профиле.</div>
          <div className="st-actions">
            <button className="btn btn-primary st-btn-sm" onClick={openProfile}>
              Открыть профиль
            </button>
          </div>
        </div>
      ) : (
        <form
          className="st-form"
          onSubmit={(e) => {
            e.preventDefault();
            void submit();
          }}
        >
          <label className="st-drop">
            <input type="file" accept="audio/*" onChange={pickAudio} />
            <span className="st-drop-icon">
              <DownloadIcon />
            </span>
            <span className="st-drop-info">
              <b>{file ? file.name : "Выбрать аудио"}</b>
              <span>
                {file
                  ? `${mb(file.size)}${duration ? ` · ${fmtTime(duration)}` : ""}`
                  : "MP3, WAV, M4A · до 40 МБ"}
              </span>
            </span>
          </label>

          <label className="st-drop">
            <input type="file" accept="image/*" onChange={pickCover} />
            <span className="st-drop-thumb">
              {coverUrl ? <img src={coverUrl} alt="" /> : <PlusIcon />}
            </span>
            <span className="st-drop-info">
              <b>{cover ? cover.name : "Обложка"}</b>
              <span>
                {cover
                  ? mb(cover.size)
                  : "Необязательно · JPG или PNG · до 8 МБ"}
              </span>
            </span>
          </label>

          <div className="st-field">
            <label className="st-label" htmlFor="st-upload-title">
              Название
            </label>
            <input
              id="st-upload-title"
              className="field"
              value={title}
              maxLength={120}
              placeholder="Как называется трек"
              disabled={busy}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>

          <div className="st-field">
            <label className="st-label" htmlFor="st-upload-genre">
              Жанр
            </label>
            <input
              id="st-upload-genre"
              className="field"
              value={genre}
              maxLength={40}
              placeholder="Необязательно"
              disabled={busy}
              onChange={(e) => setGenre(e.target.value)}
            />
          </div>

          {error ? <div className="st-alert">{error}</div> : null}

          {busy ? (
            <div className="st-stack">
              <div className="st-progress-row">
                <span>Загружаю…</span>
                <span>не закрывай окно</span>
              </div>
              <div className="st-progress st-indeterminate">
                <i />
              </div>
            </div>
          ) : null}

          <div className="st-actions">
            <button
              className="btn btn-primary st-btn-sm"
              type="submit"
              disabled={busy || !file || !title.trim()}
            >
              {busy ? "Загружаю…" : "Опубликовать"}
            </button>
            <span className="st-note">Один трек в 24 часа</span>
          </div>
        </form>
      )}
    </Dialog>
  );
}
