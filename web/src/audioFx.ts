/**
 * Эквалайзер Wonder для веба — полная цепочка как в Android (audio/Fx.kt,
 * FxProcessor.kt): предусиление → 10 полос → бас → дистошн → реверб →
 * лимитер. Плюс темп и плавный переход между треками.
 *
 * Настройки живут в localStorage (`wonder.fx`), аудиограф читает готовый
 * снимок; UI подменяет его целиком через setFx().
 */

export const FX_BANDS = [31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000];
export const FX_LABELS = ["31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k"];

export type FxSettings = {
  enabled: boolean;
  preamp: number; // дБ -12..+12
  bands: number[]; // 10 полос, дБ -12..+12
  bass: number; // дБ 0..15
  reverbMix: number; // 0..1
  reverbSize: number; // 0..1
  distDrive: number; // 0..1
  distTone: number; // 0..1
  tempo: number; // 0.5..2
  crossfade: number; // 0..12 c (плавное затухание при смене трека)
};

export type FxPreset = {
  id: string;
  name: string;
  bands: number[];
  preamp?: number;
  bass?: number;
};

export type CustomPreset = {
  name: string;
  bands: number[];
  preamp: number;
  bass: number;
};

const KEY = "wonder.fx";
const CUSTOM_KEY = "wonder.fx.custom";

const clamp = (v: number, lo: number, hi: number) =>
  Number.isFinite(v) ? Math.max(lo, Math.min(hi, v)) : lo;

/** Пресеты 1:1 с Android (Fx.presets). */
export const FX_PRESETS: FxPreset[] = [
  { id: "flat", name: "Плоский", bands: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0] },
  {
    id: "bass",
    name: "Бас",
    bands: [7, 6, 4.5, 2.5, 0.5, 0, -0.5, 0, 1, 1.5],
    preamp: -2,
    bass: 4,
  },
  { id: "vocal", name: "Вокал", bands: [-3, -2, 0, 2, 4.5, 4.5, 3, 1.5, 0, -1] },
  { id: "rock", name: "Рок", bands: [4, 3, 2, 0.5, -1, -0.5, 1.5, 3, 4, 3.5], preamp: -1.5 },
  { id: "pop", name: "Поп", bands: [-1, 0, 1.5, 3, 2.5, 0.5, -0.5, 1, 2, 2.5] },
  {
    id: "electronic",
    name: "Электро",
    bands: [5, 4, 2, 0, -1.5, 1, 2, 3, 4, 4.5],
    preamp: -1,
    bass: 3,
  },
  { id: "night", name: "Ночь", bands: [2, 1, 0, -1, -2, -2.5, -2, -1, 0, 1], preamp: 1 },
  {
    id: "hard",
    name: "Хардстайл",
    bands: [8, 7, 4, 1, -2, -1, 1, 3, 5, 4],
    preamp: -3,
    bass: 5,
  },
];

export function defaultFx(): FxSettings {
  return {
    enabled: false,
    preamp: 0,
    bands: FX_BANDS.map(() => 0),
    bass: 0,
    reverbMix: 0,
    reverbSize: 0.5,
    distDrive: 0,
    distTone: 0.5,
    tempo: 1,
    crossfade: 0,
  };
}

export function normalizeFx(raw: Partial<FxSettings> | null | undefined): FxSettings {
  const d = defaultFx();
  if (!raw || typeof raw !== "object") return d;
  const bands = Array.isArray(raw.bands) ? raw.bands : [];
  return {
    enabled: Boolean(raw.enabled),
    preamp: clamp(Number(raw.preamp ?? 0), -12, 12),
    bands: FX_BANDS.map((_, i) => clamp(Number(bands[i] ?? 0), -12, 12)),
    bass: clamp(Number(raw.bass ?? 0), 0, 15),
    reverbMix: clamp(Number(raw.reverbMix ?? 0), 0, 1),
    reverbSize: clamp(Number(raw.reverbSize ?? 0.5), 0, 1),
    distDrive: clamp(Number(raw.distDrive ?? 0), 0, 1),
    distTone: clamp(Number(raw.distTone ?? 0.5), 0, 1),
    tempo: clamp(Number(raw.tempo ?? 1), 0.5, 2),
    crossfade: clamp(Number(raw.crossfade ?? 0), 0, 12),
  };
}

