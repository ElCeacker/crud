package practica.tiendaBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import practica.tiendaBackend.entity.Users;
import practica.tiendaBackend.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import practica.tiendaBackend.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    private final UserService UserService;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserService UserService, UserRepository userRepository) {
        this.UserService = UserService;
        this.userRepository = userRepository;
    }

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Users user) {

        Optional<Users> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            // El correo ya existe en la base de datos
            return ResponseEntity
                    .badRequest()
                    .body("El correo ya está en uso");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Guardar nuevo usuario
        Users newUser = userRepository.save(user);

        return ResponseEntity.ok(newUser);
    }

    // Listar usuarios
    @GetMapping
    public List<Users> list() {
        return userRepository.findAll();
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users user) {
        try {
            boolean valid = UserService.login(user.getEmail(), user.getPassword());

            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("correo", user.getEmail());
            response.put("mensaje", "Login exitoso");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // 👈 Para ver en consola el error exacto
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error en Login: " + e.getMessage()));
        }
    }

}
