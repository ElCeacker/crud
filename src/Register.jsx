import React, { useState, useId } from "react";
import "./login.css";
import { Link } from "react-router-dom";

export default function Register({ onRegister }) {
  const nameId = useId();
  const emailId = useId();
  const passId = useId();
  const confirmId = useId();

  const [form, setForm] = useState({ name: "", email: "", password: "", confirm: "" });
  const [showPass, setShowPass] = useState(false);
  const [errors, setErrors] = useState({});
  const [status, setStatus] = useState("idle");
  const [serverError, setServerError] = useState("");

  const validate = (next = form) => {
    const e = {};
    if (!next.name.trim()) e.name = "El nombre es obligatorio.";
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
    if (next.password !== next.confirm) {
      e.confirm = "Las contraseñas no coinciden.";
    }
    return e;
  };

  const handleChange = (ev) => {
    const { name, value } = ev.target;
    const next = { ...form, [name]: value };
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
      const res = await fetch("http://localhost:8080/api/usuarios/registrar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
           nombreCompleto: form.name, correo: form.email, password: form.password,
        }),
        
      });
      
      if (!res.ok) throw new Error("Este correo ya está en uso.");
      const data = await res.json();

      setStatus("success");
      onRegister?.(data);
    } catch (err) {
      setServerError(err.message);
      setStatus("error");
    } finally {
      setTimeout(() => setStatus((s) => (s === "success" ? "success" : "idle")), 800);
    }
  }


  return (
    <div className="register-page">
      <div className="register-card">
        <div className="brand">
          <div className="logo" aria-hidden="true">📝</div>
          <h1 className="title">Crear cuenta</h1>
          <p className="subtitle">Únete a nuestra comunidad</p>
        </div>

        <form className="form" onSubmit={handleSubmit} noValidate>
          <div className={`field ${errors.name ? "has-error" : ""}`}>
            <label htmlFor={nameId}>Nombre completo</label>
            <input
              id={nameId}
              type="text"
              name="name"
              placeholder="Tu nombre"
              value={form.name}
              onChange={handleChange}
              disabled={status === "loading"}
            />
            {errors.name && <span className="error">{errors.name}</span>}
          </div>

          <div className={`field ${errors.email ? "has-error" : ""}`}>
            <label htmlFor={emailId}>Correo electrónico</label>
            <input
              id={emailId}
              type="email"
              name="email"
              placeholder="tú@correo.com"
              value={form.email}
              onChange={handleChange}
              disabled={status === "loading"}
            />
            {errors.email && <span className="error">{errors.email}</span>}
          </div>

          <div className={`field ${errors.password ? "has-error" : ""}`}>
            <label htmlFor={passId}>Contraseña</label>
            <div className="password-wrapper">
              <input
                id={passId}
                type={showPass ? "text" : "password"}
                name="password"
                placeholder="Contraseña"
                value={form.password}
                onChange={handleChange}
                disabled={status === "loading"}
              />
              <button
                type="button"
                className="ghost-btn"
                onClick={() => setShowPass((v) => !v)}
                aria-label={showPass ? "Ocultar contraseña" : "Mostrar contraseña"}
                disabled={status === "loading"}
              >
                {showPass ? "🙈" : "👁️"}
              </button>
            </div>
            {errors.password && <span className="error">{errors.password}</span>}
          </div>

          <div className={`field ${errors.confirm ? "has-error" : ""}`}>
            <label htmlFor={confirmId}>Confirmar contraseña</label>
            <input
              id={confirmId}
              type="password"
              name="confirm"
              placeholder="Repite tu contraseña"
              value={form.confirm}
              onChange={handleChange}
              disabled={status === "loading"}
            />
            {errors.confirm && <span className="error">{errors.confirm}</span>}
          </div>

          <button
            className="primary-btn"
            type="submit"
            disabled={status === "loading"}
          >
            {status === "loading" ? "Registrando..." : "Registrarse"}
          </button>

          {serverError && <div className="server-error">{serverError}</div>}
          {status === "success" && <div className="success">¡Cuenta creada con éxito!</div>}
        </form>

        <p className="meta">
            ¿Ya tienes cuenta? <Link className="link" to="/">Inicia sesión</Link>
        </p>
      </div>
    </div>
  );
}
