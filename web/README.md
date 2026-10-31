# Wonder Web

The React/TypeScript client that powers the Wonder web app and the Windows
desktop build. It is the same UI as the Android client, adapted for mouse and
keyboard.

This is a **client-only snapshot**. The backend, recommendation engine, account
service and VK/SoundCloud integrations are not part of this repository.

## Stack

- React 18 + TypeScript + Vite
- Zustand for player/auth state
- Plain CSS with a small design-token layer
- No UI framework, no analytics, no external CDN

## Run

```bash
npm ci
npm run dev      # http://127.0.0.1:5173/wonder/
npm run build    # static build in dist/
```

The client calls the API at the same origin under `/wonder/api`. Without a
backend those screens show an empty state. The Electron wrapper in
`../desktop` proxies `/wonder/api`, `/wonder/own` and `/wonder/apk` to a
backend configured with `WONDER_BACKEND` (default `http://127.0.0.1:8400`).

## What is inside

| Area | Files |
|---|---|
| Shell, routing, auth gate | `src/App.tsx`, `src/main.tsx`, `src/store.ts` |
| Player, queue, sheet, hotkeys | `src/store.ts`, `src/components/PlayerSheet.tsx` |
| Home, wave, shorts, search | `src/pages/*` |
| Library, playlists, artists, profile | `src/pages/*`, `src/components/*` |
| Audio FX and appearance | `src/audioFx.ts`, `src/skin.ts` |
| API contract | `src/api.ts` (paths only, no server address) |

## Build for the desktop wrapper

```bash
npm ci && npm run build
rm -rf ../desktop/web
cp -r dist ../desktop/web
```

Then follow `../desktop/README.md`.
