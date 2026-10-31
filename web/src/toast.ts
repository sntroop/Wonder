import { create } from "zustand";

type ToastState = {
  message: string;
  show: (message: string) => void;
  hide: () => void;
};

let timer: number | undefined;

export const useToast = create<ToastState>((set) => ({
  message: "",
  show(message: string) {
    window.clearTimeout(timer);
    set({ message });
    timer = window.setTimeout(() => set({ message: "" }), 2600);
  },
  hide() {
    window.clearTimeout(timer);
    set({ message: "" });
  },
}));
