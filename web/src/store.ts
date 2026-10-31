import { create } from "zustand";
import {
  auth as authApi,
  feedback,
  getStoredUser,
  getToken,
  imgUrl,
  setStoredUser,
  setToken,
  type Me,
  type Track,
  type User,
} from "./api";
import { applyFx, attachFx, fadeTo, getFx, resumeFx, subscribeFx } from "./audioFx";

/* ── аккаунт ──────────────────────────────────────────────────────────── */

type AuthState = {
  user: User | null;
  token: string;
  me: Me | null;
  ready: boolean;
  busy: boolean;
  error: string;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string, name: string) => Promise<void>;
  guest: () => Promise<void>;
  refresh: () => Promise<void>;
  logout: () => void;
  clearError: () => void;
};

export const useAuth = create<AuthState>((set, get) => ({
  user: getStoredUser(),
  token: getToken(),
  me: null,
  ready: false,
  busy: false,
  error: "",

  async login(username, password) {
    set({ busy: true, error: "" });
    try {
      const res = await authApi.login(username.trim().replace(/^@/, ""), password);
      setToken(res.token);
      setStoredUser(res.user);
      set({ token: res.token, user: res.user, busy: false });
      await get().refresh();
    } catch (e) {
      set({ busy: false, error: e instanceof Error ? e.message : "Ошибка входа" });
      throw e;
    }
  },

  async register(username, password, name) {
    set({ busy: true, error: "" });
    try {
      const res = await authApi.register(
        username.trim().replace(/^@/, ""),
        password,
        name,
      );
      setToken(res.token);
      setStoredUser(res.user);
      set({ token: res.token, user: res.user, busy: false });
      await get().refresh();
    } catch (e) {
      set({ busy: false, error: e instanceof Error ? e.message : "Ошибка регистрации" });
      throw e;
    }
  },

  async guest() {
    set({ busy: true, error: "" });
    try {
      const res = await authApi.guest();
      setToken(res.token);
      setStoredUser(res.user);
      set({ token: res.token, user: res.user, busy: false });
      await get().refresh();
    } catch (e) {
      set({ busy: false, error: e instanceof Error ? e.message : "Ошибка входа" });
      throw e;
    }
  },

  async refresh() {
    if (!getToken()) {
      set({ ready: true, user: null });
      return;
    }
    try {
      const me = await authApi.me();
      if (!me.user) {
        setToken("");
        setStoredUser(null);
        set({ user: null, me: null, ready: true });
        return;
      }
      setStoredUser(me.user);
      set({ user: me.user, me, ready: true });
    } catch {
      // сервер недоступен — не выкидываем из аккаунта
      set({ ready: true });
    }
  },

  logout() {
    setToken("");
    setStoredUser(null);
    set({ user: null, me: null, token: "", error: "" });
    usePlayer.getState().stop();
  },

  clearError: () => set({ error: "" }),
}));

/* ── плеер ────────────────────────────────────────────────────────────── */

type PlayerState = {
  queue: Track[];
  index: number;
  playing: boolean;
  time: number;
  duration: number;
  buffered: number;
  volume: number;
  muted: boolean;
  loading: boolean;
  error: string;
  /** открыть/закрыть правую панель очереди */
  queueOpen: boolean;
  /** открыт ли полноэкранный плеер */
  sheetOpen: boolean;

  play: (list: Track[], start?: number, src?: string) => void;
  /** дослать событие вкуса за текущий трек (перед закрытием окна) */
  flushFeedback: () => void;
  /** добавить трек в конец очереди; на пустой очереди — сразу играть */
  enqueue: (track: Track) => void;
  /** вставить трек сразу после текущего */
  playNext: (track: Track) => void;
  /** убрать трек по индексу, не сбивая текущее воспроизведение */
  removeAt: (i: number) => void;
  /** остановить и очистить очередь */
  clearQueue: () => void;
  toggle: () => void;
  pause: () => void;
  next: () => void;
  prev: () => void;
  seek: (t: number) => void;
  setVolume: (v: number) => void;
  toggleMute: () => void;
  stop: () => void;
  setQueueOpen: (v: boolean) => void;
  setSheetOpen: (v: boolean) => void;
  current: () => Track | null;
};

let audio: HTMLAudioElement | null = null;
let currentUrl = "";

/* ── вкус: события прослушивания (как в Android v9.x) ─────────────────── */

