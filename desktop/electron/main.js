'use strict';

/**
 * Wonder desktop (Electron, main process).
 *
 * Схема: локальный HTTP-сервер на 127.0.0.1:45871 отдаёт собранный web/ по
 * путям /wonder/*, а запросы /wonder/api/*, /wonder/own/*, /wonder/apk/*
 * стримит на WONDER_BACKEND (по умолчанию http://127.0.0.1:8400). Окно грузит http://127.0.0.1:45871/wonder/
 * - то же самое приложение, что и на сайте, но без внешнего браузера.
 */

const {
  app,
  BrowserWindow,
  Tray,
  Menu,
  nativeImage,
  ipcMain,
  shell,
  dialog,
  globalShortcut,
  screen,
} = require('electron');

const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');

const APP_ID = 'su.gwarden.wonder';
const HOST = '127.0.0.1';
const PORT = 45871;
const ORIGIN = `http://${HOST}:${PORT}`;
// Публичная сборка: бэкенд не входит в репозиторий. Укажи свой адрес
// (корень music-player, без /wonder) через переменную окружения.
const REMOTE_ORIGIN = process.env.WONDER_BACKEND || 'http://127.0.0.1:8400';
const REMOTE_URL = new URL(REMOTE_ORIGIN);
const REMOTE_HOST = REMOTE_URL.host;
const REMOTE_PORT = Number(REMOTE_URL.port || (REMOTE_URL.protocol === 'https:' ? 443 : 80));
const REMOTE_MODULE = REMOTE_URL.protocol === 'https:' ? https : http;
const FEED_URL = ''; // OTA-фид в публичной сборке отключён

const WEB_ROOT = path.join(__dirname, '..', 'web');
const PRELOAD = path.join(__dirname, 'preload.js');

const LOCAL_WEB = `${ORIGIN}/wonder/`;
const REMOTE_WEB = `${REMOTE_ORIGIN.replace(/\/$/, '')}/wonder/`;

/**
 * Интерфейс приложения лежит рядом в resources/app/web. Если после кривого
 * обновления папки нет, раньше каждый маршрут отдавал JSON
 * {"error":"not_found","path":"/wonder/settings"} - окно выглядело мёртвым.
 * Теперь без локальной копии молча открываем онлайн-версию.
 */
function localWebReady() {
  try {
    const st = fs.statSync(path.join(WEB_ROOT, 'index.html'));
    return st.isFile() && st.size > 0;
  } catch {
    return false;
  }
}

const HAS_LOCAL_WEB = localWebReady();
const BASE_URL = HAS_LOCAL_WEB ? LOCAL_WEB : REMOTE_WEB;
const ALLOWED_ORIGINS = new Set([ORIGIN, REMOTE_ORIGIN]);

const startedHidden = process.argv.includes('--hidden');

let mainWindow = null;
let tray = null;
let isQuitting = false;
let hiddenBalloonShown = false;
let tooltipTimer = null;
let lastTooltip = '';
let pendingDeepLink = null;
let windowStateFile = null;
let settingsFile = null;
let settings = { autostart: false };

app.setName('Wonder');
app.setAppUserModelId(APP_ID);
app.commandLine.appendSwitch('autoplay-policy', 'no-user-gesture-required');

/* ── файлы состояния ─────────────────────────────────────────────────── */

function readJson(file, fallback) {
  try {
    return JSON.parse(fs.readFileSync(file, 'utf8'));
  } catch {
    return fallback;
  }
}

function writeJson(file, value) {
  try {
    fs.mkdirSync(path.dirname(file), { recursive: true });
    fs.writeFileSync(file, JSON.stringify(value, null, 2), 'utf8');
  } catch (err) {
    console.warn('[wonder] не удалось сохранить', file, err.message);
  }
}

/* ── MIME ────────────────────────────────────────────────────────────── */

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.htm': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.map': 'application/json; charset=utf-8',
  '.txt': 'text/plain; charset=utf-8',
  '.xml': 'application/xml; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.webp': 'image/webp',
  '.gif': 'image/gif',
  '.ico': 'image/x-icon',
  '.avif': 'image/avif',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.otf': 'font/otf',
  '.mp3': 'audio/mpeg',
  '.m4a': 'audio/mp4',
  '.ogg': 'audio/ogg',
  '.wav': 'audio/wav',
  '.mp4': 'video/mp4',
  '.webm': 'video/webm',
  '.wasm': 'application/wasm',
};

