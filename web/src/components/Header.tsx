import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { imgUrl } from "../api";
import { useAuth } from "../store";
import { ArtistIcon, SettingsIcon } from "./icons";

export function Header() {
  const navigate = useNavigate();
  const user = useAuth((s) => s.user);
  const src = imgUrl(user?.photo, 120);
  const [broken, setBroken] = useState(false);

  useEffect(() => {
    setBroken(false);
  }, [src]);

  return (
    <header className="header">
      <button className="header-logo" onClick={() => navigate("/profile")} title="Профиль">
        {src && !broken ? (
          <img src={src} alt="" onError={() => setBroken(true)} />
        ) : (
          <div className="avatar header-avatar-icon">
            <ArtistIcon />
          </div>
        )}
      </button>

      <button className="wordmark" onClick={() => navigate("/")} title="На главную">
        <span className="w">W</span> Wonder
      </button>

      <button className="icon-btn" onClick={() => navigate("/settings")} title="Настройки">
        <SettingsIcon />
      </button>
    </header>
  );
}
