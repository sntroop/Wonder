/**
 * Тонкий клиент бэкенда Wonder.
 *
 * Всё ходит через /wonder/api (nginx проксирует на 127.0.0.1:8400/api),
 * поэтому в браузере нет CORS и mixed content, а картинки/аудио остаются
 * same-origin и проходят CSP лендинга.
 */

export const API = "/wonder/api";

export type Track = {
  id: string;
  source: string; // vk | soundcloud | own | youtube
  title: string;
  artist: string;
  duration?: number;
  artwork?: string | null;
  permalink?: string;
  playable?: boolean | null;
  streamable?: boolean;
  mine?: boolean;
  match?: number;
  author?: string;
  date?: number;
  owner?: number;
  speed?: number;
  art?: boolean;
  substituted?: boolean;
  vk_id?: string;
  genre?: string;
  tags?: string;
  plays?: number;
  played?: number;
};

export type Playlist = {
  id: string;
  owner_id?: string;
  title: string;
  count?: number;
  cover?: string;
  access_key?: string;
  author?: string;
  match?: number;
  mine?: boolean;
  art?: boolean;
  source?: string;
  description?: string;
};

export type HomeBlock = {
  key: string;
  title: string;
  kind: "tracks" | "playlists" | string;
  tracks?: Track[];
  playlists?: Playlist[];
};

export type User = {
  id: number;
  name: string;
  photo?: string;
  username?: string;
  owner?: boolean;
  vk_uid?: number | null;
};

export type Me = {
  user: User | null;
  owner: boolean;
  vk: {
    has_token: boolean;
    has_cookie: boolean;
    expires: number;
    ready: boolean;
    relay: boolean;
    problem: string;
  };
  sc: { connected: boolean };
  taste: { features: number; events: number; evidence: number };
};

export type Artist = {
  source: string;
  id: string;
  name: string;
  photo?: string;
  tracks?: number;
  mine?: boolean;
};

export type SearchAll = {
  tracks?: Track[];
  artists?: Artist[];
  albums?: Track[];
  playlists?: Playlist[];
  [key: string]: unknown;
};

export type Short = {
  track: Track;
  start: number;
  end: number;
  score: number;
  kind: string;
};

export type LibraryStats = {
  tracks?: number;
  artists?: number;
  playlists?: number;
  likes?: number;
  [key: string]: unknown;
};

const TOKEN_KEY = "wonder.token";
const USER_KEY = "wonder.user";

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || "";
}

export function setToken(token: string) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

export function getStoredUser(): User | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  } catch {
    return null;
  }
}

export function setStoredUser(user: User | null) {
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
  else localStorage.removeItem(USER_KEY);
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  const token = getToken();
  if (token) headers.set("X-MP-Token", token);
  if (init?.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  let res: Response;
  const ctrl = new AbortController();
  const timer = window.setTimeout(() => ctrl.abort(), 35000);
  try {
    res = await fetch(API + path, { ...init, headers, signal: ctrl.signal });
  } catch {
    throw new ApiError(0, "Нет связи с сервером");
  } finally {
    window.clearTimeout(timer);
  }
  if (!res.ok) {
    let detail = "";
    try {
      const body = await res.json();
      detail = typeof body?.detail === "string" ? body.detail : "";
    } catch {
      /* тело не JSON */
    }
    throw new ApiError(res.status, detail || `Сервер ответил ${res.status}`);
  }
  return (await res.json()) as T;
}

function post<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, { method: "POST", body: JSON.stringify(body ?? {}) });
}

/* ── auth ─────────────────────────────────────────────────────────────── */

export type AuthResponse = { status: string; token: string; user: User };

export const auth = {
  login: (username: string, password: string) =>
    post<AuthResponse>("/auth/login", { username, password }),
  register: (username: string, password: string, name: string) =>
    post<AuthResponse>("/auth/register", { username, password, name }),
  guest: () => post<AuthResponse>("/auth/guest"),
  me: () => request<Me>("/auth/me"),
  logout: () => post<{ ok: boolean }>("/auth/logout"),
  usernameFree: (u: string) =>
    request<{ username: string; free: boolean }>(
      `/auth/username?u=${encodeURIComponent(u)}`,
    ),
};