function mimeFor(file) {
  return MIME[path.extname(file).toLowerCase()] || 'application/octet-stream';
}

/* ── локальный статик-сервер + прокси ────────────────────────────────── */

const PROXY_PREFIXES = ['/wonder/api/', '/wonder/own/', '/wonder/apk/'];

function isProxyPath(pathname) {
  return PROXY_PREFIXES.some(
    (p) => pathname === p.slice(0, -1) || pathname.startsWith(p),
  );
}

function sendJson(res, status, obj) {
  const body = Buffer.from(JSON.stringify(obj));
  res.writeHead(status, {
    'content-type': 'application/json; charset=utf-8',
    'content-length': body.length,
    'cache-control': 'no-store',
  });
  res.end(body);
}

function proxyRequest(req, res) {
  const headers = { ...req.headers, host: REMOTE_HOST };
  // same-origin для локального бэкенда
  if (headers.origin) headers.origin = REMOTE_ORIGIN;
  if (headers.referer) headers.referer = `${REMOTE_ORIGIN}/wonder/`;
  // просим identity, чтобы тело шло как есть; если апстрим всё же сжал -
  // content-encoding ниже пробрасывается
  delete headers['accept-encoding'];
  delete headers['proxy-connection'];
  delete headers['connection'];
  delete headers['upgrade'];
  delete headers['transfer-encoding'];

  let settled = false;
  const fail = (err) => {
    if (settled) return;
    settled = true;
    if (!res.headersSent) {
      try {
        sendJson(res, 502, {
          error: 'bad_gateway',
          detail: err && err.message ? err.message : 'upstream error',
        });
      } catch {
        /* клиент уже ушёл */
      }
    } else {
      try {
        res.end();
      } catch {
        /* уже закрыт */
      }
    }
  };

  const proxied = REMOTE_MODULE.request(
    {
      host: REMOTE_URL.hostname,
      port: REMOTE_PORT,
      method: req.method,
      // локальный music-player ждёт /api, /own, /apk без префикса /wonder
      path: req.url.replace(/^\/wonder/, ''),
      headers,
      ...(REMOTE_URL.protocol === 'https:' ? { servername: REMOTE_URL.hostname } : {}),
    },
    (upstream) => {
      settled = true;
      const out = {};
      const keep = [
        'content-type',
        'content-length',
        'content-range',
        'accept-ranges',
        'cache-control',
        'etag',
        'last-modified',
        'content-disposition',
        'content-encoding',
        'vary',
        'expires',
        'location',
      ];
      for (const key of keep) {
        if (upstream.headers[key] !== undefined) out[key] = upstream.headers[key];
      }
      try {
        res.writeHead(upstream.statusCode || 502, upstream.statusMessage, out);
      } catch {
        upstream.destroy();
        return;
      }
      upstream.pipe(res);
      upstream.on('error', fail);
    },
  );

  proxied.setTimeout(60000, () => {
    proxied.destroy(new Error('timeout 60s'));
  });
  proxied.on('error', fail);
  req.on('aborted', () => proxied.destroy());
  res.on('close', () => {
    if (!res.writableEnded) proxied.destroy();
  });
  req.pipe(proxied);
}

