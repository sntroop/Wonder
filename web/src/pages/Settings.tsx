import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth, usePlayer } from "../store";
import { useToast } from "../toast";
import {
  ChevronLeft,
  ChevronRightIcon,
  FxIcon,
  PaintIcon,
  RefreshIcon,
  TrashIcon,
} from "../components/icons";
import { ImportDialog } from "../components/settings/ImportDialog";
import { UploadDialog } from "../components/settings/UploadDialog";
import { VkDialog } from "../components/settings/VkDialog";
import { FxDialog } from "../components/FxDialog";
import { SkinDialog } from "../components/SkinDialog";
import { InterestsDialog } from "../components/settings/InterestsDialog";

type DialogName = "import" | "upload" | "vk" | "fx" | "skin" | "interests" | null;

export function Settings() {
  const navigate = useNavigate();
  const { user, me, logout } = useAuth();
  const stop = usePlayer((s) => s.stop);
  const toast = useToast((s) => s.show);
  const [dialog, setDialog] = useState<DialogName>(null);

  const vkReady = Boolean(me?.vk?.ready);
  const vkRelay = Boolean(me?.vk?.relay);
  const scReady = Boolean(me?.sc?.connected);
  const vkStatus = vkReady
    ? "подключена"
    : vkRelay
      ? "через телефон"
      : "не подключена";

  const clearCache = async () => {
    try {
      if (typeof caches !== "undefined") {
        const keys = await caches.keys();
        await Promise.all(keys.map((key) => caches.delete(key)));
      }
      toast("Кэш очищен");
    } catch {
      toast("Не удалось очистить кэш");
    }
  };

  return (
    <div className="page st-page">
      <div className="st-head">
        <button className="icon-btn" onClick={() => navigate(-1)} title="Назад">
          <ChevronLeft />
        </button>
        <h1 className="st-title">Настройки</h1>
      </div>

      <div className="eyebrow st-eyebrow">Аккаунт</div>
      <div className="list">
        <div className="list-row">
          <div className="grow">
            <div>{user?.name || "Гость"}</div>
            <div className="hint">
              {user?.username ? `@${user.username}` : "без юзернейма"}
            </div>
          </div>
          <span className="val">{user?.owner ? "владелец" : "аккаунт"}</span>
        </div>
        <button
          className="list-row st-row-btn"
          onClick={() => navigate("/profile")}
        >
          <span className="grow">Редактировать профиль</span>
          <ChevronRightIcon />
        </button>
      </div>

      <div className="eyebrow st-eyebrow">Музыка</div>
      <div className="list">
        <div className="list-row">
          <div className="grow">
            <div>Перенос музыки</div>
            <div className="hint">ссылка, скриншот, список треков или TikTok</div>
          </div>
          <button
            className="btn btn-ghost st-btn-sm"
            onClick={() => setDialog("import")}
          >
            Открыть
          </button>
        </div>
        <div className="list-row">
          <div className="grow">
            <div>Интересы</div>
            <div className="hint">жанры и любимые артисты для «Моей волны»</div>
          </div>
          <button
            className="btn btn-ghost st-btn-sm"
            onClick={() => setDialog("interests")}
          >
            Выбрать
          </button>
        </div>
        <div className="list-row">
          <div className="grow">
            <div>Свой трек</div>
            <div className="hint">загрузить аудио и обложку</div>
          </div>
          <button
            className="btn btn-ghost st-btn-sm"
            onClick={() => setDialog("upload")}
          >
            Загрузить
          </button>
        </div>
        <button
          className="list-row st-row-btn"
          onClick={() => navigate("/profile")}
        >
          <span className="grow">Свои публикации</span>
          <ChevronRightIcon />
        </button>
      </div>

      <div className="eyebrow st-eyebrow">Подключения</div>
      <div className="list">
        <div className="list-row" title={vkReady ? undefined : me?.vk?.problem || undefined}>
          <div className="grow">
            <div>VK Музыка</div>
            <div className="hint">{vkStatus}</div>
          </div>
          {vkReady ? (
            <span className="val">активна</span>
          ) : (
            <button
              className="btn btn-primary st-btn-sm"
              onClick={() => setDialog("vk")}
            >
              Подключить
            </button>
          )}
        </div>
        <div className="list-row">
          <div className="grow">
            <div>SoundCloud</div>
            <div className="hint">{scReady ? "подключён" : "не подключён"}</div>
            {scReady ? null : (
              <div className="st-note">подключается в Windows-приложении</div>
            )}
          </div>
          <span className="val">{scReady ? "активен" : "выкл"}</span>
        </div>
      </div>

      <div className="eyebrow st-eyebrow">Звук и вид</div>
      <div className="list">
        <button className="list-row st-row-btn" onClick={() => setDialog("fx")}>
          <span className="st-row-ico">
            <FxIcon />
          </span>
          <span className="grow">
            <span>Эквалайзер</span>
            <span className="hint">10 полос, бас, предусиление и пресеты</span>
          </span>
          <ChevronRightIcon />
        </button>
        <button className="list-row st-row-btn" onClick={() => setDialog("skin")}>
          <span className="st-row-ico">
            <PaintIcon />
          </span>
          <span className="grow">
            <span>Оформление</span>
            <span className="hint">акцент, размытие фона, плотность панелей</span>
          </span>
          <ChevronRightIcon />
        </button>
      </div>

      <div className="eyebrow st-eyebrow">Приложение</div>
      <div className="list">
        <button
          className="list-row st-row-btn"
          onClick={() => window.location.reload()}
        >
          <span className="grow">Обновить приложение</span>
          <RefreshIcon />
        </button>
        <button
          className="list-row st-row-btn"
          onClick={() => navigate("/desktop")}
        >
          <span className="grow">Wonder для Windows</span>
          <span className="val">скачать</span>
        </button>
        <div className="list-row">
          <div className="grow">Горячие клавиши</div>
          <span className="val">Space · N · P · M · Q</span>
        </div>
        <div className="list-row">
          <div className="grow">Сборка</div>
          <span className="val">{__BUILD__}</span>
        </div>
        <button className="list-row st-row-btn" onClick={() => void clearCache()}>
          <span className="grow">Очистить кэш</span>
          <TrashIcon />
        </button>
        <button
          className="list-row st-row-btn"
          onClick={() => {
            stop();
            toast("Очередь очищена");
          }}
        >
          <span className="grow">Плеер</span>
          <span className="val">остановить</span>
        </button>
      </div>

      <div className="list st-logout">
        <button
          className="list-row st-row-btn st-danger"
          onClick={() => {
            stop();
            logout();
            navigate("/");
          }}
        >
          <span className="grow">Выйти из аккаунта</span>
        </button>
      </div>

      {dialog === "import" ? (
        <ImportDialog onClose={() => setDialog(null)} />
      ) : null}
      {dialog === "upload" ? (
        <UploadDialog onClose={() => setDialog(null)} />
      ) : null}
      {dialog === "vk" ? <VkDialog onClose={() => setDialog(null)} /> : null}
      {dialog === "fx" ? <FxDialog onClose={() => setDialog(null)} /> : null}
      {dialog === "skin" ? <SkinDialog onClose={() => setDialog(null)} /> : null}
      {dialog === "interests" ? (
        <InterestsDialog onClose={() => setDialog(null)} />
      ) : null}
    </div>
  );
}
