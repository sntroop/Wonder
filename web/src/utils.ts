export function fmtTime(seconds?: number): string {
  if (!seconds || !Number.isFinite(seconds) || seconds < 0) return "0:00";
  const s = Math.floor(seconds % 60);
  const m = Math.floor((seconds / 60) % 60);
  const h = Math.floor(seconds / 3600);
  const mm = h ? String(m).padStart(2, "0") : String(m);
  return `${h ? h + ":" : ""}${mm}:${String(s).padStart(2, "0")}`;
}

export function cx(...parts: Array<string | false | null | undefined>): string {
  return parts.filter(Boolean).join(" ");
}

export function sourceLabel(source: string): string {
  if (source === "vk") return "VK";
  if (source === "soundcloud") return "SC";
  if (source === "own") return "WONDER";
  if (source === "youtube") return "YT";
  return source.toUpperCase();
}

export function badgeClass(source: string): string {
  if (source === "soundcloud") return "badge sc";
  if (source === "own") return "badge own";
  return "badge";
}

export function plural(n: number, one: string, few: string, many: string): string {
  const mod10 = n % 10;
  const mod100 = n % 100;
  if (mod10 === 1 && mod100 !== 11) return one;
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return few;
  return many;
}

/** «5 минут назад» для комментариев. */
export function relTime(ts: number): string {
  const diff = Math.max(0, Math.floor(Date.now() / 1000 - ts));
  if (diff < 60) return "только что";
  const m = Math.floor(diff / 60);
  if (m < 60) return `${m} ${plural(m, "минуту", "минуты", "минут")} назад`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h} ${plural(h, "час", "часа", "часов")} назад`;
  const d = Math.floor(h / 24);
  if (d < 30) return `${d} ${plural(d, "день", "дня", "дней")} назад`;
  const mo = Math.floor(d / 30);
  return `${mo} ${plural(mo, "месяц", "месяца", "месяцев")} назад`;
}

/** Скачать трек через прокси (same-origin, поэтому download работает). */
export function downloadTrack(url: string, artist: string, title: string) {
  const name = `${artist} - ${title}`.replace(/[\\/:*?"<>|]+/g, "_").slice(0, 120);
  const a = document.createElement("a");
  a.href = url;
  a.download = `${name}.mp3`;
  document.body.appendChild(a);
  a.click();
  a.remove();
}
