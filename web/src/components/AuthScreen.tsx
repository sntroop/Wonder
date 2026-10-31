import { useState } from "react";
import { useAuth } from "../store";

export function AuthScreen() {
  const [mode, setMode] = useState<"login" | "register">("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const { login, register, guest, busy, error, clearError } = useAuth();

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password) return;
    try {
      if (mode === "login") await login(username, password);
      else await register(username, password, name || username);
    } catch {
      /* ошибка уже в store */
    }
  };

  return (
    <div className="auth">
      <div className="auth-card">
        <div className="wordmark" style={{ justifyContent: "center", paddingBottom: 12 }}>
          <span className="w">W</span> Wonder
        </div>
        <p className="lead">
          {mode === "login" ? "Вход в аккаунт" : "Новый аккаунт"}
        </p>

        <form onSubmit={submit}>
          <div className="field">
            <label>@юзернейм</label>
            <input
              value={username}
              onChange={(e) => {
                setUsername(e.target.value);
                clearError();
              }}
              placeholder="trooper"
              autoComplete="username"
              autoFocus
            />
          </div>

          {mode === "register" ? (
            <div className="field">
              <label>Имя (видно другим)</label>
              <input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Как тебя звать"
                autoComplete="nickname"
              />
            </div>
          ) : null}

          <div className="field">
            <label>Пароль</label>
            <input
              type="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                clearError();
              }}
              placeholder="••••••••"
              autoComplete={mode === "login" ? "current-password" : "new-password"}
            />
          </div>

          {error ? <div className="auth-error">{error}</div> : null}

          <div className="auth-actions">
            <button className="btn btn-primary" type="submit" disabled={busy}>
              {busy ? "Подожди…" : mode === "login" ? "Войти" : "Создать аккаунт"}
            </button>
            <button
              className="btn btn-ghost"
              type="button"
              onClick={() => void guest().catch(() => undefined)}
              disabled={busy}
            >
              Войти гостем
            </button>
          </div>
        </form>

        <div className="auth-switch">
          {mode === "login" ? (
            <>
              Нет аккаунта?{" "}
              <button onClick={() => setMode("register")}>Создать</button>
            </>
          ) : (
            <>
              Уже есть аккаунт?{" "}
              <button onClick={() => setMode("login")}>Войти</button>
            </>
          )}
        </div>

        <a className="auth-win-link" href="/wonder/desktop">
          Wonder для Windows — скачать приложение
        </a>
      </div>
    </div>
  );
}
