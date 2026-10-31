import { useEffect, useState } from "react";

export type UpdateStatus = {
  state: "checking" | "available" | "downloading" | "downloaded" | "latest" | "error";
  version?: string;
  percent?: number;
  message?: string;
};

const SKIP_KEY = "wonder.update.skip";

/**
 * Плашка обновления сверху, как в Android (UpdateBanner.kt):
 * «Обновите Wonder до версии X», тап — скачивание с процентами,
 * крестик — «не сейчас» (запоминаем версию и больше не показываем).
 */
export function UpdateBanner() {
  const api = typeof window !== "undefined" ? window.wonderDesktop : undefined;
  const [status, setStatus] = useState<UpdateStatus | null>(null);
  const [hidden, setHidden] = useState(false);

  const apply = (next: UpdateStatus) => {
    setStatus(next);
    if (next.state === "available" && next.version) {
      let skipped = "";
      try {
        skipped = localStorage.getItem(SKIP_KEY) || "";
      } catch {
        /* приватный режим */
      }
      setHidden(Boolean(skipped) && skipped === next.version);
    } else {
      setHidden(false);
    }
  };

  useEffect(() => {
    if (!api?.onUpdateStatus) return;
    const unsubscribe = api.onUpdateStatus(apply);
    // состояние могло прийти до подписки (проверка на старте приложения)
    void api.getUpdateStatus?.().then((s) => {
      if (s) apply(s);
    });
    return unsubscribe;
  }, [api]);

  const skip = () => {
    setHidden(true);
    if (status?.version) {
      try {
        localStorage.setItem(SKIP_KEY, status.version);
      } catch {
        /* приватный режим */
      }
    }
  };

  const act = () => {
    if (!api) return;
    if (status?.state === "available") void api.downloadUpdate?.();
    else if (status?.state === "downloaded") void api.installUpdate?.();
  };

  if (!api || !status || hidden) return null;
  const { state, version, percent } = status;
  if (state !== "available" && state !== "downloading" && state !== "downloaded") {
    return null;
  }
  const pct = Math.max(0, Math.min(100, percent || 0));

  return (
    <div
      className={"upd-banner" + (state === "downloading" ? " busy" : "")}
      role="status"
      onClick={act}
      title={
        state === "available"
          ? "Скачать обновление"
          : state === "downloaded"
            ? "Перезапустить и установить"
            : undefined
      }
    >
      <span className="upd-dot" />
      <div className="upd-text">
        {state === "available" ? (
          <>
            <b>Обновите Wonder до версии {version || ""}</b>
            <span>Нажмите, чтобы скачать</span>
          </>
        ) : null}
        {state === "downloading" ? (
          <>
            <b>Скачивание… {pct}%</b>
            <span className="upd-track">
              <i style={{ width: `${pct}%` }} />
            </span>
          </>
        ) : null}
        {state === "downloaded" ? (
          <>
            <b>Версия {version || ""} готова</b>
            <span>Нажмите, чтобы перезапустить и установить</span>
          </>
        ) : null}
      </div>
      {state === "downloaded" ? (
        <button
          className="upd-btn"
          onClick={(e) => {
            e.stopPropagation();
            void api.installUpdate?.();
          }}
        >
          Перезапустить
        </button>
      ) : null}
      {state === "downloading" ? <span className="upd-pct">{pct}%</span> : null}
      {state !== "downloading" ? (
        <button
          className="upd-close"
          title="Не сейчас"
          onClick={(e) => {
            e.stopPropagation();
            skip();
          }}
        >
          ×
        </button>
      ) : null}
    </div>
  );
}