const DEV_KEY = "wonder.dev";

function devId(): string {
  try {
    let v = localStorage.getItem(DEV_KEY);
    if (!v) {
      v = Math.random().toString(36).slice(2) + Date.now().toString(36);
      localStorage.setItem(DEV_KEY, v);
    }
    return v;
  } catch {
    return "";
  }
}

type ListenSession = {
  track: Track;
  src: string;
  startPos: number;
  maxPos: number;
};

let session: ListenSession | null = null;

function flushSession(): void {
  const s = session;
  session = null;
  // шортсы играют вырезку из середины трека - для вкуса это мусор
  if (!s || s.src === "shorts") return;
  const played = Math.max(0, s.maxPos - s.startPos);
  if (played < 1) return;
  const dur = s.track.duration || 0;
  const cov = dur > 0 ? Math.min(1, played / dur) : 0;
  let ev = "skip_late";
  if (played < 12 || (dur > 0 && cov < 0.2)) ev = "skip";
  else if (dur > 0 && cov >= 0.85) ev = "complete";
  else if (dur > 0 && cov >= 0.5) ev = "listen";
  void feedback(s.track, ev, Math.round(s.maxPos), {
    played: Math.round(played),
    cov,
    src: s.src,
    hour: new Date().getHours(),
    dev: devId(),
  }).catch(() => {});
}

function ensureAudio(): HTMLAudioElement {
  if (!audio) {
    audio = new Audio();
    audio.preload = "auto";
    audio.volume = usePlayer.getState().volume;
    audio.addEventListener("timeupdate", () => {
      const now = audio!.currentTime;
      if (session) session.maxPos = Math.max(session.maxPos, now);
      usePlayer.setState({ time: now });
    });
    audio.addEventListener("durationchange", () => {
      if (Number.isFinite(audio!.duration)) {
        usePlayer.setState({ duration: audio!.duration });
      }
    });
    audio.addEventListener("progress", () => {
      const b = audio!.buffered;
      if (b.length) usePlayer.setState({ buffered: b.end(b.length - 1) });
    });
    audio.addEventListener("waiting", () => usePlayer.setState({ loading: true }));
    audio.addEventListener("playing", () =>
      usePlayer.setState({ playing: true, loading: false, error: "" }),
    );
    audio.addEventListener("pause", () => usePlayer.setState({ playing: false }));
    audio.addEventListener("ended", () => usePlayer.getState().next());
    audio.addEventListener("error", () =>
      usePlayer.setState({
        loading: false,
        playing: false,
        error: "Не удалось воспроизвести трек",
      }),
    );
    // эквалайзер: подключаем элемент к цепочке фильтров один раз
    attachFx(audio);
    applyFx();
  }
  return audio;
}

function bindMediaSession(track: Track | null, playing: boolean) {
  if (!("mediaSession" in navigator)) return;
  if (track) {
    const art = imgUrl(track.artwork, 600);
    navigator.mediaSession.metadata = new MediaMetadata({
      title: track.title,
      artist: track.artist,
      album: "Wonder",
      artwork: art ? [{ src: art }] : [],
    });
  }
  navigator.mediaSession.playbackState = playing ? "playing" : "paused";
}

// темп применяем к живому элементу сразу при изменении настроек
subscribeFx((s) => {
  if (!audio) return;
  audio.playbackRate = s.enabled ? s.tempo : 1;
  audio.preservesPitch = true;
});

