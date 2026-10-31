import { useNavigate } from "react-router-dom";
import { ChevronLeft, DownloadIcon, PlayIcon, QueueIcon, WaveIcon } from "../components/icons";

const WIN_SETUP = "/wonder/downloads/Wonder-Setup-1.0.9.exe";
const WIN_PORTABLE = "/wonder/downloads/Wonder-Windows-latest.zip";

export function DesktopApp() {
  const navigate = useNavigate();

  return (
    <div className="page page-enter win-page">
      <button className="icon-btn" onClick={() => navigate(-1)} title="Назад">
        <ChevronLeft />
      </button>

      <section className="win-hero">
        <div className="win-app-icon">
          <img src="/wonder/logo.png" alt="Wonder" />
        </div>
        <div className="eyebrow">Windows 10 / 11 · x64</div>
        <h1 className="page-title">Wonder для Windows</h1>
        <p className="win-lead">
          Та же музыка, что и в вебе, но отдельным приложением: живёт в трее,
          управляется с клавиатуры и мультимедиа-клавиш, запускается вместе с
          системой и не мешает работе.
        </p>
        <div className="win-actions">
          <a className="btn btn-primary win-download" href={WIN_SETUP} download>
            <DownloadIcon />
            Скачать установщик
          </a>
          <a className="btn btn-ghost" href={WIN_PORTABLE} download>
            Портативная версия
          </a>
        </div>
        <div className="win-meta">
          Версия 1.0.9 · установщик 116 МБ · Windows 10/11 x64
        </div>
      </section>

      <section className="win-features">
        <div className="win-card">
          <QueueIcon />
          <b>Трей и мини-плеер</b>
          <span>Свернул окно — музыка играет, трек виден в трее.</span>
        </div>
        <div className="win-card">
          <PlayIcon />
          <b>Медиа-клавиши</b>
          <span>Play / Pause, следующий и предыдущий трек с клавиатуры.</span>
        </div>
        <div className="win-card">
          <WaveIcon />
          <b>Все разделы</b>
          <span>Волна, шортсы, поиск, библиотека, плейлисты и тексты песен.</span>
        </div>
        <div className="win-card">
          <DownloadIcon />
          <b>Тексты и плейлисты</b>
          <span>Караоке-тексты, ремиксы, комментарии и свои плейлисты.</span>
        </div>
      </section>

      <section className="win-note">
        <b>Как установить</b>
        <ol>
          <li>Скачай Wonder-Setup-1.0.9.exe и запусти. SmartScreen может спросить подтверждение — это нормально для нового приложения без подписи.</li>
          <li>Выбери папку (по умолчанию — Programs\Wonder в профиле пользователя) и нажми «Установить».</li>
          <li>Войди тем же аккаунтом, что и на сайте: библиотека, плейлисты и волна подтянутся.</li>
          <li>Закрытие окна сворачивает Wonder в трей. Полностью выйти — через меню в трее.</li>
        </ol>
        <div className="hint">
          Портативная версия — тот же архив с Wonder.exe, без установки и ярлыков.
        </div>
      </section>

      <section className="win-note">
        <b>Горячие клавиши</b>
        <div className="win-keys">
          <span><kbd>Space</kbd> пауза</span>
          <span><kbd>N</kbd> следующий</span>
          <span><kbd>P</kbd> предыдущий</span>
          <span><kbd>←</kbd> <kbd>→</kbd> ±5 секунд</span>
          <span><kbd>M</kbd> звук</span>
          <span><kbd>Q</kbd> очередь</span>
        </div>
      </section>
    </div>
  );
}
