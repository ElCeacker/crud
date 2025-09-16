import React, { useEffect, useMemo, useState } from "react";
import "./admin.css";

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [query, setQuery] = useState("");
  const [sortKey, setSortKey] = useState("email");
  const [sortDir, setSortDir] = useState("asc");
  const [page, setPage] = useState(1);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ id: null, email: "", name: "", role: "USER", isSelf: false });
  const [confirmDel, setConfirmDel] = useState(null);
  const pageSize = 10;

  const currentUser = (() => {
    try { return JSON.parse(localStorage.getItem("user") || "null"); }
    catch { return null; }
  })();
  const currentId = currentUser?.id ?? null;
  const currentEmail = currentUser?.correo || currentUser?.email || null;

  const authHeaders = () => {
    const token = localStorage.getItem("token") || "";
    const h = { "Content-Type": "application/json" };
    if (token) h.Authorization = `Bearer ${token}`;
    if (currentEmail) h["X-User-Email"] = currentEmail; 
    return h;
  };

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true);
        setError("");

        const res = await fetch("http://localhost:8081/api/users", {
          method: "GET",
          headers: authHeaders(),
        });

        if (!res.ok) {
          const text = await res.text().catch(() => "");
          throw new Error(`HTTP ${res.status} ${res.statusText} · ${text}`);
        }

        const data = await res.json().catch(() => []);
        setUsers(Array.isArray(data) ? data : data.content || []);
      } catch (e) {
        setError(e.message || "Error desconocido");
      } finally {
        setLoading(false);
      }
    };
    fetchUsers();
  }, []);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    const base = q
      ? users.filter((u) =>
          [u.name, u.email, (u.roles || []).join(", ")]
            .filter(Boolean)
            .some((v) => String(v).toLowerCase().includes(q))
        )
      : users;

    const sorted = [...base].sort((a, b) => {
      const va = String(a[sortKey] ?? "").toLowerCase();
      const vb = String(b[sortKey] ?? "").toLowerCase();
      if (va < vb) return sortDir === "asc" ? -1 : 1;
      if (va > vb) return sortDir === "asc" ? 1 : -1;
      return 0;
    });
    return sorted;
  }, [users, query, sortKey, sortDir]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const currentPage = Math.min(page, totalPages);
  const start = (currentPage - 1) * pageSize;
  const pageData = filtered.slice(start, start + pageSize);

  const toggleSort = (key) => {
    if (key === sortKey) setSortDir((d) => (d === "asc" ? "desc" : "asc"));
    else { setSortKey(key); setSortDir("asc"); }
  };

  const openEdit = (u) => {
    const firstRole = (u.roles && u.roles[0]) || "USER";
    const isSelf = (currentId && u.id === currentId) || (currentEmail && u.email === currentEmail);
    setEditing(u.id);
    setForm({
      id: u.id,
      email: u.email || "",
      name: u.name || "",
      role: firstRole,
      isSelf,
    });
  };

  const saveEdit = async () => {
    try {
      const original = users.find(u => u.id === form.id);
      const roleToSend = form.isSelf ? (original?.roles?.[0] || "USER") : form.role;

      const body = {
        email: (form.email || "").trim(),
        name: (form.name || "").trim(),
        roles: [roleToSend],
      };

      const res = await fetch(`http://localhost:8081/api/users/${form.id}`, {
        method: "PUT",
        headers: authHeaders(),
        body: JSON.stringify(body),
      });

      if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText} · ${text}`);
      }

      const updated = await res.json();
      setUsers((prev) => prev.map((u) => (u.id === updated.id ? updated : u)));
      setEditing(null);
    } catch (e) {
      alert(e.message || "Error al guardar");
    }
  };

  const deleteUser = async (id) => {
    const u = users.find(x => x.id === id);

    const isSelf =
      (currentId && id === currentId) ||
      (currentEmail && u?.email === currentEmail);

    if (isSelf) {
      alert("No puedes eliminar tu propia cuenta.");
      setConfirmDel(null);
      return;
    }

    try {
      const res = await fetch(`http://localhost:8081/api/users/${id}`, {
        method: "DELETE",
        headers: authHeaders(), // envía X-User-Email al backend
      });

      if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText} · ${text}`);
      }

      setUsers(prev => prev.filter(u => u.id !== id));
      setConfirmDel(null);
    } catch (e) {
      alert(e.message || "Error al eliminar");
    }
  };

  return (
    <div className="admin-container">
      <div className="admin-wrapper">
        <div className="admin-header">
          <h1 className="admin-title">Panel de Usuarios de GabriGym</h1>
          <div className="admin-search">
            <input
              value={query}
              onChange={(e) => { setQuery(e.target.value); setPage(1); }}
              placeholder="Buscar por nombre, email o rol"
              className="admin-search-input"
            />
          </div>
        </div>

        <div className="admin-table-container">
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead className="admin-thead">
                <tr>
                  <th className="admin-th" onClick={() => toggleSort("id")}>ID</th>
                  <th className="admin-th" onClick={() => toggleSort("email")}>
                    Email {sortKey === "email" && (sortDir === "asc" ? "▲" : "▼")}
                  </th>
                  <th className="admin-th">Nombre</th>
                  <th className="admin-th">Roles</th>
                  <th className="admin-th">Estado</th>
                  <th className="admin-th">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {loading && (<tr><td colSpan={6} className="admin-loading">Cargando...</td></tr>)}
                {!loading && error && (<tr><td colSpan={6} className="admin-error">{error}</td></tr>)}
                {!loading && !error && pageData.length === 0 && (<tr><td colSpan={6} className="admin-empty">Sin resultados</td></tr>)}

                {!loading && !error && pageData.map((u) => {
                  const roleList = (u.roles || []);
                  const isSelf =
                    (currentId && u.id === currentId) ||
                    (currentEmail && u.email === currentEmail);

                  return (
                    <tr key={u.id} className="admin-row">
                      <td className="admin-cell-id">{u.id ?? "—"}</td>
                      <td className="admin-cell-email">{u.email}</td>
                      <td className="admin-cell-name">{u.name || "—"}</td>
                      <td className="admin-cell-roles">
                        <div className="admin-roles">
                          {roleList.length === 0
                            ? <span className="admin-role-empty">SIN ROL</span>
                            : roleList.map((r, i) => <span key={i} className="admin-role">{r}</span>)
                          }
                        </div>
                      </td>
                      <td className="admin-cell-status">
                        <span className="admin-status">Activo</span>
                      </td>
                      <td className="admin-cell-actions">
                        <button
                          className="admin-action"
                          onClick={() => openEdit(u)}
                          disabled={isSelf}
                          title={isSelf ? "No puedes editarte" : ""}
                        >
                          Editar
                        </button>
                        <button
                          className="admin-action danger"
                          onClick={() => setConfirmDel(u)}
                          disabled={isSelf}
                          title={isSelf ? "No puedes eliminarte" : ""}
                        >
                          Eliminar
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="admin-pagination">
            <div className="admin-pagination-info">
              Página {currentPage} de {totalPages} · {filtered.length} usuarios
            </div>
            <div className="admin-pagination-buttons">
              <button onClick={() => setPage((p) => Math.max(1, p - 1))} disabled={currentPage === 1} className="admin-button">Anterior</button>
              <button onClick={() => setPage((p) => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages} className="admin-button">Siguiente</button>
            </div>
          </div>
        </div>
      </div>

      {editing !== null && (
        <div className="admin-modal">
          <div className="admin-modal-card">
            <h2 className="admin-modal-title">Editar usuario</h2>
            <div className="admin-form-grid">
              <label className="admin-label">Email</label>
              <input
                className="admin-input"
                value={form.email}
                onChange={(e)=>setForm(f=>({...f,email:e.target.value}))}
              />

              <label className="admin-label">Nombre</label>
              <input
                className="admin-input"
                value={form.name}
                onChange={(e)=>setForm(f=>({...f,name:e.target.value}))}
              />

              <label className="admin-label">Rol</label>
              <select
                className="admin-input"
                value={form.role}
                onChange={(e)=>setForm(f=>({...f,role:e.target.value}))}
                disabled={form.isSelf}
                title={form.isSelf ? "No puedes cambiar tu propio rol" : ""}
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
            <div className="admin-modal-actions">
              <button className="admin-button" onClick={()=>setEditing(null)}>Cancelar</button>
              <button className="admin-button primary" onClick={saveEdit}>Guardar</button>
            </div>
          </div>
        </div>
      )}

      {confirmDel && (
        <div className="admin-modal">
          <div className="admin-modal-card">
            <h2 className="admin-modal-title">Eliminar usuario</h2>
            <p className="admin-modal-text">¿Seguro que quieres eliminar a <strong>{confirmDel.email}</strong>?</p>
            <div className="admin-modal-actions">
              <button className="admin-button" onClick={()=>setConfirmDel(null)}>Cancelar</button>
              <button className="admin-button danger" onClick={()=>deleteUser(confirmDel.id)}>Eliminar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
