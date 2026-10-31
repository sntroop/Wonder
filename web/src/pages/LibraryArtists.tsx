import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { music, type Artist } from "../api";
import { ArtistRow } from "./Library";
import { ChevronLeft } from "../components/icons";

export function LibraryArtists() {
  const [artists, setArtists] = useState<Artist[] | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    let alive = true;
    music
      .artists()
      .then((r) => alive && setArtists(r.items || []))
      .catch(() => alive && setArtists([]));
    return () => {
      alive = false;
    };
  }, []);

  return (
    <div className="page page-enter">
      <div style={{ display: "flex", alignItems: "center", gap: 14, padding: "18px 0 4px" }}>
        <button className="icon-btn" onClick={() => navigate(-1)}>
          <ChevronLeft />
        </button>
        <h1 style={{ fontSize: 28 }}>Артисты</h1>
      </div>
      <div className="page-sub">
        {artists ? `${artists.length} в библиотеке` : "загружаю…"}
      </div>

      {artists === null ? (
        <div className="center-spin">
          <div className="spinner" />
        </div>
      ) : artists.length ? (
        artists.map((a) => <ArtistRow key={`${a.source}-${a.id}-${a.name}`} artist={a} />)
      ) : (
        <div className="empty">
          <h3>Артистов нет</h3>
          <div>Открой артиста и нажми «Добавить в библиотеку»</div>
        </div>
      )}
    </div>
  );
}
