import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Приложение живёт по пути /wonder/.
// API — same-origin /wonder/api (в desktop-обёртке проксируется на WONDER_BACKEND),
// поэтому в браузере нет CORS и mixed content.
export default defineConfig({
  base: "/wonder/",
  define: {
    __BUILD__: JSON.stringify(new Date().toISOString().slice(0, 16).replace("T", " ")),
  },
  plugins: [react()],
  server: {
    port: 5199,
    host: "0.0.0.0",
    proxy: {
      "/wonder/api": {
        target: "http://127.0.0.1:8400",
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/wonder\/api/, "/api"),
      },
    },
  },
  build: {
    outDir: "dist",
    sourcemap: false,
    target: "es2020",
  },
});
