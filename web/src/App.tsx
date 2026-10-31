import { useEffect } from "react";
import { Route, Routes, useLocation } from "react-router-dom";
import { useAuth, usePlayer } from "./store";
import { useToast } from "./toast";
import { useArtworkTint } from "./tintHook";
import { AuthScreen } from "./components/AuthScreen";
import { Header } from "./components/Header";
import { Dock } from "./components/Dock";
import { PlayerSheet } from "./components/PlayerSheet";
import { AddToPlaylistDialog } from "./components/AddToPlaylist";
import { UpdateBanner } from "./components/UpdateBanner";
import { QueuePanel } from "./components/QueuePanel";
import { Background } from "./components/Background";
import { Home } from "./pages/Home";
import { Search } from "./pages/Search";
import { Wave } from "./pages/Wave";
import { Library } from "./pages/Library";
import { LibraryMusic } from "./pages/LibraryMusic";
import { LibraryArtists } from "./pages/LibraryArtists";
import { Playlists } from "./pages/Playlists";
import { PlaylistPage } from "./pages/PlaylistPage";
import { ArtistPage } from "./pages/ArtistPage";
import { Profile } from "./pages/Profile";
import { AuthorPage } from "./pages/AuthorPage";
import { Settings } from "./pages/Settings";
import { DesktopApp } from "./pages/DesktopApp";
import { Shorts } from "./pages/Shorts";

function Splash() {
  return (
    <div className="auth">
      <div style={{ display: "grid", placeItems: "center", gap: 18 }}>
        <img src="/wonder/logo.png" alt="" style={{ width: 54, height: 54, borderRadius: 16 }} />
        <div className="spinner" />
      </div>
    </div>
  );
}

function ToastHost() {
  const message = useToast((s) => s.message);
  return message ? <div className="toast">{message}</div> : null;
}

export default function App() {
  const { user, ready, refresh } = useAuth();
  const toggle = usePlayer((s) => s.toggle);
  const next = usePlayer((s) => s.next);
  const prev = usePlayer((s) => s.prev);
  const current = usePlayer((s) => s.current());
  const location = useLocation();
  const isShorts = location.pathname.startsWith("/shorts");
  const tint = useArtworkTint(current?.artwork);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  useEffect(() => {
    // десктопные горячие клавиши: пробел, стрелки, M/N/P/Q
    const onKey = (e: KeyboardEvent) => {
      const el = e.target as HTMLElement | null;
      if (
        el &&
        (el.tagName === "INPUT" ||
          el.tagName === "TEXTAREA" ||
          el.isContentEditable)
      ) {
        return;
      }
      const p = usePlayer.getState();
      if (e.altKey && e.code === "ArrowRight") {
        e.preventDefault();
        p.next();
        return;
      }
      if (e.altKey && e.code === "ArrowLeft") {
        e.preventDefault();
        p.prev();
        return;
      }
      if (e.metaKey || e.ctrlKey || e.altKey) return;
      if (e.code === "Space") {
        e.preventDefault();
        p.toggle();
      } else if (e.code === "ArrowRight" && p.current()) {
        e.preventDefault();
        p.seek(Math.min(p.duration || p.current()?.duration || 0, p.time + 5));
      } else if (e.code === "ArrowLeft" && p.current()) {
        e.preventDefault();
        p.seek(Math.max(0, p.time - 5));
      } else if (e.code === "KeyM") {
        p.toggleMute();
      } else if (e.code === "KeyN") {
        p.next();
      } else if (e.code === "KeyP") {
        p.prev();
      } else if (e.code === "KeyQ") {
        p.setQueueOpen(!p.queueOpen);
      } else if (e.code === "Escape") {
        p.setSheetOpen(false);
        p.setQueueOpen(false);
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);

  useEffect(() => {
    // на мобильном браузере 100vh скачет из-за адресной строки
    const vh = window.innerHeight * 0.01;
    document.documentElement.style.setProperty("--vh", `${vh}px`);
  }, []);

  if (!ready) return <Splash />;
  if (!user) {
    return (
      <>
        <Background art={current?.artwork} color={tint} />
        <AuthScreen />
        <ToastHost />
      </>
    );
  }

  return (
    <div className={`app${isShorts ? " app-shorts" : ""}`}>
      <Background art={current?.artwork} color={tint} />
      <Header />
      <UpdateBanner />
      <div className="content">
        <Routes key={location.pathname}>
          <Route path="/" element={<Home />} />
          <Route path="/search" element={<Search />} />
          <Route path="/wave" element={<Wave />} />
          <Route path="/shorts" element={<Shorts />} />
          <Route path="/library" element={<Library />} />
          <Route path="/library/music" element={<LibraryMusic />} />
          <Route path="/library/artists" element={<LibraryArtists />} />
          <Route path="/playlists" element={<Playlists />} />
          <Route path="/playlist/:owner/:id" element={<PlaylistPage />} />
          <Route path="/artist/:source/:id" element={<ArtistPage />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/user/:id" element={<AuthorPage />} />
          <Route path="/settings" element={<Settings />} />
          <Route path="/desktop" element={<DesktopApp />} />
          <Route path="*" element={<Home />} />
        </Routes>
      </div>
      {isShorts ? null : <Dock />}
      <PlayerSheet />
      <ToastHost />
      <QueuePanel />
      <AddToPlaylistDialog />
    </div>
  );
}
