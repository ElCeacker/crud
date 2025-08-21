import React, { useState, useId } from "react";
import "./login.css";
import { Link } from "react-router-dom";

export default function Login({ onLogin }) {
  const emailId = useId();
  const passId = useId();
  const [form, setForm] = useState({ email: "", password: "", remember: false });
  const [showPass, setShowPass] = useState(false);
  const [errors, setErrors] = useState({});
  const [status, setStatus] = useState("idle");
  const [serverError, setServerError] = useState("");

  const validate = (next = form) => {
    const e = {};
    if (!next.email.trim()) {
      e.email = "El correo es obligatorio.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(next.email)) {
      e.email = "Formato de correo no válido.";
    }
    if (!next.password) {
      e.password = "La contraseña es obligatoria.";
    } else if (next.password.length < 6) {
      e.password = "Mínimo 6 caracteres.";
    }
    return e;
  };

  const handleChange = (ev) => {
    const { name, value, type, checked } = ev.target;
    const next = { ...form, [name]: type === "checkbox" ? checked : value };
    setForm(next);
    setErrors(validate(next));
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    const e = validate();
    setErrors(e);
    setServerError("");
    if (Object.keys(e).length) return;

    try {
        setStatus("loading");
        const res = await fetch("http://localhost:8081/api/usuarios/logear", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ correo: form.email, password: form.password }),
    });

    if (!res.ok) throw new Error("Credenciales incorrectas");
    const data = await res.json();

    // Guardar token
    localStorage.setItem("token", data.token);

    setStatus("success");
        onLogin?.({ email: data.email, token: data.token, remember: form.remember });
    } catch (err) {
    setServerError(err.message);
    setStatus("error");
    }
}


  const isDisabled = status === "loading";

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="brand">
          <div className="logo" aria-hidden="true">🔐</div>
          <h1 className="title">Inicia sesión</h1>
          <p className="subtitle">Bienvenido</p>
        </div>

        <form className="form" onSubmit={handleSubmit} noValidate>
          <div className={`field ${errors.email ? "has-error" : ""}`}>
            <label htmlFor={emailId}>Correo electrónico</label>
            <input
              id={emailId}
              type="email"
              name="email"
              placeholder="tú@correo.com"
              value={form.email}
              onChange={handleChange}
              aria-invalid={!!errors.email}
              aria-describedby={errors.email ? `${emailId}-error` : undefined}
              disabled={isDisabled}
              autoComplete="email"
            />
            {errors.email && (
              <span className="error" id={`${emailId}-error`} role="alert">
                {errors.email}
              </span>
            )}
          </div>

          <div className={`field ${errors.password ? "has-error" : ""}`}>
            <label htmlFor={passId}>Contraseña</label>
            <div className="password-wrapper">
              <input
                id={passId}
                type={showPass ? "text" : "password"}
                name="password"
                placeholder="Tu contraseña"
                value={form.password}
                onChange={handleChange}
                aria-invalid={!!errors.password}
                aria-describedby={errors.password ? `${passId}-error` : undefined}
                disabled={isDisabled}
                autoComplete="current-password"
              />
              <button
                type="button"
                className="ghost-btn"
                onClick={() => setShowPass((v) => !v)}
                aria-label={showPass ? "Ocultar contraseña" : "Mostrar contraseña"}
                disabled={isDisabled}
              >
                {showPass ? "🙈" : "👁️"}
              </button>
            </div>
            {errors.password && (
              <span className="error" id={`${passId}-error`} role="alert">
                {errors.password}
              </span>
            )}
          </div>

          <button
            className="primary-btn"
            type="submit"
            disabled={isDisabled}
            aria-busy={status === "loading"}
          >
            {status === "loading" ? "Accediendo..." : "Entrar"}
          </button>

          {serverError && (
            <div className="server-error" role="alert" aria-live="polite">
              {serverError}
            </div>
          )}
          {status === "success" && (
            <div className="success" role="status" aria-live="polite">
              ¡Sesión iniciada!
            </div>
          )}
        </form>

        <p className="meta">
          ¿No tienes cuenta? <Link className="link" to="/register">Regístrate</Link>
        </p>
      </div>
    </div>
  );
}
