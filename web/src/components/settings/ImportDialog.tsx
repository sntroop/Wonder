import { useEffect, useRef, useState } from "react";
import {
  importer,
  playlists,
  type ImportJob,
  type ImportJobStart,
  type ImportPreview,
} from "../../api";
import { usePlayer } from "../../store";
import { useToast } from "../../toast";
import { badgeClass, fmtTime, sourceLabel } from "../../utils";
import { Cover } from "../Cover";
import { PlayIcon } from "../icons";
import { Dialog } from "./Dialog";

type Props = { onClose: () => void };
type Mode = "link" | "shot" | "list" | "tiktok";

const MODES: Array<{ key: Mode; label: string }> = [
  { key: "link", label: "Ссылка" },
  { key: "shot", label: "Скриншот" },
  { key: "list", label: "Список" },
  { key: "tiktok", label: "TikTok" },
];

/**
 * «Перенос музыки»: чужой плейлист по ссылке, скриншот списка (OCR),
 * просто список строк «Artist — Title» или трек из TikTok.
 * Превью/прогресс/сохранение общие: сервер в любом случае отдаёт job.
 */
export function ImportDialog({ onClose }: Props) {
  const play = usePlayer((s) => s.play);
  const toast = useToast((s) => s.show);

  const [mode, setMode] = useState<Mode>("link");
  const [url, setUrl] = useState("");
  const [ttUrl, setTtUrl] = useState("");
  const [text, setText] = useState("");
  const [shotName, setShotName] = useState("");
  const [preview, setPreview] = useState<ImportPreview | null>(null);
  const [checking, setChecking] = useState(false);
  const [starting, setStarting] = useState(false);
  const [ocrBusy, setOcrBusy] = useState(false);
  const [job, setJob] = useState<ImportJob | null>(null);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState("");

  const urlRef = useRef("");
  const jobRef = useRef("");
  const aliveRef = useRef(true);
  const fileRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    aliveRef.current = true;
    return () => {
      aliveRef.current = false;
    };
  }, []);

  const running = job?.state === "running";

  // опрос фоновой задачи каждые 1.2 с; останавливается на done/error и размонтировании
  useEffect(() => {
    const id = jobRef.current;
    if (!running || !id) return;
    let stopped = false;
    const timer = window.setInterval(() => {
      importer
        .status(id)
        .then((st) => {
          if (stopped || !aliveRef.current || jobRef.current !== id) return;
          if (st.state !== "running") {
            stopped = true;
            window.clearInterval(timer);
          }
          setJob(st);
        })
        .catch((e: unknown) => {
          if (stopped || !aliveRef.current || jobRef.current !== id) return;
          stopped = true;
          window.clearInterval(timer);
          const message =
            e instanceof Error ? e.message : "Не удалось получить статус импорта";
          setError(message);
          setJob((cur) =>
            cur ? { ...cur, state: "error", error: message } : cur,
          );
        });
    }, 1200);
    return () => {
      stopped = true;
      window.clearInterval(timer);
    };
  }, [running]);

  const reset = () => {
    jobRef.current = "";
    setJob(null);
    setPreview(null);
    setSaved(false);
    setError("");
  };

  const switchMode = (next: Mode) => {
    if (running || starting || ocrBusy) return;
    setMode(next);
    reset();
  };

  const changeUrl = (value: string) => {
    setUrl(value);
    urlRef.current = value.trim();
    setError("");
    if (running) return;
    setPreview(null);
    setJob(null);
    setSaved(false);
  };

  const check = async () => {
    const link = url.trim();
    urlRef.current = link;
    if (!link || checking || running || starting) return;
    setChecking(true);
    setError("");
    setSaved(false);
    try {
      const p = await importer.preview(link, 100);
      if (!aliveRef.current || urlRef.current !== link) return;
      setPreview(p);
      if (!p.total) setError("В плейлисте не нашлось треков");
    } catch (e) {
      if (!aliveRef.current || urlRef.current !== link) return;
      setPreview(null);
      setError(e instanceof Error ? e.message : "Не удалось прочитать плейлист");
    } finally {
      if (aliveRef.current) setChecking(false);
    }
  };

  const startJob = (res: ImportJobStart) => {
    jobRef.current = res.job;
    setJob({
      state: "running",
      done: 0,
      total: res.total,
      matched: 0,
      title: res.title,
      cover: res.cover || "",
      source: res.source,
      items: [],
    });
  };

  const startLink = async () => {
    const link = url.trim();
    urlRef.current = link;
    if (!link || starting || running) return;
    setStarting(true);
    setError("");
    setSaved(false);
    try {
      const res = await importer.start(link, 200);
      if (!aliveRef.current || urlRef.current !== link) return;
      startJob(res);
    } catch (e) {
      if (!aliveRef.current || urlRef.current !== link) return;
      setError(e instanceof Error ? e.message : "Не удалось начать импорт");
    } finally {
      if (aliveRef.current) setStarting(false);
    }
  };

  const startText = async (title: string) => {
    const value = text.trim();
    if (!value || starting || running) return;
    setStarting(true);
    setError("");
    setSaved(false);
    try {
      const res = await importer.text(value, title);
      if (!aliveRef.current) return;
      startJob(res);
    } catch (e) {
      if (!aliveRef.current) return;
      setError(e instanceof Error ? e.message : "Не удалось начать импорт");
    } finally {
      if (aliveRef.current) setStarting(false);
    }
  };

  const startTikTok = async () => {
    const link = ttUrl.trim();
    if (!link || starting || running) return;
    setStarting(true);
    setError("");
    setSaved(false);
    try {
      const res = await importer.tiktok(link);
      if (!aliveRef.current) return;
      startJob(res);
    } catch (e) {
      if (!aliveRef.current) return;
      setError(e instanceof Error ? e.message : "Не удалось прочитать TikTok");
    } finally {
      if (aliveRef.current) setStarting(false);
    }
  };

  const pickShot = async (file: File | undefined) => {
    if (!file || ocrBusy || running || starting) return;
    setOcrBusy(true);
    setError("");
    setShotName(file.name);
    try {
      const res = await importer.ocr(file);
      if (!aliveRef.current) return;
      const recognized = (res.text || "").trim();
      setText(recognized);
      if (!recognized) setError("На скриншоте не нашлось текста — попробуй другой снимок");
    } catch (e) {
      if (!aliveRef.current) return;
      setError(e instanceof Error ? e.message : "Не удалось распознать скриншот");
    } finally {
      if (aliveRef.current) setOcrBusy(false);
    }
  };

  const save = async () => {
    const items = job?.items || [];
    if (!items.length || saving) return;
    setSaving(true);
    setError("");
    const title = (job?.title || preview?.title || "").trim() || "Импорт";
    try {
      const pl = await playlists.create(title, "", 0);
      for (const t of items) {
        await playlists.add(pl.id, t);
      }
      if (!aliveRef.current) return;
      setSaved(true);
      toast("Плейлист сохранён");
    } catch (e) {
      if (!aliveRef.current) return;
      setError(e instanceof Error ? e.message : "Не удалось сохранить плейлист");
    } finally {
      if (aliveRef.current) setSaving(false);
    }
  };

  const items = job?.items || [];
  const metaTitle = job?.title || preview?.title || "";
  const metaCover = job?.cover || preview?.cover || "";
  const metaSource = job?.source || preview?.source || "";
  const metaTotal = job?.total || preview?.total || 0;
  const pct =
    job && job.total > 0
      ? Math.max(0, Math.min(100, Math.round((job.done / job.total) * 100)))
      : 0;
  const busy = running || starting || ocrBusy;

  return (
    <Dialog
      caption="Музыка"
      title="Перенос музыки"
      onClose={onClose}
      closeDisabled={running || starting || ocrBusy}
      wide
    >
      <div className="st-form">
        <div className="st-modes">
          {MODES.map((m) => (
            <button
              key={m.key}
              className={"st-mode" + (mode === m.key ? " on" : "")}
              disabled={busy}
              onClick={() => switchMode(m.key)}
            >
              {m.label}
            </button>
          ))}
        </div>

        {mode === "link" ? (
          <div className="st-field">
            <label className="st-label" htmlFor="st-import-url">
              Ссылка на плейлист
            </label>
            <input
              id="st-import-url"
              className="field"
              value={url}
              placeholder="https://open.spotify.com/playlist/…"
              disabled={busy}
              onChange={(e) => changeUrl(e.target.value)}
              onKeyDown={(e) => {
                if (e.key !== "Enter") return;
                e.preventDefault();
                if (preview) void startLink();
                else void check();
              }}
            />
            <div className="st-note">
              Spotify, Apple Music, YouTube, Яндекс, VK, Dotify, Muriel — подберём треки в SoundCloud
            </div>
          </div>
        ) : null}

        {mode === "shot" ? (
          <div className="st-field">
            <label className="st-label">Скриншот плейлиста</label>
            <div className="st-actions">
              <button
                className="btn btn-ghost st-btn-sm"
                disabled={busy}
                onClick={() => fileRef.current?.click()}
              >
                {ocrBusy ? "Распознаю…" : text ? "Другой скриншот" : "Выбрать скриншот"}
              </button>
              {shotName ? <span className="st-note">{shotName}</span> : null}
            </div>
            <input
              ref={fileRef}
              className="st-hidden"
              type="file"
              accept="image/*"
              onChange={(e) => {
                void pickShot(e.target.files?.[0]);
                e.target.value = "";
              }}
            />
            <textarea
              className="st-textarea"
              value={text}
              placeholder={"Здесь появится распознанный список.\nМожно поправить руками:\nArtist — Title"}
              disabled={busy}
              onChange={(e) => setText(e.target.value)}
            />
            <div className="st-note">
              Работает с тёмными скриншотами: сервер инвертирует и увеличивает картинку
            </div>
          </div>
        ) : null}

        {mode === "list" ? (
          <div className="st-field">
            <label className="st-label">Список треков</label>
            <textarea
              className="st-textarea"
              value={text}
              placeholder={"По одной строке:\nArtist — Title\nArtist - Title"}
              disabled={busy}
              onChange={(e) => setText(e.target.value)}
            />
            <div className="st-note">До 10 000 строк, разделитель — тире</div>
          </div>
        ) : null}

        {mode === "tiktok" ? (
          <div className="st-field">
            <label className="st-label" htmlFor="st-import-tt">
              Ссылка на TikTok
            </label>
            <input
              id="st-import-tt"
              className="field"
              value={ttUrl}
              placeholder="https://www.tiktok.com/@user/video/…"
              disabled={busy}
              onChange={(e) => setTtUrl(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  void startTikTok();
                }
              }}
            />
            <div className="st-note">
              Распознаём звук по видео и сразу кладём трек в «Мою музыку»
            </div>
          </div>
        ) : null}

        {!job ? (
          <div className="st-actions">
            {mode === "link" ? (
              <>
                <button
                  className="btn btn-ghost st-btn-sm"
                  disabled={!url.trim() || checking || busy}
                  onClick={() => void check()}
                >
                  {checking ? "Проверяю…" : "Проверить"}
                </button>
                <button
                  className="btn btn-primary st-btn-sm"
                  disabled={
                    !url.trim() || checking || busy || (preview !== null && !preview.total)
                  }
                  onClick={() => void startLink()}
                >
                  {starting ? "Запускаю…" : "Импортировать"}
                </button>
              </>
            ) : null}
            {mode === "shot" || mode === "list" ? (
              <button
                className="btn btn-primary st-btn-sm"
                disabled={!text.trim() || busy}
                onClick={() =>
                  void startText(mode === "shot" ? "Импорт по скриншоту" : "Импорт по тексту")
                }
              >
                {starting ? "Запускаю…" : "Импортировать"}
              </button>
            ) : null}
            {mode === "tiktok" ? (
              <button
                className="btn btn-primary st-btn-sm"
                disabled={!ttUrl.trim() || busy}
                onClick={() => void startTikTok()}
              >
                {starting ? "Слушаю звук…" : "Распознать трек"}
              </button>
            ) : null}
          </div>
        ) : null}

        {metaTitle || metaCover || (metaSource && job) ? (
          <div className="st-preview">
            <div className="st-preview-cover">
              <Cover src={metaCover} size={160} />
            </div>
            <div className="st-preview-info">
              <div className="st-preview-title">{metaTitle || "Плейлист"}</div>
              <div className="st-preview-sub">
                <span>{metaTotal} треков</span>
                {metaSource ? (
                  <span className={badgeClass(metaSource)}>{sourceLabel(metaSource)}</span>
                ) : null}
              </div>
            </div>
          </div>
        ) : null}

        {running && job ? (
          <div className="st-stack">
            <div className="st-progress-row">
              <span>Подбираю треки…</span>
              <span>
                {job.done} / {job.total}
              </span>
            </div>
            <div className="st-progress">
              <i style={{ width: `${pct}%` }} />
            </div>
          </div>
        ) : null}

        {job?.state === "error" ? (
          <div className="st-stack">
            <div className="st-alert">
              {job.error || error || "Импорт не удался"}
            </div>
            <div className="st-actions">
              <button className="btn btn-ghost st-btn-sm" onClick={reset}>
                Попробовать снова
              </button>
            </div>
          </div>
        ) : null}

        {job?.state === "done" ? (
          <div className="st-stack">
            <div className="st-note">
              Найдено {items.length} из {job.total} треков
            </div>
            {items.length ? (
              <div className="st-imp-list">
                {items.slice(0, 5).map((t, i) => (
                  <button
                    key={`${t.source}:${t.id}`}
                    className="st-imp-row"
                    title="Слушать"
                    onClick={() => play(items, i)}
                  >
                    <span className="st-imp-cover">
                      <Cover src={t.artwork} size={120} />
                    </span>
                    <span className="st-imp-info">
                      <span className="st-imp-title">{t.title}</span>
                      <span className="st-imp-artist">{t.artist}</span>
                    </span>
                    <span className="st-imp-dur">{fmtTime(t.duration)}</span>
                  </button>
                ))}
              </div>
            ) : (
              <div className="st-alert">Не удалось подобрать ни одного трека</div>
            )}
            {items.length > 5 ? (
              <div className="st-note">и ещё {items.length - 5}</div>
            ) : null}
            <div className="st-actions">
              <button
                className="btn btn-primary st-btn-sm"
                disabled={!items.length}
                onClick={() => play(items, 0)}
              >
                <PlayIcon />
                Слушать
              </button>
              <button
                className="btn btn-ghost st-btn-sm"
                disabled={!items.length || saving || saved}
                onClick={() => void save()}
              >
                {saving ? "Сохраняю…" : "Сохранить в плейлист"}
              </button>
            </div>
          </div>
        ) : null}

        {error && job?.state !== "error" ? (
          <div className="st-alert">{error}</div>
        ) : null}
      </div>
    </Dialog>
  );
}
