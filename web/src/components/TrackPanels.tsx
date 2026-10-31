import { useEffect, useMemo, useRef, useState } from "react";
import {
  lyrics,
  remixes,
  social,
  type CommentItem,
  type LyricsData,
  type Track,
} from "../api";
import { usePlayer } from "../store";
import { useToast } from "../toast";
import { fmtTime, relTime } from "../utils";
import { Cover } from "./Cover";
import { ChevronLeft, CloseIcon, SendIcon, TrashIcon } from "./icons";

export function SheetPanel({
  title,
  onBack,
  children,
  right,
}: {
  title: string;
  onBack: () => void;
  children: React.ReactNode;
  right?: React.ReactNode;
}) {
  return (
    <div className="sheet-panel">
      <div className="sheet-panel-head">
        <button className="icon-btn" onClick={onBack} title="Назад">
          <ChevronLeft />
        </button>
        <b>{title}</b>
        <span className="panel-right">{right}</span>
      </div>
      <div className="sheet-panel-body">{children}</div>
    </div>
  );
}

/* ── текст песни ──────────────────────────────────────────────────────── */

export function LyricsPanel({
  track,
  onBack,
  side = false,
}: {
  track: Track;
  onBack: () => void;
  side?: boolean;
}) {
  const time = usePlayer((s) => s.time);
  const seek = usePlayer((s) => s.seek);
  const [data, setData] = useState<LyricsData | null>(null);
  const [error, setError] = useState("");
  const listRef = useRef<HTMLDivElement>(null);
  const lineRefs = useRef<Array<HTMLButtonElement | null>>([]);

  useEffect(() => {
    let alive = true;
    setData(null);
    setError("");
    lyrics(track)
      .then((d) => alive && setData(d))
      .catch((e) => {
        if (!alive) return;
        setError(e instanceof Error ? e.message : "Текст не загрузился");
      });
    return () => {
      alive = false;
    };
  }, [track.source, track.id]);

  const lines = useMemo(() => {
    if (!data) return [];
    if (data.synced && data.lines.length) {
      return data.lines.map((l) => ({ t: l.t / 1000, text: l.text }));
    }
    if (data.plain.trim()) {
      return data.plain
        .split("\n")
        .map((text) => text.trim())
        .filter(Boolean)
        .map((text) => ({ t: -1, text }));
    }
    return [];
  }, [data]);

  const active = useMemo(() => {
    if (!data?.synced) return -1;
    let idx = -1;
    for (let i = 0; i < lines.length; i += 1) {
      if (lines[i].t <= time + 0.12) idx = i;
      else break;
    }
    return idx;
  }, [lines, time, data?.synced]);

  useEffect(() => {
    if (active < 0) return;
    const el = lineRefs.current[active];
    if (!el) return;
    el.scrollIntoView({ block: "center", behavior: "smooth" });
  }, [active]);

  const body = (
    <>
      <div className="lyr-track">
        <Cover src={track.artwork} size={72} />
        <div>
          <b>{track.title}</b>
          <span>{track.artist}</span>
        </div>
      </div>

      {error ? (
        <div className="empty">{error}</div>
      ) : data === null ? (
        <div className="lyr-loading">
          <span className="spinner small" />
          Ищу текст…
        </div>
      ) : lines.length === 0 ? (
        <div className="empty">
          <div>Текста для этого трека нет</div>
          {data.instrumental ? <div className="hint">Это инструментал</div> : null}
        </div>
      ) : (
        <div className="lyr-list" ref={listRef}>
          {lines.map((line, i) => (
            <button
              key={`${i}-${line.t}`}
              ref={(el) => {
                lineRefs.current[i] = el;
              }}
              className={`lyr-line${i === active ? " on" : ""}${
                line.t < 0 ? " plain" : ""
              }`}
              onClick={() => {
                if (line.t >= 0) seek(line.t);
              }}
            >
              {line.text}
            </button>
          ))}
          {!data.synced ? (
            <div className="lyr-note">Без таймингов — синхронизация недоступна</div>
          ) : null}
        </div>
      )}
    </>
  );

  if (side) {
    return (
      <aside className="sheet-side">
        <div className="sheet-side-head">
          <b>Текст песни</b>
          <button className="icon-btn" onClick={onBack} title="Скрыть текст">
            <CloseIcon />
          </button>
        </div>
        {body}
      </aside>
    );
  }

  return (
    <SheetPanel title="Текст песни" onBack={onBack}>
      {body}
    </SheetPanel>
  );
}

/* ── ремиксы ──────────────────────────────────────────────────────────── */

