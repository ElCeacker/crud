package practica.tiendaBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import practica.tiendaBackend.entity.Usuarios;
import practica.tiendaBackend.repository.UsuarioRepositorio;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import practica.tiendaBackend.service.ServicioUsuario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioControlador {
    private final ServicioUsuario servicioUsuario;

    private final UsuarioRepositorio usuarioRepositorio;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioControlador(ServicioUsuario servicioUsuario, UsuarioRepositorio usuarioRepositorio) {
        this.servicioUsuario = servicioUsuario;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Autowired
    private ServicioUsuario usuarioService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Usuarios usuario) {

        Optional<Usuarios> usuarioExistente = usuarioRepositorio.findByCorreo(usuario.getCorreo());

        if (usuarioExistente.isPresent()) {
            // El correo ya existe en la base de datos
            return ResponseEntity
                    .badRequest()
                    .body("El correo ya está en uso");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Guardar nuevo usuario
        Usuarios nuevoUsuario = usuarioRepositorio.save(usuario);

        return ResponseEntity.ok(nuevoUsuario);
    }

    // Listar usuarios
    @GetMapping
    public List<Usuarios> listar() {
        return usuarioRepositorio.findAll();
    }

    // Login
    @PostMapping("/logear")
    public ResponseEntity<?> login(@RequestBody Usuarios usuario) {
        try {
            boolean valido = servicioUsuario.login(usuario.getCorreo(), usuario.getPassword());

            if (!valido) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("correo", usuario.getCorreo());
            response.put("mensaje", "Login exitoso");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // 👈 Para ver en consola el error exacto
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error en Login: " + e.getMessage()));
        }
    }

}
