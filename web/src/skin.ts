import { create } from "zustand";

/**
 * Оформление Wonder (как Skin.kt в Android): акцент, фон (обложка или своя
 * картинка/GIF/видео), размытие, затемнение, плотность поверхностей, шрифт.
 * Всё локально: localStorage + файл фона в IndexedDB (см. bg.ts).
 */

export type FontId = "unbounded" | "manrope" | "inter" | "jetbrains" | "ptserif";
export type BgMode = "art" | "custom";

export type Skin = {
  accent: string; // #rrggbb
  blur: number; // px, 0..120
  dim: number; // 0..0.8 - затемнение фона
  surface: number; // 0.5..1 - плотность дока и листа плеера
  font: FontId;
  bg: BgMode;
};

const KEY = "wonder.skin";

export const ACCENTS = [
  { id: "white", value: "#ffffff", name: "Белый" },
  { id: "lime", value: "#d1fe17", name: "Лайм" },
  { id: "sky", value: "#7cc4ff", name: "Небо" },
  { id: "rose", value: "#ff8fb1", name: "Роза" },
  { id: "amber", value: "#ffb066", name: "Янтарь" },
  { id: "violet", value: "#b18cff", name: "Фиолет" },
];

export const FONTS: Array<{ id: FontId; name: string; ui: string; display: string }> = [
  { id: "unbounded", name: "Unbounded", ui: "Inter", display: "Unbounded" },
  { id: "manrope", name: "Manrope", ui: "Manrope", display: "Manrope" },
  { id: "inter", name: "Inter", ui: "Inter", display: "Inter" },
  { id: "jetbrains", name: "JetBrains Mono", ui: "JetBrains Mono", display: "JetBrains Mono" },
  { id: "ptserif", name: "PT Serif", ui: "PT Serif", display: "PT Serif" },
];

export const DEFAULT_SKIN: Skin = {
  accent: "#ffffff",
  blur: 90,
  dim: 0.35,
  surface: 0.92,
  font: "unbounded",
  bg: "art",
};

const clamp = (v: number, lo: number, hi: number) =>
  Number.isFinite(v) ? Math.max(lo, Math.min(hi, v)) : lo;

function hex(v: string): string {
  const s = String(v || "").trim();
  return /^#[0-9a-f]{6}$/i.test(s) ? s.toLowerCase() : DEFAULT_SKIN.accent;
}

function fontOf(id?: string): FontId {
  return FONTS.some((f) => f.id === id) ? (id as FontId) : DEFAULT_SKIN.font;
}

/** Цвет текста на акценте: тёмный на светлом, белый на тёмном. */
function ink(accent: string): string {
  const h = hex(accent).slice(1);
  const r = parseInt(h.slice(0, 2), 16);
  const g = parseInt(h.slice(2, 4), 16);
  const b = parseInt(h.slice(4, 6), 16);
  const lum = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255;
  return lum > 0.62 ? "#0a0a0c" : "#ffffff";
}

export function normalizeSkin(raw: Partial<Skin> | null | undefined): Skin {
  if (!raw || typeof raw !== "object") return { ...DEFAULT_SKIN };
  return {
    accent: hex(raw.accent || DEFAULT_SKIN.accent),
    blur: clamp(Number(raw.blur ?? DEFAULT_SKIN.blur), 0, 120),
    dim: clamp(Number(raw.dim ?? DEFAULT_SKIN.dim), 0, 0.8),
    surface: clamp(Number(raw.surface ?? DEFAULT_SKIN.surface), 0.5, 1),
    font: fontOf(raw.font),
    bg: raw.bg === "custom" ? "custom" : "art",
  };
}

export function loadSkin(): Skin {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? normalizeSkin(JSON.parse(raw)) : { ...DEFAULT_SKIN };
  } catch {
    return { ...DEFAULT_SKIN };
  }
}

export function applySkin(skin: Skin): void {
  const s = normalizeSkin(skin);
  const font = FONTS.find((f) => f.id === s.font) || FONTS[0];
  const root = document.documentElement.style;
  root.setProperty("--pill", s.accent);
  root.setProperty("--accent", s.accent);
  root.setProperty("--accent-ink", ink(s.accent));
  root.setProperty("--bg-blur", `${s.blur}px`);
  root.setProperty("--bg-dim", String(s.dim));
  root.setProperty("--surf-a", String(s.surface));
  root.setProperty("--font-ui", `"${font.ui}"`);
  root.setProperty("--font-display", `"${font.display}"`);
  document.documentElement.dataset.font = s.font;
}

export function saveSkin(skin: Skin): Skin {
  const s = normalizeSkin(skin);
  applySkin(s);
  try {
    localStorage.setItem(KEY, JSON.stringify(s));
  } catch {
    /* приватный режим */
  }
  return s;
}

/* ── реактивное состояние для компонентов ─────────────────────────────── */

type SkinState = {
  skin: Skin;
  update: (patch: Partial<Skin>) => Skin;
  reset: () => Skin;
};

export const useSkin = create<SkinState>((set, get) => ({
  skin: loadSkin(),
  update(patch) {
    const next = saveSkin({ ...get().skin, ...patch });
    set({ skin: next });
    return next;
  },
  reset() {
    const next = saveSkin({ ...DEFAULT_SKIN });
    set({ skin: next });
    return next;
  },
}));

export { ink };
