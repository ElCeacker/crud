package practica.gimnasioBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import practica.gimnasioBackend.entity.Users;
import practica.gimnasioBackend.service.UserServices;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    private final UserServices UserServices;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserController( UserServices userServices) {
        UserServices = userServices;
    }



    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Users user) {
        return UserServices.register(user);
    }

    public static ResponseEntity<String> getStringResponseEntity(Optional<Users> existingUser) {
        if (existingUser.isPresent()) {
            // El correo ya existe en la base de datos
            return ResponseEntity
                    .badRequest()
                    .body("El correo ya está en uso");
        }
        return null;
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users user) {
        try {
            boolean valid = UserServices.login(user.getEmail(), user.getPassword());

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