export function loadFx(): FxSettings {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? normalizeFx(JSON.parse(raw)) : defaultFx();
  } catch {
    return defaultFx();
  }
}

/* ── пользовательские пресеты (3 слота, как в Android) ───────────────── */

export function loadCustomPresets(): Array<CustomPreset | null> {
  try {
    const raw = localStorage.getItem(CUSTOM_KEY);
    const list = raw ? (JSON.parse(raw) as Array<CustomPreset | null>) : [];
    return [0, 1, 2].map((i) => {
      const p = list[i];
      if (!p || !Array.isArray(p.bands)) return null;
      return {
        name: String(p.name || `Мой ${i + 1}`),
        bands: FX_BANDS.map((_, k) => clamp(Number(p.bands[k] ?? 0), -12, 12)),
        preamp: clamp(Number(p.preamp ?? 0), -12, 12),
        bass: clamp(Number(p.bass ?? 0), 0, 15),
      };
    });
  } catch {
    return [null, null, null];
  }
}

export function saveCustomPreset(slot: number, preset: CustomPreset): void {
  const list = loadCustomPresets();
  list[clamp(slot, 0, 2)] = preset;
  try {
    localStorage.setItem(CUSTOM_KEY, JSON.stringify(list));
  } catch {
    /* приватный режим */
  }
}

export function deleteCustomPreset(slot: number): void {
  const list = loadCustomPresets();
  list[clamp(slot, 0, 2)] = null;
  try {
    localStorage.setItem(CUSTOM_KEY, JSON.stringify(list));
  } catch {
    /* приватный режим */
  }
}

/* ── аудиограф ───────────────────────────────────────────────────────── */

type Graph = {
  ctx: AudioContext;
  source: MediaElementAudioSourceNode;
  preamp: GainNode;
  filters: BiquadFilterNode[];
  bassFilter: BiquadFilterNode;
  distDry: GainNode;
  shaper: WaveShaperNode;
  tone: BiquadFilterNode;
  distWet: GainNode;
  postDist: GainNode;
  revDry: GainNode;
  convolver: ConvolverNode;
  revWet: GainNode;
  limiter: DynamicsCompressorNode;
  fade: GainNode;
};

let graph: Graph | null = null;
let current = loadFx();
let impulseKey = "";
let fadeTimer: number | null = null;

