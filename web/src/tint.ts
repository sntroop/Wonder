/**
 * Цвет из обложки — точный порт ArtworkTint.kt из Android-клиента.
 *
 * Берём 32x32, отбрасываем прозрачные/почти чёрные/почти белые пиксели,
 * считаем насыщенность, группируем в бакеты 64x64x64, выбираем самый
 * «живой» цвет и подтягиваем насыщенность/яркость в HSV.
 * Если цветных пикселей меньше 6% — обложка считается ч/б и фон нейтральный.
 */

const cache = new Map<string, string | null>();

function rgbToHsv(r: number, g: number, b: number): [number, number, number] {
  const rr = r / 255;
  const gg = g / 255;
  const bb = b / 255;
  const mx = Math.max(rr, gg, bb);
  const mn = Math.min(rr, gg, bb);
  const d = mx - mn;
  let h = 0;
  if (d !== 0) {
    if (mx === rr) h = ((gg - bb) / d) % 6;
    else if (mx === gg) h = (bb - rr) / d + 2;
    else h = (rr - gg) / d + 4;
    h *= 60;
    if (h < 0) h += 360;
  }
  return [h, mx === 0 ? 0 : d / mx, mx];
}

function hsvToRgb(h: number, s: number, v: number): [number, number, number] {
  const c = v * s;
  const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
  const m = v - c;
  let r = 0;
  let g = 0;
  let b = 0;
  if (h < 60) [r, g, b] = [c, x, 0];
  else if (h < 120) [r, g, b] = [x, c, 0];
  else if (h < 180) [r, g, b] = [0, c, x];
  else if (h < 240) [r, g, b] = [0, x, c];
  else if (h < 300) [r, g, b] = [x, 0, c];
  else [r, g, b] = [c, 0, x];
  return [
    Math.round((r + m) * 255),
    Math.round((g + m) * 255),
    Math.round((b + m) * 255),
  ];
}

function boost(r: number, g: number, b: number): [number, number, number] {
  let [h, s, v] = rgbToHsv(r, g, b);
  if (s < 0.55) s = 0.55;
  if (v < 0.85) v = 0.85;
  return hsvToRgb(h, s, v);
}

function analyze(img: HTMLImageElement): string | null {
  const size = 32;
  const canvas = document.createElement("canvas");
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext("2d", { willReadFrequently: true });
  if (!ctx) return null;
  ctx.drawImage(img, 0, 0, size, size);
  let data: Uint8ClampedArray;
  try {
    data = ctx.getImageData(0, 0, size, size).data;
  } catch {
    return null;
  }

  const buckets = new Map<number, { w: number; r: number; g: number; b: number; sat: number; n: number }>();
  for (let i = 0; i < data.length; i += 4) {
    const a = data[i + 3];
    if (a < 200) continue;
    const r = data[i];
    const g = data[i + 1];
    const b = data[i + 2];
    const mx = Math.max(r, g, b);
    const mn = Math.min(r, g, b);
    if (mx < 26 || mn > 235) continue;
    const sat = mx === 0 ? 0 : (mx - mn) / mx;
    if (sat < 0.1) continue;
    const key = ((r >> 6) << 6) | ((g >> 6) << 3) | (b >> 6);
    const bucket = buckets.get(key) || { w: 0, r: 0, g: 0, b: 0, sat: 0, n: 0 };
    const w = 0.25 + sat * sat;
    bucket.w += w;
    bucket.r += r * w;
    bucket.g += g * w;
    bucket.b += b * w;
    if (sat > bucket.sat) bucket.sat = sat;
    bucket.n += 1;
    buckets.set(key, bucket);
  }

  let best: (typeof buckets extends Map<number, infer V> ? V : never) | null = null;
  let bestScore = 0;
  for (const bucket of buckets.values()) {
    const score = bucket.w * (0.65 + bucket.sat);
    if (score > bestScore) {
      bestScore = score;
      best = bucket;
    }
  }
  if (!best) return null;

  const share = best.n / (size * size);
  if (share < 0.06) return null;

  const n = Math.max(0.001, best.w);
  const [r, g, b] = boost(best.r / n, best.g / n, best.b / n);
  return `#${[r, g, b].map((v) => v.toString(16).padStart(2, "0")).join("")}`;
}

export async function tintOf(url: string): Promise<string | null> {
  if (cache.has(url)) return cache.get(url) ?? null;
  const proxied = url.startsWith("/wonder/api")
    ? url
    : `/wonder/api/img?u=${encodeURIComponent(url)}&s=128`;
  try {
    const img = await new Promise<HTMLImageElement>((resolve, reject) => {
      const el = new Image();
      el.crossOrigin = "anonymous";
      el.onload = () => resolve(el);
      el.onerror = () => reject(new Error("img"));
      el.src = proxied;
    });
    const color = analyze(img);
    cache.set(url, color);
    return color;
  } catch {
    cache.set(url, null);
    return null;
  }
}