function serveStatic(req, res, pathname) {
  // Локальной копии интерфейса нет (битое/неполное обновление) - не отдаём
  // JSON-стену, а уводим на сайт по тому же маршруту.
  if (!HAS_LOCAL_WEB) {
    const accept = String(req.headers.accept || '');
    if (accept.includes('text/html') || !path.extname(pathname)) {
      const tail = pathname.replace(/^\/wonder/, '') || '/';
      res.writeHead(302, { location: `${REMOTE_WEB.replace(/\/$/, '')}${tail}` });
      return res.end();
    }
    return sendJson(res, 404, { error: 'not_found', path: pathname });
  }

  let rel;
  try {
    rel = decodeURIComponent(pathname.slice('/wonder'.length));
  } catch {
    return sendJson(res, 400, { error: 'bad_path' });
  }
  rel = rel.replace(/\\/g, '/');
  if (rel === '' || rel === '/') rel = '/index.html';

  const root = path.resolve(WEB_ROOT);
  let target = path.resolve(root, '.' + rel);
  if (target !== root && !target.startsWith(root + path.sep)) {
    return sendJson(res, 403, { error: 'forbidden' });
  }

  let stat = null;
  try {
    stat = fs.statSync(target);
  } catch {
    stat = null;
  }
  if (stat && stat.isDirectory()) {
    target = path.join(target, 'index.html');
    try {
      stat = fs.statSync(target);
    } catch {
      stat = null;
    }
  }

  // SPA fallback: путь без расширения отдаём как index.html
  if (!stat && !path.extname(rel)) {
    target = path.join(root, 'index.html');
    try {
      stat = fs.statSync(target);
    } catch {
      stat = null;
    }
  }

  if (!stat || !stat.isFile()) {
    return sendJson(res, 404, { error: 'not_found', path: pathname });
  }

  const ext = path.extname(target).toLowerCase();
  const isHtml = ext === '.html';

  // читаем файл целиком: самые большие тут шрифт и бандл (~1 МБ), зато
  // работает и из asar, где стримы с диапазонами не гарантированы
  let data;
  try {
    data = fs.readFileSync(target);
  } catch {
    return sendJson(res, 404, { error: 'not_found', path: pathname });
  }

  const size = data.length;
  const headers = {
    'content-type': mimeFor(target),
    'content-length': size,
    'cache-control': isHtml ? 'no-cache' : 'public, max-age=3600',
    'accept-ranges': 'bytes',
  };

  if (req.method === 'HEAD') {
    res.writeHead(200, headers);
    return res.end();
  }

  // Range для локальных файлов (на всякий случай, аудио идёт через API)
  const range = req.headers.range;
  if (range && /^bytes=/.test(range)) {
    const m = /^bytes=(\d*)-(\d*)$/.exec(range.trim());
    if (m) {
      let start = m[1] ? parseInt(m[1], 10) : 0;
      let end = m[2] ? parseInt(m[2], 10) : size - 1;
      if (!m[1] && m[2]) {
        // суффиксный range: bytes=-500 - последние 500 байт
        start = Math.max(0, size - parseInt(m[2], 10));
        end = size - 1;
      }
      if (Number.isNaN(start) || Number.isNaN(end) || start > end || start >= size) {
        res.writeHead(416, { 'content-range': `bytes */${size}` });
        return res.end();
      }
      end = Math.min(end, size - 1);
      headers['content-range'] = `bytes ${start}-${end}/${size}`;
      headers['content-length'] = end - start + 1;
      res.writeHead(206, headers);
      return res.end(data.subarray(start, end + 1));
    }
  }

  res.writeHead(200, headers);
  res.end(data);
}

function handleRequest(req, res) {
  let pathname = '/';
  try {
    pathname = new URL(req.url, ORIGIN).pathname;
  } catch {
    return sendJson(res, 400, { error: 'bad_url' });
  }

  if (req.method === 'OPTIONS' && isProxyPath(pathname)) {
    // CORS не нужен (same-origin), но preflight не должен уходить наверх
    res.writeHead(204, { 'cache-control': 'no-store' });
    return res.end();
  }

  if (isProxyPath(pathname)) return proxyRequest(req, res);

  if (pathname === '/wonder') {
    res.writeHead(301, { location: '/wonder/' });
    return res.end();
  }

  if (pathname === '/healthz') return sendJson(res, 200, { ok: true });

  if (!pathname.startsWith('/wonder/')) {
    return sendJson(res, 404, { error: 'not_found' });
  }

  if (req.method !== 'GET' && req.method !== 'HEAD') {
    return sendJson(res, 405, { error: 'method_not_allowed' });
  }

  return serveStatic(req, res, pathname);
}

function startServer() {
  return new Promise((resolve, reject) => {
    const server = http.createServer((req, res) => {
      try {
        handleRequest(req, res);
      } catch (err) {
        console.error('[wonder] handler error', err);
        try {
          sendJson(res, 500, { error: 'internal' });
        } catch {
          /* closed */
        }
      }
    });
    server.on('error', reject);
    server.listen(PORT, HOST, () => {
      console.log(`[wonder] static+proxy on ${ORIGIN}/wonder/`);
      resolve(server);
    });
  });
}