export function RemixesPanel({
  track,
  onBack,
  onClose,
}: {
  track: Track;
  onBack: () => void;
  onClose: () => void;
}) {
  const [items, setItems] = useState<Track[] | null>(null);
  const [error, setError] = useState("");
  const play = usePlayer((s) => s.play);

  useEffect(() => {
    let alive = true;
    setItems(null);
    setError("");
    remixes(track)
      .then((r) => alive && setItems(r.items || []))
      .catch((e) => {
        if (!alive) return;
        setItems([]);
        setError(e instanceof Error ? e.message : "Ремиксы не нашлись");
      });
    return () => {
      alive = false;
    };
  }, [track.source, track.id]);

  return (
    <SheetPanel
      title="Ремиксы"
      onBack={onBack}
      right={<span className="hint">{items ? `${items.length}` : ""}</span>}
    >
      {error ? <div className="empty">{error}</div> : null}
      {items === null ? (
        <div className="ap-list">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="skeleton" style={{ height: 58 }} />
          ))}
        </div>
      ) : items.length ? (
        <div className="rm-list">
          {items.map((t, i) => (
            <button
              key={`${t.source}-${t.id}-${i}`}
              className="rm-row"
              onClick={() => {
                play(items, i);
                onClose();
              }}
            >
              <span className="rm-cover">
                <Cover src={t.artwork} size={120} />
              </span>
              <span className="rm-info">
                <b>{t.title}</b>
                <span>{t.artist}</span>
              </span>
              <span className="rm-dur">{fmtTime(t.duration)}</span>
            </button>
          ))}
        </div>
      ) : (
        <div className="empty">Ремиксов на этот трек нет</div>
      )}
    </SheetPanel>
  );
}

/* ── комментарии ──────────────────────────────────────────────────────── */

export function CommentsPanel({
  track,
  onBack,
  pos = 0,
}: {
  track: Track;
  onBack: () => void;
  pos?: number;
}) {
  const toast = useToast((s) => s.show);
  const [items, setItems] = useState<CommentItem[] | null>(null);
  const [likes, setLikes] = useState(0);
  const [text, setText] = useState("");
  const [sending, setSending] = useState(false);

  const load = () => {
    social
      .get(track)
      .then((r) => {
        setItems(r.comments || []);
        setLikes(r.likes || 0);
      })
      .catch(() => setItems([]));
  };

  useEffect(() => {
    setItems(null);
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [track.source, track.id]);

  const send = async () => {
    const value = text.trim();
    if (!value || sending) return;
    setSending(true);
    try {
      await social.comment(track, value, Math.round(pos));
      setText("");
      load();
    } catch (e) {
      toast(e instanceof Error ? e.message : "Не отправилось");
    } finally {
      setSending(false);
    }
  };

  const remove = async (id: number) => {
    try {
      await social.removeComment(id);
      setItems((prev) => (prev || []).filter((c) => c.id !== id));
    } catch {
      toast("Не получилось");
    }
  };

  return (
    <SheetPanel
      title={`Комментарии${items ? ` · ${items.length}` : ""}`}
      onBack={onBack}
      right={likes ? <span className="hint">{likes} ♥</span> : null}
    >
      <div className="cm-list">
        {items === null ? (
          <div className="spinner small" style={{ margin: "18px auto" }} />
        ) : items.length ? (
          items.map((c) => (
            <div className="cm-row" key={c.id}>
              <div className="cm-avatar">{(c.name || "?").slice(0, 1).toUpperCase()}</div>
              <div className="cm-info">
                <div className="cm-head">
                  <b>{c.name || "слушатель"}</b>
                  <span>{c.ts ? relTime(c.ts) : ""}</span>
                  {c.pos > 0 ? <i>на {fmtTime(c.pos)}</i> : null}
                </div>
                <div className="cm-text">{c.text}</div>
              </div>
              {c.mine ? (
                <button className="icon-btn cm-del" onClick={() => remove(c.id)} title="Удалить">
                  <TrashIcon />
                </button>
              ) : null}
            </div>
          ))
        ) : (
          <div className="empty">Пока никто не написал</div>
        )}
      </div>

      <div className="cm-compose">
        <input
          className="field"
          value={text}
          maxLength={400}
          placeholder="Написать комментарий…"
          onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") void send();
          }}
        />
        <button
          className="cm-send"
          disabled={!text.trim() || sending}
          onClick={() => void send()}
          title="Отправить"
        >
          <SendIcon />
        </button>
      </div>
    </SheetPanel>
  );
}

/* ── заглушка закрытия снаружи ────────────────────────────────────────── */

export function PanelClose({ onClose }: { onClose: () => void }) {
  return (
    <button className="icon-btn" onClick={onClose} title="Закрыть">
      <CloseIcon />
    </button>
  );
}
