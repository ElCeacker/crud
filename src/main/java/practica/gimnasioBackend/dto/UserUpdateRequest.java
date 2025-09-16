package practica.gimnasioBackend.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String id;     // opcional si lo recibes en body
    private String email;
    private String name;   // o "nombre"
    private String role;   // "USER" o "ADMIN"

    public UserUpdateRequest() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
