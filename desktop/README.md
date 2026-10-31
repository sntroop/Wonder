# Wonder Desktop (Windows)

Electron wrapper around the Wonder web client. It serves the built web app
from a local HTTP server and proxies API calls to a Wonder backend.

This is a **client-only snapshot**. The backend, accounts, recommendation
engine and service integrations are not included.

## How it works

```
BrowserWindow
  └─ http://127.0.0.1:45871/wonder/     (local static server)
       ├─ /wonder/api/*  ── proxy ──▶ WONDER_BACKEND
       ├─ /wonder/own/*  ── proxy ──▶ WONDER_BACKEND
       └─ /wonder/apk/*  ── proxy ──▶ WONDER_BACKEND
```

- Window state, tray, autostart, global media keys, deep links (`wonder://`)
  and single-instance lock are in `electron/main.js`.
- The proxy rewrites `/wonder/api/...` to `/api/...`, so `WONDER_BACKEND`
  should point at the music backend root, not at a `/wonder` page.
- Default backend: `http://127.0.0.1:8400`.
- OTA updates are disabled in this snapshot: no update feed URL is shipped.

## Build

1. Build the web client:

```bash
cd ../web
npm ci
npm run build
rm -rf ../desktop/web
cp -r dist ../desktop/web
```

2. Install Electron dependencies and run:

```bash
npm ci
npm start
```

3. Windows installer (run on Windows, or use `makensis` from electron-builder
   on Linux):

```bash
npm run dist:win
```

Artifacts are written to `release/` (ignored by git). The NSIS script in
`installer.nsi` installs per-user without UAC and creates Start Menu and
desktop shortcuts.

## Backend

Start any compatible Wonder backend and point the app at it:

```bash
WONDER_BACKEND=http://127.0.0.1:8400 npm start
```

Without a backend the UI loads, but Home/Wave/Shorts/Search show empty states
and playback has nothing to stream. No data is sent anywhere else.

## What is not here

- server addresses, API keys, account tokens;
- OTA feed and `app-update.yml`;
- built installers and `web/` bundle (build them from source);
- backend code, database, cache.
