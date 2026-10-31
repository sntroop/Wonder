import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { usePlayer, useAuth } from "./store";
import { applySkin, loadSkin } from "./skin";
import { useBg } from "./bg";
import "./styles.css";
import "./styles/playlists.css";
import "./styles/menu.css";
import "./styles/settings.css";
import "./styles/fonts.css";

// оформление из localStorage ставим до первого кадра, чтобы не мигало
applySkin(loadSkin());
// своя картинка фона живёт в IndexedDB - подтягиваем при старте
void useBg.getState().load();

// отладочный доступ из автотестов (скриншоты, проверка воспроизведения)
(window as unknown as Record<string, unknown>).__wonder = { player: usePlayer, auth: useAuth };

// перед закрытием окна досылаем событие вкуса за текущий трек
window.addEventListener("pagehide", () => usePlayer.getState().flushFeedback());

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter basename="/wonder">
      <App />
    </BrowserRouter>
  </StrictMode>,
);