export const usePlayer = create<PlayerState>((set, get) => ({
  queue: [],
  index: -1,
  playing: false,
  time: 0,
  duration: 0,
  buffered: 0,
  volume: Number(localStorage.getItem("wonder.volume") || 0.9),
  muted: false,
  loading: false,
  error: "",
  queueOpen: false,
  sheetOpen: false,

  play(list, start = 0, src = "app") {
    if (!list.length) return;
    const idx = Math.max(0, Math.min(start, list.length - 1));
    const track = list[idx];
    const a = ensureAudio();
    const url = `/wonder/api/stream/${track.source}/${track.id}`;
    // закрываем прошлую сессию (skip/listen/complete) и открываем новую
    flushSession();
    session = { track, src, startPos: 0, maxPos: 0 };
    set({ queue: list, index: idx, time: 0, duration: track.duration || 0, error: "" });
    const fx = getFx();
    const cross = fx.enabled ? fx.crossfade : 0;
    const applyState = () => {
      // темп из эквалайзера; preservesPitch держит высоту тона
      a.playbackRate = fx.enabled ? fx.tempo : 1;
      a.preservesPitch = true;
    };
    const startUrl = () => {
      a.src = url;
      a.load();
      applyState();
      resumeFx();
      void a.play().catch(() => set({ playing: false, loading: false }));
      if (cross > 0) void fadeTo(1, Math.min(cross, 3) * 700);
    };
    if (url !== currentUrl) {
      currentUrl = url;
      set({ buffered: 0, loading: true });
      // плавный переход: гасим старый трек, запускаем новый, поднимаем громкость
      if (cross > 0 && !a.paused && a.readyState > 0) {
        void fadeTo(0, Math.min(cross, 3) * 700).then(startUrl);
      } else {
        startUrl();
      }
    } else {
      resumeFx();
      applyState();
      void a.play().catch(() => set({ playing: false, loading: false }));
    }
    bindMediaSession(track, true);

    if ("mediaSession" in navigator) {
      navigator.mediaSession.setActionHandler("play", () => get().toggle());
      navigator.mediaSession.setActionHandler("pause", () => get().toggle());
      navigator.mediaSession.setActionHandler("nexttrack", () => get().next());
      navigator.mediaSession.setActionHandler("previoustrack", () => get().prev());
      navigator.mediaSession.setActionHandler("seekto", (d) => {
        if (typeof d.seekTime === "number") get().seek(d.seekTime);
      });
    }
  },

  enqueue(track) {
    const { queue, index } = get();
    if (!queue.length || index < 0) {
      get().play([track], 0);
      return;
    }
    set({ queue: [...queue, track] });
  },

  playNext(track) {
    const { queue, index } = get();
    if (!queue.length || index < 0) {
      get().play([track], 0);
      return;
    }
    const next = queue.slice();
    next.splice(index + 1, 0, track);
    set({ queue: next });
  },

  removeAt(i) {
    const { queue, index } = get();
    if (i < 0 || i >= queue.length) return;
    const next = queue.slice();
    next.splice(i, 1);
    if (i === index) {
      if (!next.length) {
        get().stop();
        return;
      }
      get().play(next, Math.min(i, next.length - 1));
      return;
    }
    set({ queue: next, index: i < index ? index - 1 : index });
  },

  clearQueue() {
    get().stop();
  },

  toggle() {
    const a = ensureAudio();
    if (!get().queue.length) return;
    if (a.paused) {
      void a.play();
    } else {
      a.pause();
    }
    bindMediaSession(get().current(), !a.paused);
  },

  pause() {
    ensureAudio().pause();
  },

  next() {
    const { queue, index } = get();
    if (!queue.length) return;
    const next = index + 1 < queue.length ? index + 1 : 0;
    get().play(queue, next);
  },

  prev() {
    const { queue, index, time } = get();
    if (!queue.length) return;
    if (time > 4) {
      get().seek(0);
      return;
    }
    const prev = index - 1 >= 0 ? index - 1 : queue.length - 1;
    get().play(queue, prev);
  },

  seek(t) {
    const a = ensureAudio();
    if (Number.isFinite(t)) {
      a.currentTime = Math.max(0, t);
      set({ time: a.currentTime });
    }
  },

  setVolume(v) {
    const a = ensureAudio();
    const vol = Math.max(0, Math.min(1, v));
    a.volume = vol;
    a.muted = vol === 0;
    localStorage.setItem("wonder.volume", String(vol));
    set({ volume: vol, muted: vol === 0 });
  },

  toggleMute() {
    const a = ensureAudio();
    a.muted = !a.muted;
    set({ muted: a.muted });
  },

  stop() {
    flushSession();
    if (audio) {
      audio.pause();
      audio.src = "";
    }
    currentUrl = "";
    set({ queue: [], index: -1, playing: false, time: 0, duration: 0, buffered: 0 });
  },

  flushFeedback() {
    if (audio && session) session.maxPos = Math.max(session.maxPos, audio.currentTime);
    flushSession();
  },

  setQueueOpen(v) {
    set({ queueOpen: v });
  },

  setSheetOpen(v) {
    set({ sheetOpen: v });
  },

  current() {
    const { queue, index } = get();
    return index >= 0 && index < queue.length ? queue[index] : null;
  },
}));
