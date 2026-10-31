import { imgUrl } from "../api";
import { useSkin } from "../skin";
import { useBg } from "../bg";

/**
 * Фон приложения: своя картинка/GIF/видео из оформления или размытая
 * обложка трека (как в APK: ArtworkTint).
 */
export function Background({ art, color }: { art?: string | null; color?: string | null }) {
  const skin = useSkin((s) => s.skin);
  const bgUrl = useBg((s) => s.url);
  const bgKind = useBg((s) => s.kind);

  if (skin.bg === "custom" && bgUrl) {
    return (
      <div className="bg" aria-hidden>
        {bgKind === "video" ? (
          <video
            key={bgUrl}
            className="bg-art bg-user"
            src={bgUrl}
            autoPlay
            muted
            loop
            playsInline
          />
        ) : (
          <img key={bgUrl} className="bg-art bg-user" src={bgUrl} alt="" />
        )}
        <div className="bg-dark bg-dark-soft" />
      </div>
    );
  }

  const url = imgUrl(art, 600);
  return (
    <div className="bg" aria-hidden>
      {url ? <img key={url} className="bg-art" src={url} alt="" /> : null}
      <div
        className="bg-tint"
        style={
          color
            ? {
                background: `radial-gradient(120% 85% at 50% -18%, ${color}bb, transparent 62%), linear-gradient(180deg, ${color}66 0%, rgba(7,7,8,0.92) 55%, #070708 78%)`,
              }
            : undefined
        }
      />
      <div className="bg-dark" />
    </div>
  );
}
