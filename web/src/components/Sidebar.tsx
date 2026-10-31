import { useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { imgUrl } from "../api";
import { useAuth, usePlayer } from "../store";
import {
  HomeIcon,
  LibraryIcon,
  PlaylistIcon,
  SearchIcon,
  SettingsIcon,
  ShortsIcon,
  WaveIcon,
} from "./icons";

const items = [
  { to: "/", label: "Главная", icon: HomeIcon, end: true },
  { to: "/search", label: "Поиск", icon: SearchIcon },
  { to: "/wave", label: "Моя волна", icon: WaveIcon },
  { to: "/shorts", label: "Шортсы", icon: ShortsIcon },
  { to: "/library", label: "Библиотека", icon: LibraryIcon },
  { to: "/playlists", label: "Плейлисты", icon: PlaylistIcon },
];

export function Sidebar() {
  const user = useAuth((s) => s.user);
  const me = useAuth((s) => s.me);
  const queue = usePlayer((s) => s.queue);
  const navigate = useNavigate();
  const src = imgUrl(user?.photo, 120);
  const [broken, setBroken] = useState(false);

  useEffect(() => {
    setBroken(false);
  }, [src]);

  const vkReady = me?.vk?.ready;
  const vkRelay = me?.vk?.relay;

  return (
    <aside className="sidebar">
      <div className="brand">
        <img src="/wonder/logo.png" alt="" />
        <span>Wonder</span>
      </div>

      <nav className="nav">
        {items.map(({ to, label, icon: Icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) => `nav-item${isActive ? " active" : ""}`}
          >
            <Icon />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="nav-title">Аккаунт</div>
      <nav className="nav">
        <NavLink
          to="/profile"
          className={({ isActive }) => `nav-item${isActive ? " active" : ""}`}
        >
          <SettingsIcon />
          <span>Профиль и настройки</span>
        </NavLink>
      </nav>

      <div className="sidebar-foot">
        <div className="user-chip" onClick={() => navigate("/profile")}>
          <div className="avatar">
            {src && !broken ? (
              <img src={src} alt="" onError={() => setBroken(true)} />
            ) : (
              (user?.name || "W").replace("@", "").slice(0, 1).toUpperCase()
            )}
          </div>
          <div className="user-meta">
            <b>{user?.name || "Гость"}</b>
            <span>
              {vkReady ? "VK подключён" : vkRelay ? "VK через телефон" : "VK не подключён"}
            </span>
          </div>
        </div>

        <div style={{ display: "flex", justifyContent: "space-between" }}>
          <span>{queue.length ? `Очередь: ${queue.length}` : "Очередь пуста"}</span>
          <span title={__BUILD__}>v0.1</span>
        </div>
      </div>
    </aside>
  );
}