/* ── состояние окна ──────────────────────────────────────────────────── */

function saveWindowState() {
  if (!mainWindow || mainWindow.isDestroyed() || !windowStateFile) return;
  const maximized = mainWindow.isMaximized();
  const bounds = maximized ? mainWindow.getNormalBounds() : mainWindow.getBounds();
  writeJson(windowStateFile, { ...bounds, maximized });
}

function restoreWindowState() {
  const fallback = { width: 1440, height: 920 };
  const state = readJson(windowStateFile, null);
  if (!state || typeof state !== 'object') return fallback;

  const width = Math.max(1024, Number(state.width) || fallback.width);
  const height = Math.max(700, Number(state.height) || fallback.height);
  const result = { width, height };

  if (Number.isFinite(state.x) && Number.isFinite(state.y)) {
    const displays = screen.getAllDisplays();
    const visible = displays.some((d) => {
      const b = d.workArea;
      return (
        state.x + 80 > b.x &&
        state.x < b.x + b.width - 80 &&
        state.y + 40 > b.y &&
        state.y < b.y + b.height - 40
      );
    });
    if (visible) {
      result.x = Math.round(state.x);
      result.y = Math.round(state.y);
    }
  }
  result.maximized = Boolean(state.maximized);
  return result;
}

/* ── окно ────────────────────────────────────────────────────────────── */

function showWindow() {
  if (!mainWindow || mainWindow.isDestroyed()) return;
  if (mainWindow.isMinimized()) mainWindow.restore();
  mainWindow.show();
  mainWindow.focus();
}

function toggleWindow() {
  if (!mainWindow || mainWindow.isDestroyed()) return;
  if (mainWindow.isVisible() && !mainWindow.isMinimized()) {
    mainWindow.hide();
  } else {
    showWindow();
  }
}

