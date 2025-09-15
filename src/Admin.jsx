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
  const pageSize = 10;

  useEffect(() => {
  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError("");

      const token = localStorage.getItem("token") || "";
      const headers = { "Content-Type": "application/json" };
      if (token) headers.Authorization = `Bearer ${token}`;

      const res = await fetch("http://localhost:8081/api/users", {
        method: "GET",
        headers,
      });

      if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText} · ${text}`);
      }

      const data = await res.json().catch(() => []);
      setUsers(Array.isArray(data) ? data : data.content || []);
    } catch (e) {
      console.error(e);
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
          [u.nombre, u.name, u.allName, u.email, (u.roles || []).map((r) => (typeof r === "string" ? r : r.name || r.role || r.authority)).join(", ")]
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
    else {
      setSortKey(key);
      setSortDir("asc");
    }
  };

  return (
    <div className="admin-container">
      <div className="admin-wrapper">
        <div className="admin-header">
          <h1 className="admin-title">Panel de Usuarios</h1>
          <div className="admin-search">
            <input
              value={query}
              onChange={(e) => {
                setQuery(e.target.value);
                setPage(1);
              }}
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
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr>
                    <td colSpan={5} className="admin-loading">Cargando...</td>
                  </tr>
                )}
                {!loading && error && (
                  <tr>
                    <td colSpan={5} className="admin-error">{error}</td>
                  </tr>
                )}
                {!loading && !error && pageData.length === 0 && (
                  <tr>
                    <td colSpan={5} className="admin-empty">Sin resultados</td>
                  </tr>
                )}
                {!loading && !error &&
                  pageData.map((u) => {
                    const displayName = u.nombre || u.name || u.allName || "—";
                    const roleList = (u.roles || [])
                      .map((r) => (typeof r === "string" ? r : r.name || r.role || r.authority))
                      .filter(Boolean);
                    return (
                      <tr key={u.id} className="admin-row">
                        <td className="admin-cell-id">{u.id ?? "—"}</td>
                        <td className="admin-cell-email">{u.email}</td>
                        <td className="admin-cell-name">{displayName}</td>
                        <td className="admin-cell-roles">
                          <div className="admin-roles">
                            {roleList.length === 0 ? (
                              <span className="admin-role-empty">SIN ROL</span>
                            ) : (
                              roleList.map((r, i) => (
                                <span key={i} className="admin-role">{r}</span>
                              ))
                            )}
                          </div>
                        </td>
                        <td className="admin-cell-status">
                          <span className="admin-status">Activo</span>
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
              <button
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                className="admin-button"
              >
                Anterior
              </button>
              <button
                onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                className="admin-button"
              >
                Siguiente
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
