import { useMemo, useState } from "react";
import { Dialog } from "./settings/Dialog";
import {
  FX_BANDS,
  FX_LABELS,
  FX_PRESETS,
  deleteCustomPreset,
  fxCurve,
  getFx,
  loadCustomPresets,
  saveCustomPreset,
  setFx,
  type CustomPreset,
  type FxPreset,
  type FxSettings,
} from "../audioFx";

const RANGE = 12;

type SliderProps = {
  label: string;
  hint?: string;
  value: number;
  min: number;
  max: number;
  step: number;
  suffix?: string;
  disabled?: boolean;
  onChange: (v: number) => void;
};

function Slider({ label, hint, value, min, max, step, suffix = "", disabled, onChange }: SliderProps) {
  return (
    <label className="fx-row">
      <span className="fx-row-name">
        {label}
        {hint ? <i>{hint}</i> : null}
      </span>
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        disabled={disabled}
        onChange={(e) => onChange(Number(e.target.value))}
      />
      <b>
        {value > 0 && min < 0 ? "+" : ""}
        {step < 1 ? value.toFixed(2).replace(/\.?0+$/, "") : Math.round(value)}
        {suffix}
      </b>
    </label>
  );
}

/** Эквалайзер как в Android: 10 полос, бас, реверб, дистошн, темп, пресеты. */
export function FxDialog({ onClose }: { onClose: () => void }) {
  const [fx, setLocal] = useState<FxSettings>(() => {
    const s = getFx();
    return { ...s, bands: [...s.bands] };
  });
  const [custom, setCustom] = useState<Array<CustomPreset | null>>(() => loadCustomPresets());

  const patch = (next: Partial<FxSettings>) => {
    const value = { ...fx, ...next, bands: next.bands ? [...next.bands] : [...fx.bands] };
    setLocal(value);
    setFx(value);
  };

  const applyPreset = (p: FxPreset) => {
    patch({
      bands: p.bands,
      preamp: p.preamp ?? 0,
      bass: p.bass ?? 0,
      enabled: true,
    });
  };

  const applyCustom = (p: CustomPreset) => {
    patch({ bands: p.bands, preamp: p.preamp, bass: p.bass, enabled: true });
  };

  const samePreset = (p: FxPreset) =>
    p.bands.every((b, i) => Math.abs(b - (fx.bands[i] ?? 0)) < 0.01) &&
    Math.abs((p.preamp ?? 0) - fx.preamp) < 0.01 &&
    Math.abs((p.bass ?? 0) - fx.bass) < 0.01;

  const curve = useMemo(() => fxCurve(fx, 48), [fx]);
  const curvePoints = useMemo(() => {
    if (!curve.length) return "";
    return curve
      .map((p, i) => {
        const x = (i / (curve.length - 1)) * 100;
        const y = 30 - Math.max(-15, Math.min(15, p.db)) * 1.55;
        return `${x.toFixed(2)},${y.toFixed(2)}`;
      })
      .join(" ");
  }, [curve]);

  const peak = useMemo(() => {
    let min = 0;
    let max = 0;
    curve.forEach((p) => {
      min = Math.min(min, p.db);
      max = Math.max(max, p.db);
    });
    return { min, max };
  }, [curve]);

  const saveToSlot = (slot: number) => {
    saveCustomPreset(slot, {
      name: `Мой ${slot + 1}`,
      bands: [...fx.bands],
      preamp: fx.preamp,
      bass: fx.bass,
    });
    setCustom(loadCustomPresets());
  };

  const removeSlot = (slot: number) => {
    deleteCustomPreset(slot);
    setCustom(loadCustomPresets());
  };

  return (
    <Dialog
      caption="Звук"
      title="Эквалайзер"
      onClose={onClose}
      className="fx-dialog"
    >
      <div className="st-form">
        <div className="fx-head">
          <button
            className={"fx-switch" + (fx.enabled ? " on" : "")}
            onClick={() => patch({ enabled: !fx.enabled })}
            aria-pressed={fx.enabled}
            title="Включить обработку"
          >
            <span className="fx-switch-knob" />
          </button>
          <div className="fx-head-text">
            <b>{fx.enabled ? "Обработка включена" : "Звук как с источника"}</b>
            <span>10 полос · бас · реверб · дистошн · темп</span>
          </div>
        </div>

        <div className="fx-presets">
          {FX_PRESETS.map((p) => (
            <button
              key={p.id}
              className={"fx-preset" + (samePreset(p) ? " on" : "")}
              disabled={!fx.enabled}
              onClick={() => applyPreset(p)}
            >
              {p.name}
            </button>
          ))}
        </div>

        <div className="fx-curve">
          <svg viewBox="0 0 100 60" preserveAspectRatio="none" aria-hidden>
            <line x1="0" y1="30" x2="100" y2="30" className="fx-curve-zero" />
            <line x1="0" y1="13" x2="100" y2="13" className="fx-curve-grid" />
            <line x1="0" y1="47" x2="100" y2="47" className="fx-curve-grid" />
            {[0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((i) => (
              <line
                key={i}
                x1={(i / 9) * 100}
                y1="0"
                x2={(i / 9) * 100}
                y2="60"
                className="fx-curve-grid"
              />
            ))}
            <polyline points={curvePoints} className="fx-curve-line" />
          </svg>
          <span className="fx-curve-peak">
            {peak.max > 0 ? `+${peak.max.toFixed(1)}` : peak.max.toFixed(1)} /{" "}
            {peak.min.toFixed(1)} дБ
          </span>
        </div>

        <div className="fx-bands">
          {FX_BANDS.map((_, i) => (
            <div className="fx-band" key={i}>
              <span className="fx-band-val">
                {fx.bands[i] > 0 ? `+${fx.bands[i]}` : fx.bands[i]}
              </span>
              <div className="fx-band-box">
                <input
                  type="range"
                  min={-RANGE}
                  max={RANGE}
                  step={0.5}
                  value={fx.bands[i]}
                  disabled={!fx.enabled}
                  aria-label={`${FX_LABELS[i]} Гц`}
                  onChange={(e) => {
                    const bands = [...fx.bands];
                    bands[i] = Number(e.target.value);
                    patch({ bands });
                  }}
                />
              </div>
              <span className="fx-band-label">{FX_LABELS[i]}</span>
            </div>
          ))}
        </div>

        <div className="fx-sliders">
          <Slider
            label="Предусиление"
            value={fx.preamp}
            min={-12}
            max={12}
            step={0.5}
            suffix=" дБ"
            disabled={!fx.enabled}
            onChange={(v) => patch({ preamp: v })}
          />
          <Slider
            label="Басы"
            hint="полка 90 Гц"
            value={fx.bass}
            min={0}
            max={15}
            step={0.5}
            suffix=" дБ"
            disabled={!fx.enabled}
            onChange={(v) => patch({ bass: v })}
          />
          <Slider
            label="Реверб"
            hint="доля в сигнале"
            value={Math.round(fx.reverbMix * 100)}
            min={0}
            max={100}
            step={1}
            suffix="%"
            disabled={!fx.enabled}
            onChange={(v) => patch({ reverbMix: v / 100 })}
          />
          <Slider
            label="Размер зала"
            value={Math.round(fx.reverbSize * 100)}
            min={0}
            max={100}
            step={1}
            suffix="%"
            disabled={!fx.enabled || fx.reverbMix <= 0}
            onChange={(v) => patch({ reverbSize: v / 100 })}
          />
          <Slider
            label="Дистошн"
            hint="сатурация"
            value={Math.round(fx.distDrive * 100)}
            min={0}
            max={100}
            step={1}
            suffix="%"
            disabled={!fx.enabled}
            onChange={(v) => patch({ distDrive: v / 100 })}
          />
          <Slider
            label="Тон"
            hint="темнее / ярче"
            value={Math.round(fx.distTone * 100)}
            min={0}
            max={100}
            step={1}
            suffix="%"
            disabled={!fx.enabled || fx.distDrive <= 0}
            onChange={(v) => patch({ distTone: v / 100 })}
          />
          <Slider
            label="Темп"
            hint="без смены тона"
            value={fx.tempo}
            min={0.5}
            max={2}
            step={0.01}
            suffix="×"
            disabled={!fx.enabled}
            onChange={(v) => patch({ tempo: v })}
          />
          <Slider
            label="Переход"
            hint="затухание при смене"
            value={fx.crossfade}
            min={0}
            max={12}
            step={1}
            suffix=" c"
            disabled={!fx.enabled}
            onChange={(v) => patch({ crossfade: v })}
          />
        </div>

        <div className="fx-custom">
          <div className="fx-custom-head">
            <span>Мои пресеты</span>
            <div className="fx-custom-save">
              {[0, 1, 2].map((slot) => (
                <button
                  key={slot}
                  className="fx-preset"
                  disabled={!fx.enabled}
                  title={`Сохранить настройки в слот ${slot + 1}`}
                  onClick={() => saveToSlot(slot)}
                >
                  Записать {slot + 1}
                </button>
              ))}
            </div>
          </div>
          <div className="fx-presets">
            {custom.map((p, slot) =>
              p ? (
                <span className="fx-chip" key={slot}>
                  <button className="fx-chip-apply" onClick={() => applyCustom(p)}>
                    {p.name}
                  </button>
                  <button
                    className="fx-chip-del"
                    title="Удалить пресет"
                    onClick={() => removeSlot(slot)}
                  >
                    ×
                  </button>
                </span>
              ) : null,
            )}
            {custom.every((p) => !p) ? (
              <span className="st-note">Пока пусто — настрой полосы и нажми «Записать»</span>
            ) : null}
          </div>
        </div>

        <div className="st-actions">
          <button
            className="btn btn-ghost st-btn-sm"
            onClick={() => {
              const next = {
                ...fx,
                bands: FX_BANDS.map(() => 0),
                bass: 0,
                preamp: 0,
                reverbMix: 0,
                reverbSize: 0.5,
                distDrive: 0,
                distTone: 0.5,
                tempo: 1,
                crossfade: 0,
              };
              setLocal(next);
              setFx(next);
            }}
          >
            Сбросить всё
          </button>
          <span className="st-note">Настройки сохраняются на этом устройстве</span>
        </div>
      </div>
    </Dialog>
  );
}