/* ── музыка ───────────────────────────────────────────────────────────── */

export type FeedbackExtra = {
  /** сколько реально прослушано, сек */
  played?: number;
  /** покрытие трека 0..1 */
  cov?: number;
  /** откуда запущен: wave/home/search/my/playlist/shorts/artist */
  src?: string;
  /** локальный час */
  hour?: number;
  /** анонимный id устройства */
  dev?: string;
};

export function feedback(
  t: Track,
  ev: string,
  pos = 0,
  extra: FeedbackExtra = {},
): Promise<{ ok: boolean }> {
  return post<{ ok: boolean }>("/feedback", {
    ev,
    id: String(t.id),
    source: t.source,
    artist: t.artist,
    dur: t.duration || 0,
    pos,
    ...extra,
  });
}

export const music = {
  home: (refresh = false) =>
    request<{ blocks: HomeBlock[] }>(`/vk/home${refresh ? "?refresh=1" : ""}`),
  search: (q: string, source = "soundcloud", limit = 40) =>
    request<{ source: string; items: Track[] }>(
      `/search?q=${encodeURIComponent(q)}&source=${source}&limit=${limit}`,
    ),
  searchAll: (q: string, limit = 20) =>
    request<SearchAll>(`/search/all?q=${encodeURIComponent(q)}&limit=${limit}`),
  wave: (limit = 40, refresh = false) =>
    request<{ items: Track[] }>(
      `/vk/wave?limit=${limit}${refresh ? "&refresh=1" : ""}`,
    ),
  myAudio: (count = 500) =>
    request<{ items: Track[] }>(`/vk/my/audio?count=${count}`),
  myPlaylists: (count = 50) =>
    request<{ items: Playlist[] }>(`/vk/myplaylists?count=${count}`),
  playlist: (ownerId: string, id: string, accessKey = "", count = 300) =>
    request<{ items: Track[] }>(
      `/vk/playlist/${ownerId}/${id}/tracks?access_key=${encodeURIComponent(
        accessKey,
      )}&count=${count}`,
    ),
  recent: (limit = 30) =>
    request<{ items: Track[] }>(`/recent?limit=${limit}`),
  artists: () => request<{ items: Artist[] }>("/artists"),
  artist: (source: string, id: string, name: string, limit = 100) =>
    request<{ tracks?: Track[]; items?: Track[]; artist?: Artist; bio?: string }>(
      `/artist?source=${encodeURIComponent(source)}&id=${encodeURIComponent(
        id,
      )}&name=${encodeURIComponent(name)}&limit=${limit}`,
    ),
  libraryStats: () => request<LibraryStats>("/library/stats"),
  artistAdd: (a: { source: string; id: string; name: string; cover?: string }) =>
    post<{ ok: boolean }>("/artists/add", {
      source: a.source,
      id: a.id,
      name: a.name,
      cover: a.cover || "",
    }),
  shorts: (limit = 12) =>
    request<{ items: Short[] }>(`/shorts?limit=${limit}`),
  like: (t: Track) =>
    post<{ liked: boolean; likes?: number }>("/track/like", {
      source: t.source,
      id: t.id,
      artist: t.artist,
      title: t.title,
      artwork: t.artwork || "",
    }),
  add: (t: Track) =>
    post<{ ok: boolean }>("/library/add", { track: t }).then((r) => {
      // «добавил в библиотеку» - сильный сигнал вкуса (как в Android, вес 1.6)
      void feedback(t, "add", 0).catch(() => {});
      return r;
    }),
  remove: (t: Track) =>
    post<{ ok: boolean }>("/library/remove", { source: t.source, id: t.id }),
};

/* ── урлы ─────────────────────────────────────────────────────────────── */

export function imgUrl(url?: string | null, size = 400): string | undefined {
  if (!url) return undefined;
  if (url.startsWith(API)) return url;
  if (url.startsWith("http")) {
    return `${API}/img?u=${encodeURIComponent(url)}&s=${size}`;
  }
  if (url.startsWith("/")) return API + url;
  return url;
}

