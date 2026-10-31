import { useCallback, useEffect, useRef, useState } from "react";
import { vkConnect, type VkQr } from "../../api";
import { useAuth } from "../../store";
import { useToast } from "../../toast";
import { Dialog } from "./Dialog";

type VkStatus = "waiting" | "scanned" | "ok" | "error";

type Props = { onClose: () => void };

/** Подключение VK Музыки по QR: старт, опрос статуса, автозакрытие. */
export function VkDialog({ onClose }: Props) {
  const toast = useToast((s) => s.show);

  const [qr, setQr] = useState<VkQr | null>(null);
  const [status, setStatus] = useState<VkStatus>("waiting");
  const [starting, setStarting] = useState(false);
  const [error, setError] = useState("");

  const stoppedRef = useRef(false);
  const idRef = useRef("");
  const closeTimer = useRef<number | undefined>(undefined);
  const closeRef = useRef(onClose);

  useEffect(() => {
    closeRef.current = onClose;
  }, [onClose]);

  const begin = useCallback(async () => {
    setStarting(true);
    setError("");
    setStatus("waiting");
    setQr(null);
    idRef.current = "";
    try {
      const s = await vkConnect.start();
      if (stoppedRef.current) return;
      idRef.current = s.id;
      setQr(s);
    } catch (e) {
      if (stoppedRef.current) return;
      setStatus("error");
      setError(e instanceof Error ? e.message : "Не удалось начать подключение");
    } finally {
      if (!stoppedRef.current) setStarting(false);
    }
  }, []);

  useEffect(() => {
    stoppedRef.current = false;
    void begin();
    return () => {
      stoppedRef.current = true;
      if (closeTimer.current !== undefined) {
        window.clearTimeout(closeTimer.current);
        closeTimer.current = undefined;
      }
    };
  }, [begin]);

  useEffect(() => {
    const id = qr?.id;
    if (!id) return;
    let stopped = false;
    let timer = 0;

    const tick = async () => {
      try {
        const r = await vkConnect.check(id);
        if (stopped || stoppedRef.current || idRef.current !== id) return;
        if (r.status === "ok") {
          setStatus("ok");
          window.clearInterval(timer);
          toast("VK подключён");
          void useAuth.getState().refresh();
          closeTimer.current = window.setTimeout(() => {
            if (!stoppedRef.current) closeRef.current();
          }, 1000);
        } else if (r.status === "error") {
          setStatus("error");
          setError(r.api_error || "VK не подтвердил вход");
          window.clearInterval(timer);
        } else if (r.status === "scanned") {
          setStatus("scanned");
        } else {
          setStatus("waiting");
        }
      } catch (e) {
        if (stopped || stoppedRef.current || idRef.current !== id) return;
        setStatus("error");
        setError(
          e instanceof Error ? e.message : "Не удалось проверить статус",
        );
        window.clearInterval(timer);
      }
    };

    timer = window.setInterval(() => void tick(), 2000);
    return () => {
      stopped = true;
      window.clearInterval(timer);
    };
  }, [qr?.id, toast]);

  const statusText =
    status === "scanned"
      ? "Подтверди вход в VK"
      : status === "ok"
        ? "VK подключён"
        : status === "error"
          ? "Не получилось"
          : "Ждём подтверждения";

  const retry = () => {
    void begin();
  };

  return (
    <Dialog caption="Подключения" title="VK Музыка" onClose={onClose}>
      {starting || (!qr && !error) ? (
        <div className="st-stack">
          <div className="st-status st-wait">Начинаю подключение…</div>
          <div className="st-progress st-indeterminate">
            <i />
          </div>
        </div>
      ) : null}

      {qr ? (
        <div className="st-vk">
          {qr.qr ? (
            <img className="st-qr-img" src={qr.qr} alt="QR-код для входа в VK" />
          ) : null}
          <div className="st-qr-text">
            Открой VK на телефоне → Настройки → QR-код и отсканируй
          </div>
          <div
            className={`st-status${
              status === "error"
                ? " st-err"
                : status === "waiting" || status === "scanned" || status === "ok"
                  ? " st-wait"
                  : ""
            }`}
          >
            {statusText}
          </div>
          <a
            className="st-qr-link"
            href={qr.url}
            target="_blank"
            rel="noreferrer"
          >
            Открыть ссылку на компьютере
          </a>
        </div>
      ) : null}

      {error ? (
        <div className="st-stack">
          <div className="st-alert">{error}</div>
          <div className="st-actions">
            <button className="btn btn-ghost st-btn-sm" onClick={retry}>
              Попробовать снова
            </button>
          </div>
        </div>
      ) : null}
    </Dialog>
  );
}
