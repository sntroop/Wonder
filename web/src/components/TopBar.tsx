import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { usePlayer } from "../store";
import { QueueIcon, RefreshIcon, SearchIcon } from "./icons";

type Props = {
  onRefresh?: () => void;
  refreshing?: boolean;
};

export function TopBar({ onRefresh, refreshing }: Props) {
  const [q, setQ] = useState("");
  const navigate = useNavigate();
  const queueOpen = usePlayer((s) => s.queueOpen);
  const setQueueOpen = usePlayer((s) => s.setQueueOpen);

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    const value = q.trim();
    if (value) navigate(`/search?q=${encodeURIComponent(value)}`);
  };

  return (
    <header className="topbar">
      <form className="searchbox" onSubmit={submit}>
        <SearchIcon />
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Треки, артисты, альбомы, плейлисты"
          aria-label="Поиск"
        />
      </form>
      <div className="topbar-spacer" />
      {onRefresh ? (
        <button
          className="icon-btn"
          title="Обновить"
          onClick={onRefresh}
          disabled={refreshing}
          style={refreshing ? { opacity: 0.5 } : undefined}
        >
          <RefreshIcon />
        </button>
      ) : null}
      <button
        className="icon-btn"
        title="Очередь"
        onClick={() => setQueueOpen(!queueOpen)}
      >
        <QueueIcon />
      </button>
    </header>
  );
}
