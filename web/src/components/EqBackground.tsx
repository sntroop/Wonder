import { useEffect, useRef } from "react";
import { API, type Track } from "../api";
import { usePlayer } from "../store";

type BeatMap = {
  rate?: number;
  energy?: number[];
  beats?: number[];
  pending?: boolean;
};

/**
 * Эквалайзер вокруг обложки — порт EqBackground.kt.
 *
 * Сервер отдаёт карту громкости (/api/beat, 25 замеров в секунду) и список
 * битов. Клиент просто листает карту по текущей позиции плеера, поэтому
 * лучи попадают в трек, а не живут своей жизнью.
 *
 * Рисуется поверх родителя: родитель должен быть position: relative и
 * квадратным (обложка), canvas растягивается на 210% и центрируется.
 */
export function EqBackground({
  track,
  color,
  playing,
  frac = 0.5,
}: {
  track?: Track | null;
  color?: string | null;
  playing: boolean;
  /** радиус обложки как доля стороны родителя (0.5 = обложка во всю ширину) */
  frac?: number;
}) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const mapRef = useRef<BeatMap | null>(null);
  const levelRef = useRef(0);
  const beatRef = useRef(0);

  // карта громкости: pending -> переспрашиваем
  useEffect(() => {
    mapRef.current = null;
    levelRef.current = 0;
    if (!track) return;
    let alive = true;
    let timer: number | undefined;

    const load = async () => {
      try {
        const res = await fetch(`${API}/beat/${track.source}/${track.id}`);
        const data = (await res.json()) as BeatMap;
        if (!alive) return;
        if (data.pending) {
          timer = window.setTimeout(load, 2500);
          return;
        }
        mapRef.current = data;
      } catch {
        /* без карты — ровное дыхание */
      }
    };
    void load();
    return () => {
      alive = false;
      window.clearTimeout(timer);
    };
  }, [track?.source, track?.id]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;
    const host = canvas.parentElement;
    let raf = 0;
    // timeupdate приходит 4 раза в секунду — между ними доводим позицию сами,
    // иначе лучи дёргаются и не попадают в ритм
    let lastTime = -1;
    let lastStamp = 0;

    const resize = () => {
      const dpr = Math.min(2, window.devicePixelRatio || 1);
      const rect = canvas.getBoundingClientRect();
      canvas.width = Math.max(1, Math.round(rect.width * dpr));
      canvas.height = Math.max(1, Math.round(rect.height * dpr));
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    };
    resize();
    const ro = new ResizeObserver(resize);
    ro.observe(canvas);

    const rgb = (() => {
      if (!color) return [255, 255, 255];
      const m = color.replace("#", "");
      return [
        parseInt(m.slice(0, 2), 16),
        parseInt(m.slice(2, 4), 16),
        parseInt(m.slice(4, 6), 16),
      ];
    })();

    const draw = () => {
      raf = requestAnimationFrame(draw);
      const w = canvas.clientWidth;
      const h = canvas.clientHeight;
      if (!w || !h) return;

      const st = usePlayer.getState();
      const now = performance.now();
      if (st.time !== lastTime) {
        lastTime = st.time;
        lastStamp = now;
      }
      const drift = st.playing ? (now - lastStamp) / 1000 : 0;
      const pos = Math.min(st.time + drift, st.duration || Number.POSITIVE_INFINITY);
      const map = mapRef.current;
      const rate = map?.rate || 25;
      const energyArr = map?.energy || [];
      const idx = Math.min(
        Math.max(0, Math.floor(pos * rate)),
        Math.max(0, energyArr.length - 1),
      );
      const raw = map && energyArr.length ? Math.min(1, energyArr[idx] || 0) : 0;

      // быстро подхватываем атаку, мягко отпускаем — так «бьёт»
      const k = raw > levelRef.current ? 0.5 : 0.12;
      levelRef.current += (raw - levelRef.current) * k;
      const level = levelRef.current;

      // бит
      let beat = 0;
      for (const b of map?.beats || []) {
        const d = pos - b;
        if (d >= 0 && d < 0.35) beat = Math.max(beat, 1 - d / 0.35);
      }
      beatRef.current += (beat - beatRef.current) * 0.5;

      if (host) {
        host.style.setProperty("--beat", beatRef.current.toFixed(3));
        host.style.setProperty("--level", Math.min(1, level * 1.4).toFixed(3));
      }

      ctx.clearRect(0, 0, w, h);

      // canvas в 2.1 раза больше родителя; frac — радиус обложки
      const base = (Math.min(w, h) / 2.1) * frac;
      const cx = w / 2;
      const cy = h / 2;
      const t = performance.now() / 1000;

      // без карты — мягкое «дыхание», чтобы эквалайзер всё равно жил
      const fallback = 0.34 + 0.18 * Math.sin(t * 1.9) + 0.12 * Math.sin(t * 3.1 + 1.2);
      const lvl = map ? level : fallback;
      const pulse = 1 + beatRef.current * 0.7;

      // свечение цвета обложки
      const glow = ctx.createRadialGradient(cx, cy, base * 0.7, cx, cy, base * 1.85);
      glow.addColorStop(0, `rgba(${rgb[0]},${rgb[1]},${rgb[2]},${0.08 + lvl * 0.18})`);
      glow.addColorStop(1, "rgba(0,0,0,0)");
      ctx.fillStyle = glow;
      ctx.fillRect(0, 0, w, h);

      // ровное кольцо «эквалайзера»: все лучи одной длины, дышат вместе
      const count = 72;
      const inner = base * 1.035;
      const len = base * (0.06 + lvl * 0.3) * pulse;
      ctx.lineCap = "round";
      ctx.lineWidth = Math.max(2.5, base * 0.022);

      for (let i = 0; i < count; i += 1) {
        const angle = (i / count) * Math.PI * 2 - Math.PI / 2;
        // лёгкая разница между соседними лучами (как у спектра), но без шума
        const accent = i % 4 === 0 ? 1.28 : i % 2 === 0 ? 1.08 : 0.92;
        const outer = inner + Math.max(1.5, len * accent);
        const alpha = 0.2 + lvl * 0.7 * accent;
        ctx.strokeStyle = `rgba(255,255,255,${Math.min(0.85, alpha)})`;
        ctx.beginPath();
        ctx.moveTo(cx + Math.cos(angle) * inner, cy + Math.sin(angle) * inner);
        ctx.lineTo(cx + Math.cos(angle) * outer, cy + Math.sin(angle) * outer);
        ctx.stroke();
      }
    };

    raf = requestAnimationFrame(draw);
    return () => {
      cancelAnimationFrame(raf);
      ro.disconnect();
      if (host) {
        host.style.removeProperty("--beat");
        host.style.removeProperty("--level");
      }
    };
  }, [color, playing, frac]);

  return <canvas ref={canvasRef} className="eq-bg" aria-hidden />;
}
