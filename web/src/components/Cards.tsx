import { useNavigate } from "react-router-dom";
import type { Playlist, Track } from "../api";
import { usePlayer } from "../store";
import { Cover } from "./Cover";
import { PlayIcon } from "./icons";

export function TrackCard({
  track,
  queue,
  index,
}: {
  track: Track;
  queue?: Track[];
  index?: number;
}) {
  const play = usePlayer((s) => s.play);
  const current = usePlayer((s) => s.current());
  const isCurrent = current?.source === track.source && current?.id === track.id;

  return (
    <div
      className="card"
      onClick={() => play(queue?.length ? queue : [track], index ?? 0)}
      title={`${track.artist} — ${track.title}`}
    >
      <div className="card-cover">
        <Cover src={track.artwork} size={400} />
        <div className={"card-play" + (isCurrent ? " visible" : "")}>
          <PlayIcon />
        </div>
      </div>
      <div className="card-title" style={isCurrent ? { color: "#8fd6ff" } : undefined}>
        {track.title}
      </div>
      <div className="card-sub">{track.artist}</div>
    </div>
  );
}

export function PlaylistCard({ playlist }: { playlist: Playlist }) {
  const navigate = useNavigate();
  const open = () =>
    navigate(
      `/playlist/${playlist.owner_id || 0}/${playlist.id}` +
        (playlist.access_key ? `?access_key=${encodeURIComponent(playlist.access_key)}` : ""),
    );

  return (
    <div className="card" onClick={open}>
      <div className="card-cover">
        <Cover src={playlist.cover} size={400} />
        <div className="card-play">
          <PlayIcon />
        </div>
      </div>
      <div className="card-title">{playlist.title}</div>
      <div className="card-sub">
        {playlist.author ? `${playlist.author} · ` : ""}
        {playlist.count ? `${playlist.count} треков` : playlist.source === "wonder" ? "плейлист Wonder" : ""}
      </div>
    </div>
  );
}
