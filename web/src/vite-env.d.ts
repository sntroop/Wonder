/// <reference types="vite/client" />

declare const __BUILD__: string;

type WonderUpdateStatus = {
  state: "checking" | "available" | "downloading" | "downloaded" | "latest" | "error";
  version?: string;
  percent?: number;
  message?: string;
};

type WonderDesktop = {
  version: string;
  platform: string;
  isElectron: boolean;
  minimize: () => Promise<void>;
  hide: () => Promise<void>;
  quit: () => Promise<void>;
  toggleAutostart: (value: boolean) => Promise<boolean>;
  getAutostart: () => Promise<boolean>;
  checkUpdates: () => Promise<{ ok: boolean }>;
  getUpdateStatus?: () => Promise<WonderUpdateStatus | null>;
  downloadUpdate?: () => Promise<{ ok: boolean }>;
  installUpdate?: () => Promise<{ ok: boolean }>;
  onUpdateStatus: (cb: (status: WonderUpdateStatus) => void) => () => void;
};

interface Window {
  wonderDesktop?: WonderDesktop;
}
