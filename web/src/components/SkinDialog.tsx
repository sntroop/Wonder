import { useRef, useState } from "react";
import { Dialog } from "./settings/Dialog";
import {
  ACCENTS,
  FONTS,
  useSkin,
  type BgMode,
  type FontId,
} from "../skin";
import { useBg } from "../bg";

/** Оформление: акцент, фон (обложка или свой файл), размытие, панели, шрифт. */
export function SkinDialog({ onClose }: { onClose: () => void }) {
  const skin = useSkin((s) => s.skin);
  const update = useSkin((s) => s.update);
  const reset = useSkin((s) => s.reset);
  const bgUrl = useBg((s) => s.url);
  const bgKind = useBg((s) => s.kind);
  const bgName = useBg((s) => s.name);
  const [busy, setBusy] = useState(false);
  const fileRef = useRef<HTMLInputElement>(null);

  const pickFile = async (file: File | undefined) => {
    if (!file) return;
    setBusy(true);
    try {
      await useBg.getState().setFile(file);
      update({ bg: "custom" });
    } finally {
      setBusy(false);
    }
  };

  const setMode = (bg: BgMode) => {
    if (bg === "custom" && !bgUrl) {
      fileRef.current?.click();
      return;
    }
    update({ bg });
  };

  return (
    <Dialog caption="Интерфейс" title="Оформление" onClose={onClose} className="skin-dialog">
      <div className="st-form">
        <div className="st-field">
          <label className="st-label">Акцент</label>
          <div className="sk-accents">
            {ACCENTS.map((a) => (
              <button
                key={a.id}
                className={"sk-swatch" + (skin.accent === a.value ? " on" : "")}
                style={{ background: a.value }}
                title={a.name}
                onClick={() => update({ accent: a.value })}
              >
                <span className="sk-swatch-name">{a.name}</span>
              </button>
            ))}
            <label className="sk-custom" title="Свой цвет">
              <input
                type="color"
                value={skin.accent}
                onChange={(e) => update({ accent: e.target.value })}
              />
              <span>свой</span>
            </label>
          </div>
        </div>

        <div className="st-field">
          <label className="st-label">Шрифт</label>
          <div className="sk-fonts">
            {FONTS.map((f) => (
              <button
                key={f.id}
                className={"sk-font" + (skin.font === f.id ? " on" : "")}
                style={{ fontFamily: `"${f.ui}", sans-serif` }}
                onClick={() => update({ font: f.id as FontId })}
              >
                {f.name}
              </button>
            ))}
          </div>
        </div>

        <div className="st-field">
          <label className="st-label">Фон</label>
          <div className="sk-bg-row">
            <button
              className={"sk-bg-mode" + (skin.bg === "art" ? " on" : "")}
              onClick={() => setMode("art")}
            >
              Обложка трека
            </button>
            <button
              className={"sk-bg-mode" + (skin.bg === "custom" ? " on" : "")}
              onClick={() => setMode("custom")}
              disabled={busy}
            >
              {busy ? "Загружаю…" : "Своя картинка / видео"}
            </button>
          </div>

          {bgUrl ? (
            <div className="sk-bg-preview">
              {bgKind === "video" ? (
                <video src={bgUrl} muted loop autoPlay playsInline />
              ) : (
                <img src={bgUrl} alt="" />
              )}
              <div className="sk-bg-info">
                <b>{bgName || "свой фон"}</b>
                <span>{bgKind === "video" ? "видео" : "картинка или GIF"}</span>
              </div>
              <div className="sk-bg-actions">
                <button className="btn btn-ghost st-btn-sm" onClick={() => fileRef.current?.click()}>
                  Заменить
                </button>
                <button
                  className="btn btn-ghost st-btn-sm"
                  onClick={() => {
                    void useBg.getState().clear();
                    update({ bg: "art" });
                  }}
                >
                  Убрать
                </button>
              </div>
            </div>
          ) : (
            <div className="hint">
              Можно картинку, GIF или короткое видео — файл хранится на этом устройстве
            </div>
          )}

          <input
            ref={fileRef}
            className="st-hidden"
            type="file"
            accept="image/*,video/*"
            onChange={(e) => {
              void pickFile(e.target.files?.[0]);
              e.target.value = "";
            }}
          />
        </div>

        <label className="sk-range">
          <span>Размытие фона</span>
          <input
            type="range"
            min={0}
            max={120}
            step={2}
            value={skin.blur}
            onChange={(e) => update({ blur: Number(e.target.value) })}
          />
          <b>{skin.blur}px</b>
        </label>

        <label className="sk-range">
          <span>Затемнение</span>
          <input
            type="range"
            min={0}
            max={0.8}
            step={0.02}
            value={skin.dim}
            onChange={(e) => update({ dim: Number(e.target.value) })}
          />
          <b>{Math.round(skin.dim * 100)}%</b>
        </label>

        <label className="sk-range">
          <span>Плотность панелей</span>
          <input
            type="range"
            min={0.5}
            max={1}
            step={0.02}
            value={skin.surface}
            onChange={(e) => update({ surface: Number(e.target.value) })}
          />
          <b>{Math.round(skin.surface * 100)}%</b>
        </label>

        <div className="st-actions">
          <button className="btn btn-ghost st-btn-sm" onClick={() => reset()}>
            Вернуть как было
          </button>
          <span className="st-note">Действует сразу, хранится на этом устройстве</span>
        </div>
      </div>
    </Dialog>
  );
}