/** Подключаем аудиоэлемент к цепочке фильтров (один раз за жизнь элемента). */
export function attachFx(audio: HTMLAudioElement): void {
  if (graph) return;
  try {
    const Ctx: typeof AudioContext =
      (window as unknown as { AudioContext: typeof AudioContext }).AudioContext ||
      (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
    if (!Ctx) return;
    const ctx = new Ctx();

    const source = ctx.createMediaElementSource(audio);
    const preamp = ctx.createGain();

    const filters = FX_BANDS.map((freq) => {
      const b = ctx.createBiquadFilter();
      b.type = "peaking";
      b.frequency.value = freq;
      b.Q.value = 1.4;
      b.gain.value = 0;
      return b;
    });

    const bassFilter = ctx.createBiquadFilter();
    bassFilter.type = "lowshelf";
    bassFilter.frequency.value = 90;
    bassFilter.gain.value = 0;

    const distDry = ctx.createGain();
    const shaper = ctx.createWaveShaper();
    shaper.oversample = "2x";
    const tone = ctx.createBiquadFilter();
    tone.type = "lowpass";
    tone.frequency.value = 6000;
    const distWet = ctx.createGain();
    const postDist = ctx.createGain();

    const revDry = ctx.createGain();
    const convolver = ctx.createConvolver();
    const revWet = ctx.createGain();

    const limiter = ctx.createDynamicsCompressor();
    limiter.threshold.value = -3;
    limiter.knee.value = 0;
    limiter.ratio.value = 20;
    limiter.attack.value = 0.003;
    limiter.release.value = 0.25;

    const fade = ctx.createGain();

    // основная цепочка: source → preamp → 10 полос → бас
    let node: AudioNode = source;
    node.connect(preamp);
    node = preamp;
    for (const f of filters) {
      node.connect(f);
      node = f;
    }
    node.connect(bassFilter);

    // дистошн: сухой + мокрый (waveshaper → тон)
    bassFilter.connect(distDry);
    distDry.connect(postDist);
    bassFilter.connect(shaper);
    shaper.connect(tone);
    tone.connect(distWet);
    distWet.connect(postDist);

    // реверб: сухой + мокрый (convolver)
    postDist.connect(revDry);
    revDry.connect(limiter);
    postDist.connect(convolver);
    convolver.connect(revWet);
    revWet.connect(limiter);

    limiter.connect(fade);
    fade.connect(ctx.destination);

    graph = {
      ctx,
      source,
      preamp,
      filters,
      bassFilter,
      distDry,
      shaper,
      tone,
      distWet,
      postDist,
      revDry,
      convolver,
      revWet,
      limiter,
      fade,
    };
    buildImpulse();
    applyFx();
  } catch (e) {
    console.warn("[wonder] эквалайзер недоступен", e);
  }
}

/** Возобновляем контекст: браузер держит его на паузе до действия пользователя. */
export function resumeFx(): void {
  if (graph && graph.ctx.state === "suspended") {
    graph.ctx.resume().catch(() => {});
  }
}

/** Импульсный отклик для реверба: шум с экспоненциальным затуханием. */
function buildImpulse(): void {
  if (!graph) return;
  const size = current.reverbSize;
  const key = size.toFixed(2);
  if (key === impulseKey) return;
  impulseKey = key;
  const ctx = graph.ctx;
  const seconds = 0.3 + size * 2.7;
  const len = Math.max(1, Math.floor(ctx.sampleRate * seconds));
  const buf = ctx.createBuffer(2, len, ctx.sampleRate);
  const decay = 2.2 + (1 - size) * 2.2;
  for (let ch = 0; ch < 2; ch += 1) {
    const data = buf.getChannelData(ch);
    for (let i = 0; i < len; i += 1) {
      data[i] = (Math.random() * 2 - 1) * Math.pow(1 - i / len, decay);
    }
  }
  graph.convolver.buffer = buf;
}

function makeCurve(drive: number) {
  const n = 1024;
  const curve = new Float32Array(n);
  const k = 1 + drive * 22;
  for (let i = 0; i < n; i += 1) {
    const x = (i * 2) / n - 1;
    curve[i] = Math.tanh(x * k) * 0.7;
  }
  return curve;
}

export function applyFx(settings: FxSettings = current): void {
  current = settings;
  if (!graph) return;
  const on = settings.enabled;
  const g = graph;

  g.preamp.gain.value = on ? Math.pow(10, settings.preamp / 20) : 1;
  g.filters.forEach((f, i) => {
    f.gain.value = on ? settings.bands[i] ?? 0 : 0;
  });
  g.bassFilter.gain.value = on ? settings.bass : 0;

  // дистошн: как в Android - микс dry/wet по drive, тон режет верх
  const drive = on ? settings.distDrive : 0;
  g.distDry.gain.value = 1 - drive;
  g.distWet.gain.value = drive;
  if (drive > 0.001) {
    g.shaper.curve = makeCurve(drive);
    g.tone.frequency.value = 600 + (on ? settings.distTone : 0.5) * 11400;
  }

  // реверб: dry = 1 - mix*0.55, wet = mix*0.9 (как в FxProcessor)
  const mix = on ? settings.reverbMix : 0;
  g.revDry.gain.value = 1 - mix * 0.55;
  g.revWet.gain.value = mix * 0.9;
  buildImpulse();

  g.fade.gain.value = g.fade.gain.value || 1;
}

/** Плавное затухание/нарастание (кроссфейд при смене трека). */
export function fadeTo(target: number, ms: number): Promise<void> {
  if (!graph) return Promise.resolve();
  if (fadeTimer !== null) {
    window.clearTimeout(fadeTimer);
    fadeTimer = null;
  }
  const g = graph;
  const now = g.ctx.currentTime;
  const value = Math.max(0, Math.min(1, target));
  g.fade.gain.cancelScheduledValues(now);
  g.fade.gain.setValueAtTime(g.fade.gain.value, now);
  if (ms <= 0) {
    g.fade.gain.setValueAtTime(value, now);
    return Promise.resolve();
  }
  g.fade.gain.linearRampToValueAtTime(value, now + ms / 1000);
  return new Promise((resolve) => {
    fadeTimer = window.setTimeout(() => {
      fadeTimer = null;
      resolve();
    }, ms);
  });
}

/* ── АЧХ для графика ──────────────────────────────────────────────────── */

export type CurvePoint = { f: number; db: number };

let curveCtx: OfflineAudioContext | null = null;

/** Суммарная АЧХ полос + баса (для кривой в редакторе). */
export function fxCurve(settings: FxSettings, points = 40): CurvePoint[] {
  try {
    if (!curveCtx) curveCtx = new OfflineAudioContext(1, 1, 44100);
    const freqs = new Float32Array(points);
    for (let i = 0; i < points; i += 1) {
      const t = i / (points - 1);
      freqs[i] = 20 * Math.pow(1000, t); // 20 Гц .. 20 кГц
    }
    const total = new Float32Array(points);
    const out = new Float32Array(points);
    const phase = new Float32Array(points);
    FX_BANDS.forEach((freq, i) => {
      const b = curveCtx!.createBiquadFilter();
      b.type = "peaking";
      b.frequency.value = freq;
      b.Q.value = 1.4;
      b.gain.value = settings.enabled ? clamp(settings.bands[i] ?? 0, -12, 12) : 0;
      b.getFrequencyResponse(freqs, out, phase);
      for (let k = 0; k < points; k += 1) total[k] += 20 * Math.log10(Math.max(out[k], 1e-6));
    });
    const shelf = curveCtx.createBiquadFilter();
    shelf.type = "lowshelf";
    shelf.frequency.value = 90;
    shelf.gain.value = settings.enabled ? settings.bass : 0;
    shelf.getFrequencyResponse(freqs, out, phase);
    for (let k = 0; k < points; k += 1) total[k] += 20 * Math.log10(Math.max(out[k], 1e-6));
    const pre = settings.enabled ? settings.preamp : 0;
    return Array.from(freqs, (f, i) => ({ f, db: total[i] + pre }));
  } catch {
    return [];
  }
}

/* ── состояние/подписки ───────────────────────────────────────────────── */

const listeners = new Set<(s: FxSettings) => void>();

export function getFx(): FxSettings {
  return current;
}

export function setFx(next: FxSettings): FxSettings {
  current = normalizeFx(next);
  impulseKey = "";
  applyFx(current);
  try {
    localStorage.setItem(KEY, JSON.stringify(current));
  } catch {
    /* приватный режим */
  }
  listeners.forEach((cb) => {
    try {
      cb(current);
    } catch {
      /* ошибка подписчика не должна ломать звук */
    }
  });
  return current;
}

export function subscribeFx(cb: (s: FxSettings) => void): () => void {
  listeners.add(cb);
  return () => listeners.delete(cb);
}
