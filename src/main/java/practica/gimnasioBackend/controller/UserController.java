package practica.gimnasioBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import practica.gimnasioBackend.dto.Login;
import practica.gimnasioBackend.dto.LoginResponse;
import practica.gimnasioBackend.dto.UserResponse;
import practica.gimnasioBackend.entity.Roles;
import practica.gimnasioBackend.entity.Users;
import practica.gimnasioBackend.service.UserServices;

import java.util.HashMap;
import java.util.List;
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
    public ResponseEntity<LoginResponse> login(@RequestBody Login req) {
        try {
            Users user = UserServices.authenticate(req.getEmail(), req.getPassword());

            List<String> roleNames = user.getRoles()
                    .stream()
                    .map(Roles::getName)     // "ADMIN", "USER"
                    .toList();

            LoginResponse resp = new LoginResponse(
                    user.getEmail(),
                    roleNames,
                    "Login exitoso"
            );

            return ResponseEntity.ok(resp);

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, List.of(), "Credenciales inválidas"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(null, List.of(), "Error en Login: " + e.getMessage()));
        }
    }
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> out = UserServices.getAllUsers().stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getRoles().stream().map(Roles::getName).toList()
                ))
                .toList();
        return ResponseEntity.ok(out);
    }



}
