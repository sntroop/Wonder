import { usePlayer } from "../store";
import { Cover } from "./Cover";
import { EqBars } from "./EqBars";
import { ChevronLeft, CloseIcon, NoteIcon } from "./icons";
import { fmtTime } from "../utils";

export function QueuePanel() {
  const open = usePlayer((s) => s.queueOpen);
  const setOpen = usePlayer((s) => s.setQueueOpen);
  const queue = usePlayer((s) => s.queue);
  const index = usePlayer((s) => s.index);
  const playing = usePlayer((s) => s.playing);
  const play = usePlayer((s) => s.play);
  const removeAt = usePlayer((s) => s.removeAt);
  const clearQueue = usePlayer((s) => s.clearQueue);

  const total = queue.reduce((sum, t) => sum + (t.duration || 0), 0);

  return (
    <aside className={`queue-panel${open ? " open" : ""}`} aria-hidden={!open}>
      <div className="queue-head">
        <div className="qp-head-text">
          <h4>Очередь · {queue.length}</h4>
          {queue.length ? <span className="qp-total">{fmtTime(total)}</span> : null}
        </div>
        <div className="qp-head-actions">
          {queue.length ? (
            <button className="qp-clear" onClick={clearQueue}>
              Очистить
            </button>
          ) : null}
          <button
            className="qp-close"
            onClick={() => setOpen(false)}
            title="Скрыть"
            aria-label="Скрыть очередь"
          >
            <ChevronLeft style={{ transform: "rotate(180deg)" }} />
          </button>
        </div>
      </div>

      <div className="queue-list">
        {queue.length === 0 ? (
          <div className="empty qp-empty">
            <NoteIcon className="qp-empty-icon" />
            <div>Пока пусто</div>
          </div>
        ) : (
          queue.map((t, i) => (
            <div
              key={`${t.source}-${t.id}-${i}`}
              className={`track qp-row${i === index ? " playing" : ""}`}
              onClick={() => play(queue, i)}
              role="button"
              tabIndex={0}
              aria-label={`Играть: ${t.title}`}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                  e.preventDefault();
                  play(queue, i);
                }
              }}
            >
              <div className="qp-index">
                {i === index ? (
                  <EqBars playing={playing} size={14} />
                ) : (
                  <span>{i + 1}</span>
                )}
              </div>
              <div className="track-cover qp-cover">
                <Cover src={t.artwork} size={80} />
              </div>
              <div className="track-info">
                <div className="track-title qp-title">{t.title}</div>
                <div className="track-artist">{t.artist}</div>
              </div>
              <div className="track-dur qp-dur">{fmtTime(t.duration)}</div>
              <button
                className="qp-row-del"
                title="Убрать из очереди"
                aria-label={`Убрать «${t.title}» из очереди`}
                onClick={(e) => {
                  e.stopPropagation();
                  removeAt(i);
                }}
                onKeyDown={(e) => e.stopPropagation()}
              >
                <CloseIcon />
              </button>
            </div>
          ))
        )}
      </div>
    </aside>
  );
}
