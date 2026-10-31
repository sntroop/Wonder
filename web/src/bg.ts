import { create } from "zustand";

/**
 * Своя картинка/GIF/видео на фоне. Файл лежит в IndexedDB (localStorage
 * не тянет мегабайты), в состоянии - только object URL.
 */

const DB_NAME = "wonder-skin";
const STORE = "bg";
const KEY = "current";

export type BgKind = "image" | "video";

type BgState = {
  url: string | null;
  kind: BgKind | null;
  ready: boolean;
  name: string;
  load: () => Promise<void>;
  setFile: (file: File) => Promise<void>;
  clear: () => Promise<void>;
};

function openDb(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, 1);
    req.onupgradeneeded = () => {
      const db = req.result;
      if (!db.objectStoreNames.contains(STORE)) db.createObjectStore(STORE);
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

async function putRecord(blob: Blob, kind: BgKind, name: string): Promise<void> {
  const db = await openDb();
  await new Promise<void>((resolve, reject) => {
    const tx = db.transaction(STORE, "readwrite");
    tx.objectStore(STORE).put({ blob, kind, name }, KEY);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
  db.close();
}

async function getRecord(): Promise<{ blob: Blob; kind: BgKind; name: string } | null> {
  const db = await openDb();
  const rec = await new Promise<{ blob: Blob; kind: BgKind; name: string } | null>((resolve, reject) => {
    const tx = db.transaction(STORE, "readonly");
    const req = tx.objectStore(STORE).get(KEY);
    req.onsuccess = () => resolve(req.result || null);
    req.onerror = () => reject(req.error);
  });
  db.close();
  return rec;
}

async function dropRecord(): Promise<void> {
  const db = await openDb();
  await new Promise<void>((resolve, reject) => {
    const tx = db.transaction(STORE, "readwrite");
    tx.objectStore(STORE).delete(KEY);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
  db.close();
}

let liveUrl: string | null = null;

export const useBg = create<BgState>((set) => ({
  url: null,
  kind: null,
  ready: false,
  name: "",

  async load() {
    try {
      const rec = await getRecord();
      if (liveUrl) URL.revokeObjectURL(liveUrl);
      if (!rec) {
        liveUrl = null;
        set({ url: null, kind: null, name: "", ready: true });
        return;
      }
      liveUrl = URL.createObjectURL(rec.blob);
      set({ url: liveUrl, kind: rec.kind, name: rec.name || "", ready: true });
    } catch {
      set({ ready: true });
    }
  },

  async setFile(file) {
    const kind: BgKind = file.type.startsWith("video/") ? "video" : "image";
    await putRecord(file, kind, file.name);
    if (liveUrl) URL.revokeObjectURL(liveUrl);
    liveUrl = URL.createObjectURL(file);
    set({ url: liveUrl, kind, name: file.name, ready: true });
  },

  async clear() {
    await dropRecord();
    if (liveUrl) URL.revokeObjectURL(liveUrl);
    liveUrl = null;
    set({ url: null, kind: null, name: "", ready: true });
  },
}));