export function streamUrl(t: Track): string {
  return `${API}/stream/${t.source}/${t.id}`;
}

/* ── тексты песен ─────────────────────────────────────────────────────── */

export type LyricsLine = { t: number; text: string };
export type LyricsData = {
  found: boolean;
  synced: boolean;
  lines: LyricsLine[];
  plain: string;
  instrumental?: boolean;
};

export function lyrics(t: Track): Promise<LyricsData> {
  const q = new URLSearchParams({
    artist: t.artist || "",
    title: t.title || "",
    duration: String(t.duration || 0),
    source: t.source || "",
    id: String(t.id || ""),
  });
  return request<LyricsData>(`/lyrics?${q.toString()}`);
}

/* ── ремиксы ──────────────────────────────────────────────────────────── */

export function remixes(t: Track, limit = 60): Promise<{ items: Track[] }> {
  const q = new URLSearchParams({
    artist: t.artist || "",
    title: t.title || "",
    limit: String(limit),
  });
  return request<{ items: Track[] }>(`/remixes?${q.toString()}`);
}

/* ── соцсеть: лайки и комментарии ─────────────────────────────────────── */

export type CommentItem = {
  id: number;
  uid: number;
  name: string;
  pos: number;
  text: string;
  ts: number;
  mine: boolean;
};

export type TrackSocial = {
  likes: number;
  liked: boolean;
  comments: CommentItem[];
};

export const social = {
  get: (t: Track) =>
    request<TrackSocial>(`/track/${t.source}/${t.id}/social`),
  comment: (t: Track, text: string, pos = 0) =>
    post<CommentItem>(`/track/${t.source}/${t.id}/comment`, { text, pos }),
  removeComment: (cid: number) =>
    request<{ ok: boolean }>(`/comment/${cid}`, { method: "DELETE" }),
};

/* ── плейлисты приложения ─────────────────────────────────────────────── */

export type AppPlaylist = Playlist & {
  pid?: number;
  is_mine?: boolean;
  public?: number;
  is_public?: boolean;
  tracks?: Track[];
};

export const playlists = {
  mine: () => request<{ items: AppPlaylist[] }>("/my/playlists"),
  get: (pid: number | string) =>
    request<AppPlaylist & { tracks: Track[] }>(`/playlists/${pid}`),
  create: (title: string, description = "", isPublic = 1) =>
    post<AppPlaylist>("/playlists/create", {
      title,
      description,
      public: isPublic,
    }),
  add: (pid: number | string, t: Track) =>
    post<{ ok: boolean }>(`/playlists/${pid}/add`, { track: t }),
  remove: (pid: number | string, t: Track) =>
    post<{ ok: boolean }>(`/playlists/${pid}/remove`, {
      source: t.source,
      id: t.id,
    }),
  update: (pid: number | string, title: string, description = "", isPublic = 1) =>
    post<{ ok: boolean }>(`/playlists/${pid}/update`, {
      title,
      description,
      public: isPublic,
    }),
  destroy: (pid: number | string) =>
    request<{ ok: boolean }>(`/playlists/${pid}`, { method: "DELETE" }),
};

/* ── импорт чужого плейлиста ──────────────────────────────────────────── */

export type ImportPreview = {
  source: string;
  title?: string;
  cover?: string;
  tracks: Track[];
  total: number;
};

export type ImportJobStart = {
  job: string;
  title?: string;
  cover?: string;
  source?: string;
  total: number;
};

export type ImportJob = {
  state: "running" | "done" | "error" | string;
  done: number;
  total: number;
  matched: number;
  title?: string;
  cover?: string;
  source?: string;
  error?: string;
  items: Track[];
};