function createWindow() {
  const state = restoreWindowState();

  mainWindow = new BrowserWindow({
    width: state.width,
    height: state.height,
    x: state.x,
    y: state.y,
    minWidth: 1024,
    minHeight: 700,
    backgroundColor: '#070708',
    autoHideMenuBar: true,
    show: false,
    title: 'Wonder',
    icon: resolveWindowIcon(),
    webPreferences: {
      preload: PRELOAD,
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false,
      spellcheck: false,
      backgroundThrottling: false,
    },
  });

  if (state.maximized) mainWindow.maximize();

  mainWindow.loadURL(BASE_URL);

  mainWindow.once('ready-to-show', () => {
    if (!startedHidden) showWindow();
  });

  // скрытие в трей вместо закрытия (если трея нет - закрываемся по-настоящему)
  mainWindow.on('close', (event) => {
    if (isQuitting) return;
    if (!tray) {
      isQuitting = true;
      return;
    }
    event.preventDefault();
    mainWindow.hide();
    if (!hiddenBalloonShown && tray && process.platform === 'win32') {
      hiddenBalloonShown = true;
      try {
        tray.displayBalloon({
          title: 'Wonder',
          content: 'Wonder свёрнут в трей',
          iconType: 'info',
        });
      } catch {
        /* не критично */
      }
    }
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  let saveTimer = null;
  const scheduleSave = () => {
    clearTimeout(saveTimer);
    saveTimer = setTimeout(saveWindowState, 400);
  };
  mainWindow.on('resize', scheduleSave);
  mainWindow.on('move', scheduleSave);
  mainWindow.on('maximize', scheduleSave);
  mainWindow.on('unmaximize', scheduleSave);

  // навигация: только свой origin, внешние ссылки - в системный браузер
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    if (/^https?:\/\//i.test(url)) shell.openExternal(url).catch(() => {});
    return { action: 'deny' };
  });

  mainWindow.webContents.on('will-navigate', (event, url) => {
    try {
      const target = new URL(url);
      if (!ALLOWED_ORIGINS.has(`${target.protocol}//${target.host}`)) {
        event.preventDefault();
        if (/^https?:/i.test(url)) shell.openExternal(url).catch(() => {});
      }
    } catch {
      event.preventDefault();
    }
  });

  // блокируем вложения/webview
  mainWindow.webContents.on('will-attach-webview', (event) => {
    event.preventDefault();
  });

  mainWindow.webContents.on('did-finish-load', () => {
    if (pendingDeepLink) {
      const link = pendingDeepLink;
      pendingDeepLink = null;
      navigateDeepLink(link);
    }
  });
}

/* ── deep links (wonder://) ──────────────────────────────────────────── */

function navigateDeepLink(rawUrl) {
  let route = '/';
  try {
    const u = new URL(rawUrl);
    if (u.protocol !== 'wonder:') return;
    const fromQuery = u.searchParams.get('route') || u.searchParams.get('path');
    if (fromQuery) {
      route = fromQuery.startsWith('/') ? fromQuery : `/${fromQuery}`;
    } else {
      route = `/${u.hostname}${u.pathname}`.replace(/\/{2,}/g, '/');
      if (u.search) route += u.search;
    }
    if (u.hash) route += u.hash;
  } catch {
    return;
  }

  const target = `${BASE_URL.replace(/\/$/, '')}${route}`;
  if (!mainWindow || mainWindow.isDestroyed()) return;
  showWindow();
  mainWindow.loadURL(target).catch(() => {});
}

function handleArgv(argv) {
  const link = (argv || []).find((a) => typeof a === 'string' && a.startsWith('wonder://'));
  if (!link) return;
  if (mainWindow && !mainWindow.webContents.isLoading()) {
    navigateDeepLink(link);
  } else {
    pendingDeepLink = link;
  }
}

function setupProtocolClient() {
  // регистрируем только упакованную версию: в dev exe - это electron,
  // и запись в реестр Windows была бы мусорной
  if (!app.isPackaged) return;
  try {
    app.setAsDefaultProtocolClient('wonder');
  } catch (err) {
    console.warn('[wonder] protocol register failed', err.message);
  }
}

/* ── трей ────────────────────────────────────────────────────────────── */

function resolveTrayIcon() {
  const candidates = [
    path.join(__dirname, 'assets', 'tray.png'),
    path.join(process.resourcesPath || '', 'build', 'tray.png'),
    path.join(__dirname, '..', 'build', 'tray.png'),
    path.join(__dirname, '..', 'build', 'icon.png'),
    path.join(process.resourcesPath || '', 'build', 'icon.png'),
  ];
  for (const file of candidates) {
    try {
      if (file && fs.existsSync(file)) {
        // читаем буфером: так работает и внутри app.asar
        const img = nativeImage.createFromBuffer(fs.readFileSync(file));
        if (!img.isEmpty()) return img;
      }
    } catch {
      /* следующий кандидат */
    }
  }
  return nativeImage.createEmpty();
}

// иконка окна/панели задач: тёмная плитка, а не белый знак для трея
function resolveWindowIcon() {
  const candidates = [
    path.join(__dirname, 'assets', 'icon.png'),
    path.join(process.resourcesPath || '', 'build', 'icon.png'),
    path.join(__dirname, '..', 'build', 'icon.png'),
  ];
  for (const file of candidates) {
    try {
      if (file && fs.existsSync(file)) {
        // читаем буфером: так работает и внутри app.asar
        const img = nativeImage.createFromBuffer(fs.readFileSync(file));
        if (!img.isEmpty()) return img;
      }
    } catch {
      /* следующий кандидат */
    }
  }
  return resolveTrayIcon();
}

function runPlayer(code) {
  if (!mainWindow || mainWindow.isDestroyed()) return Promise.resolve(false);
  const script = `(() => {
    try {
      const p = window.__wonder && window.__wonder.player;
      if (!p) return false;
      const s = p.getState();
      if (!s) return false;
      ${code}
      return true;
    } catch (e) {
      return false;
    }
  })()`;
  return mainWindow.webContents.executeJavaScript(script, true).catch(() => false);
}

function playerToggle() {
  return runPlayer('s.toggle();');
}
function playerNext() {
  return runPlayer('s.next();');
}
function playerPrev() {
  return runPlayer('s.prev();');
}
function playerPause() {
  return runPlayer('if (typeof s.pause === "function") s.pause();');
}

function rebuildTrayMenu() {
  if (!tray) return;
  const menu = Menu.buildFromTemplate([
    { label: 'Показать/Скрыть', click: () => toggleWindow() },
    { label: 'Играть/Пауза', click: () => playerToggle() },
    { label: 'Следующий трек', click: () => playerNext() },
    { label: 'Предыдущий трек', click: () => playerPrev() },
    { type: 'separator' },
    {
      label: 'Автозапуск',
      type: 'checkbox',
      checked: Boolean(settings.autostart),
      click: (item) => {
        setAutostart(item.checked);
        rebuildTrayMenu();
      },
    },
    { label: 'Проверить обновления', click: () => checkUpdates(true) },
    { type: 'separator' },
    {
      label: 'Выход',
      click: () => {
        isQuitting = true;
        app.quit();
      },
    },
  ]);
  tray.setContextMenu(menu);
}

function createTray() {
  try {
    tray = new Tray(resolveTrayIcon());
  } catch (err) {
    console.warn('[wonder] tray failed', err.message);
    tray = null;
    return;
  }
  tray.setToolTip('Wonder');
  rebuildTrayMenu();
  tray.on('click', () => toggleWindow());
  tray.on('double-click', () => showWindow());

  tooltipTimer = setInterval(updateTooltip, 2000);
  updateTooltip();
}

let tooltipBusy = false;
async function updateTooltip() {
  if (!tray || tooltipBusy) return;
  if (!mainWindow || mainWindow.isDestroyed()) return;
  if (mainWindow.webContents.isLoading()) return;
  tooltipBusy = true;
  try {
    const info = await mainWindow.webContents.executeJavaScript(
      `(() => {
        try {
          const p = window.__wonder && window.__wonder.player;
          if (!p) return null;
          const s = p.getState();
          const t = typeof s.current === 'function' ? s.current() : null;
          if (!t) return null;
          return { title: String(t.title || ''), artist: String(t.artist || '') };
        } catch (e) { return null; }
      })()`,
      false,
    );
    if (!tray) return;
    const text = info
      ? `Wonder - ${info.artist ? info.artist + ' - ' : ''}${info.title}`.slice(0, 120)
      : 'Wonder';
    if (text !== lastTooltip) {
      lastTooltip = text;
      tray.setToolTip(text);
    }
  } catch {
    /* окно могло перезагружаться */
  } finally {
    tooltipBusy = false;
  }
}

/* ── автозапуск ──────────────────────────────────────────────────────── */

function applyLoginItem(enabled) {
  try {
    if (process.platform === 'win32' || process.platform === 'darwin') {
      app.setLoginItemSettings({
        openAtLogin: Boolean(enabled),
        openAsHidden: Boolean(enabled),
        path: process.execPath,
        args: ['--hidden'],
      });
    }
  } catch (err) {
    console.warn('[wonder] setLoginItemSettings failed', err.message);
  }
}

function setAutostart(enabled) {
  settings.autostart = Boolean(enabled);
  writeJson(settingsFile, settings);
  applyLoginItem(settings.autostart);
  return settings.autostart;
}

/* ── обновления ──────────────────────────────────────────────────────── */

let autoUpdater = null;
let updaterReady = false;
let manualUpdateCheck = false;

let lastUpdateStatus = null;

function sendUpdateStatus(payload) {
  lastUpdateStatus = payload;
  if (!mainWindow || mainWindow.isDestroyed()) return;
  try {
    mainWindow.webContents.send('wonder:update-status', payload);
  } catch {
    /* окно закрыто */
  }
}

function showBox(options) {
  try {
    if (mainWindow && !mainWindow.isDestroyed()) {
      return dialog.showMessageBox(mainWindow, options);
    }
    return dialog.showMessageBox(options);
  } catch {
    return Promise.resolve({ response: 1 });
  }
}

function setupUpdater() {
  if (!FEED_URL) return;
  if (!app.isPackaged) return;
  try {
    ({ autoUpdater } = require('electron-updater'));
  } catch (err) {
    console.warn('[wonder] electron-updater недоступен', err.message);
    return;
  }
  try {
    autoUpdater.autoDownload = false;
    autoUpdater.autoInstallOnAppQuit = true;
    autoUpdater.setFeedURL({ provider: 'generic', url: FEED_URL });
    updaterReady = true;

    autoUpdater.on('checking-for-update', () => sendUpdateStatus({ state: 'checking' }));

    autoUpdater.on('update-available', (info) => {
      // не блокируем модалкой: наверху окна появляется плашка с кнопкой
      sendUpdateStatus({ state: 'available', version: info && info.version });
    });

    autoUpdater.on('update-not-available', () => {
      sendUpdateStatus({ state: 'latest' });
      if (manualUpdateCheck) {
        showBox({
          type: 'info',
          title: 'Wonder',
          message: 'У вас последняя версия',
          detail: `Текущая версия: ${app.getVersion()}`,
          buttons: ['OK'],
        }).catch(() => {});
      }
      manualUpdateCheck = false;
    });

    autoUpdater.on('download-progress', (p) => {
      sendUpdateStatus({ state: 'downloading', percent: Math.round(p.percent || 0) });
    });

    autoUpdater.on('update-downloaded', (info) => {
      sendUpdateStatus({ state: 'downloaded', version: info && info.version });
      // окно свёрнуто в трей - иначе пользователь не увидит плашку
      const visible = mainWindow && !mainWindow.isDestroyed() && mainWindow.isVisible();
      if (!visible) {
        showBox({
            type: 'info',
            title: 'Обновление готово',
            message: 'Обновление загружено',
            detail: 'Перезапустить Wonder, чтобы установить его?',
            buttons: ['Перезапустить', 'Позже'],
            defaultId: 0,
            cancelId: 1,
          })
          .then(({ response }) => {
            if (response === 0) {
              isQuitting = true;
              setImmediate(() => autoUpdater.quitAndInstall());
            }
          })
          .catch(() => {});
      }
    });

    autoUpdater.on('error', (err) => {
      console.warn('[wonder] updater error', err && err.message);
      sendUpdateStatus({ state: 'error', message: err && err.message });
    });
  } catch (err) {
    updaterReady = false;
    console.warn('[wonder] updater init failed', err.message);
  }
}

async function checkUpdates(manual) {
  manualUpdateCheck = Boolean(manual);
  if (!app.isPackaged) {
    if (manual) {
      showBox({
        type: 'info',
        title: 'Wonder',
        message: 'Проверка обновлений доступна только в собранной версии',
        buttons: ['OK'],
      }).catch(() => {});
    }
    manualUpdateCheck = false;
    return;
  }
  if (!updaterReady || !autoUpdater) {
    if (manual) {
      showBox({
        type: 'warning',
        title: 'Wonder',
        message: 'Обновления недоступны: модуль updater не загрузился',
        buttons: ['OK'],
      }).catch(() => {});
    }
    manualUpdateCheck = false;
    return;
  }
  try {
    await autoUpdater.checkForUpdates();
  } catch (err) {
    console.warn('[wonder] checkForUpdates failed', err && err.message);
    sendUpdateStatus({ state: 'error', message: err && err.message });
    if (manual) {
      showBox({
        type: 'warning',
        title: 'Wonder',
        message: 'Не удалось проверить обновления',
        detail: String((err && err.message) || err),
        buttons: ['OK'],
      }).catch(() => {});
    }
    manualUpdateCheck = false;
  }
}

/* ── IPC ─────────────────────────────────────────────────────────────── */

function setupIpc() {
  ipcMain.on('wonder:version', (event) => {
    event.returnValue = app.getVersion();
  });

  ipcMain.handle('wonder:minimize', () => {
    if (mainWindow && !mainWindow.isDestroyed()) mainWindow.minimize();
  });

  ipcMain.handle('wonder:hide', () => {
    if (mainWindow && !mainWindow.isDestroyed()) mainWindow.hide();
  });

  ipcMain.handle('wonder:quit', () => {
    isQuitting = true;
    app.quit();
  });

  ipcMain.handle('wonder:get-autostart', () => Boolean(settings.autostart));

  ipcMain.handle('wonder:toggle-autostart', (_event, value) => {
    const next = typeof value === 'boolean' ? value : !settings.autostart;
    const result = setAutostart(next);
    rebuildTrayMenu();
    return result;
  });

  ipcMain.handle('wonder:check-updates', () => {
    checkUpdates(true);
  });

  ipcMain.handle('wonder:get-update-status', () => lastUpdateStatus);

  // кнопки в плашке обновления сверху (без модальных окон)
  ipcMain.handle('wonder:download-update', () => {
    if (!updaterReady || !autoUpdater) return { ok: false };
    autoUpdater.downloadUpdate().catch((err) => {
      console.warn('[wonder] download', err.message);
      sendUpdateStatus({ state: 'error', message: err.message });
    });
    return { ok: true };
  });

  ipcMain.handle('wonder:install-update', () => {
    if (!updaterReady || !autoUpdater) return { ok: false };
    isQuitting = true;
    setImmediate(() => autoUpdater.quitAndInstall());
    return { ok: true };
  });
}

/* ── медиа-клавиши ───────────────────────────────────────────────────── */

function registerMediaKeys() {
  const keys = [
    ['MediaPlayPause', playerToggle],
    ['MediaNextTrack', playerNext],
    ['MediaPreviousTrack', playerPrev],
    ['MediaStop', playerPause],
  ];
  for (const [accelerator, handler] of keys) {
    try {
      const ok = globalShortcut.register(accelerator, () => handler());
      if (!ok) console.warn(`[wonder] media key busy: ${accelerator}`);
    } catch (err) {
      console.warn(`[wonder] media key failed: ${accelerator}`, err.message);
    }
  }
}

function unregisterMediaKeys() {
  try {
    globalShortcut.unregisterAll();
  } catch {
    /* ignore */
  }
}

/* ── запуск ──────────────────────────────────────────────────────────── */

const gotLock = app.requestSingleInstanceLock();
if (!gotLock) {
  app.quit();
} else {
  app.on('second-instance', (_event, argv) => {
    showWindow();
    handleArgv(argv);
  });

  app.on('open-url', (event, url) => {
    event.preventDefault();
    handleArgv([url]);
  });

  app.whenReady().then(async () => {
    windowStateFile = path.join(app.getPath('userData'), 'window.json');
    settingsFile = path.join(app.getPath('userData'), 'settings.json');
    settings = { autostart: false, ...readJson(settingsFile, {}) };

    setupIpc();
    setupProtocolClient();
    setupUpdater();

    try {
      await startServer();
    } catch (err) {
      console.error('[wonder] server start failed', err.message);
      dialog.showErrorBox(
        'Wonder',
        `Не удалось запустить локальный сервер на ${ORIGIN}: ${err.message}`,
      );
      app.quit();
      return;
    }

    createWindow();
    createTray();
    registerMediaKeys();

    // локального интерфейса нет - сказали об этом один раз, окно уже онлайн
    if (!HAS_LOCAL_WEB) {
      console.warn('[wonder] resources/app/web отсутствует, работаем через', REMOTE_WEB);
      showBox({
        type: 'warning',
        title: 'Wonder',
        message: 'Файлы интерфейса не найдены',
        detail:
          'Похоже, установка обновилась не полностью. Сейчас открыта онлайн-версия Wonder. ' +
          'Чтобы вернуть локальную, закрой Wonder через трей, удали папку установки и запусти ' +
          'Wonder-Setup заново.',
        buttons: ['OK'],
      }).catch(() => {});
    }

    // автозапуск: синхронизируем реестр с настройкой
    if (process.platform === 'win32') applyLoginItem(settings.autostart);

    handleArgv(process.argv);

    setTimeout(() => checkUpdates(false), 12000);
    // и дальше раз в 4 часа, как фоновая проверка на телефоне
    setInterval(() => checkUpdates(false), 4 * 60 * 60 * 1000);

    app.on('activate', () => {
      if (!mainWindow || mainWindow.isDestroyed()) createWindow();
      else showWindow();
    });
  });

  app.on('before-quit', () => {
    isQuitting = true;
    if (tooltipTimer) {
      clearInterval(tooltipTimer);
      tooltipTimer = null;
    }
    saveWindowState();
    unregisterMediaKeys();
  });

  app.on('window-all-closed', () => {
    // приложение живёт в трее, окно закрывать не завершаем
    if (process.platform !== 'darwin' && isQuitting) app.quit();
  });
}