export const importer = {
  preview: (url: string, limit = 100) =>
    request<ImportPreview>(
      `/import/preview?url=${encodeURIComponent(url)}&limit=${limit}`,
    ),
  start: (url: string, limit = 60) =>
    post<ImportJobStart>("/import/job", { url, limit }),
  status: (job: string) => request<ImportJob>(`/import/job/${job}`),
  /** скриншот плейлиста -> текст (Tesseract на сервере) */
  ocr: (file: File) => {
    const fd = new FormData();
    fd.append("file", file);
    return form<{ text: string }>("/import/ocr", fd);
  },
  /** список «Artist — Title» построчно (в т.ч. после OCR) */
  text: (text: string, title?: string) =>
    post<ImportJobStart>("/import/text", { text, title }),
  /** ссылка на TikTok: сервер распознаёт трек (Shazam) и кладёт в «Мою музыку» */
  tiktok: (url: string) => post<ImportJobStart>("/import/tiktok", { url }),
};

/* ── загрузка своего трека ────────────────────────────────────────────── */

export type UploadMeta = {
  title: string;
  genre?: string;
  duration?: number;
  dropStart?: number;
  dropLen?: number;
};

async function form<T>(path: string, data: FormData): Promise<T> {
  const headers = new Headers();
  const token = getToken();
  if (token) headers.set("X-MP-Token", token);
  let res: Response;
  try {
    res = await fetch(API + path, { method: "POST", body: data, headers });
  } catch {
    throw new ApiError(0, "Нет связи с сервером");
  }
  if (!res.ok) {
    let detail = "";
    try {
      const body = await res.json();
      detail = typeof body?.detail === "string" ? body.detail : "";
    } catch {
      /* не JSON */
    }
    throw new ApiError(res.status, detail || `Сервер ответил ${res.status}`);
  }
  return (await res.json()) as T;
}

export const upload = {
  track: (file: File, meta: UploadMeta, cover?: File | null) => {
    const fd = new FormData();
    fd.set("file", file);
    if (cover) fd.set("cover", cover);
    fd.set("title", meta.title);
    fd.set("genre", meta.genre || "");
    fd.set("duration", String(meta.duration || 0));
    fd.set("drop_start", String(meta.dropStart || 0));
    fd.set("drop_len", String(meta.dropLen || 15));
    return form<{ ok: boolean; track: Track; cooldown: number }>(
      "/own/upload",
      fd,
    );
  },
};

/* ── интересы (жанры + любимые артисты) ───────────────────────────────── */

export type InterestItem = { id: string; title: string; img: string };
export type InterestGroup = { key: string; title: string; items: InterestItem[] };

export const interests = {
  catalog: () =>
    request<{ groups: InterestGroup[]; total: number; max: number }>("/interests"),
  prefs: () => request<{ artists: string[]; genres: string[] }>("/guest/prefs"),
  save: (genres: string[], artists: string[]) =>
    post<{ ok: boolean; genres: string[]; artists: string[] }>("/guest/prefs", {
      genres,
      artists,
    }),
};

/* ── подписки на авторов ──────────────────────────────────────────────── */

export const author = {
  sub: (uid: number, on = true) =>
    post<{ subs: number; subscribed: boolean }>(`/author/${uid}/sub`, { on }),
};

/* ── профиль ──────────────────────────────────────────────────────────── */

export type Profile = {
  uid: number;
  name: string;
  bio: string;
  avatar: string;
  stats?: { tracks?: number; plays?: number; likes?: number; followers?: number; [k: string]: number | undefined };
  last_upload?: number;
  can_upload_in?: number;
  tracks?: Track[];
  self?: boolean;
  subscribed?: boolean;
};

export const profile = {
  get: (user = 0) =>
    request<Profile>(`/profile${user ? `?user=${user}` : ""}`),
  update: (name: string, bio: string) =>
    post<Profile>("/profile/update", { name, bio }),
  avatar: (file: File) => {
    const fd = new FormData();
    fd.set("file", file);
    return form<Profile>("/profile/avatar", fd);
  },
};

/* ── подключение VK ───────────────────────────────────────────────────── */

export type VkQr = {
  id: string;
  url: string;
  qr?: string;
  status?: string;
};

export const vkConnect = {
  start: () => post<VkQr>("/vk/qr/start"),
  check: (id: string) =>
    request<VkQr & { api?: boolean; api_error?: string; status: string }>(
      `/vk/qr/check?id=${encodeURIComponent(id)}`,
    ),
};
