package practica.gimnasioBackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import practica.gimnasioBackend.dto.UserResponse;
import practica.gimnasioBackend.dto.UserUpdateRequest;
import practica.gimnasioBackend.entity.Roles;
import practica.gimnasioBackend.entity.Users;
import practica.gimnasioBackend.repository.RoleRepository;
import practica.gimnasioBackend.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static practica.gimnasioBackend.controller.UserController.getStringResponseEntity;

@Service
public class UserServiceImpl implements UserServices {

    // (estaban ya en tu clase; los mantengo)
    private final UserRepository userRepo;        // no lo uso abajo, conservo para compatibilidad
    private final RoleRepository roleRepo;        // no lo uso abajo, conservo para compatibilidad

    private final UserRepository userRepository;  // este es el que estás usando en tu código
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;  // este es el que estás usando en tu código

    @Autowired
    public UserServiceImpl(
            UserRepository userRepo,
            RoleRepository roleRepo,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository
    ) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    /* ==========================
       REGISTRO / LOGIN (tuyo)
       ========================== */

    @Override
    public ResponseEntity<?> register(Users user) {
        Optional<Users> existingUser = userRepository.findByEmail(user.getEmail());
        ResponseEntity<String> body = getStringResponseEntity(existingUser);
        if (body != null) return body;

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = rolDefault(user);

        Users newUser = userRepository.save(user);
        return ResponseEntity.ok(newUser);
    }

    public Users rolDefault(Users user) {
        Roles defaultRole = roleRepository.findByName("USER");
        if (defaultRole == null) {
            throw new RuntimeException("Rol USER no existe en la base de datos");
        }
        user.getRoles().clear();
        user.getRoles().add(defaultRole);
        return userRepository.save(user);
    }

    @Override
    public Users authenticate(String email, String rawPassword) {
        Users u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(rawPassword, u.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        return u;
    }

    @Override
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean login(String email, String password) {
        Optional<Users> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;
        return passwordEncoder.matches(password, userOpt.get().getPassword());
    }

    @Override
    public UserResponse update(Long id, UserResponse req, String currentEmailOrNull) {
        return null;
    }

    /* =======================================================
       NUEVO: LISTAR (DTO), ACTUALIZAR y ELIMINAR PARA EL ADMIN
       ======================================================= */

    /** Lista de usuarios como DTO (sin password) */
    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Actualiza email, nombre y (si no es el propio usuario) el rol.
     * Usa UserUpdateRequest (id, email, name, role).
     * currentEmailOrNull llega de cabecera "X-User-Email" o de tu seguridad.
     */
    public UserResponse update(Long id, UserUpdateRequest req, String currentEmailOrNull) {
        // si te llega id en el body y quieres validar, puedes comprobarlo:
        // if (req.id() != null && !req.id().isBlank() && !id.toString().equals(req.id())) { ... }

        Users u = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        boolean isSelf = currentEmailOrNull != null
                && currentEmailOrNull.equalsIgnoreCase(u.getEmail());

        // email
        if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
            u.setEmail(req.getEmail().trim());
        }

        // nombre (si tu campo real es "nombre", usa u.setNombre(...))
        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            u.setAllName(req.getName().trim());
        }

        // rol (solo si NO es el propio usuario)
        if (req.getRole() != null && !isSelf) {
            String roleName = req.getRole().trim().toUpperCase();
            if (!roleName.equals("USER") && !roleName.equals("ADMIN")) {
                throw new IllegalArgumentException("Rol inválido. Usa USER o ADMIN.");
            }

            Roles role = roleRepo.findByName(roleName);
            if (role == null) {
                throw new NoSuchElementException("Rol no encontrado: " + roleName);
            }

        }


        Users saved = userRepository.save(u);
        return toResponse(saved);
    }

    /** Eliminar usuario (no puede eliminarse a sí mismo) */
    @Override
    public void deleteUser(Long id, String currentEmail) {
        Users u = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        if (currentEmail != null && currentEmail.equalsIgnoreCase(u.getEmail())) {
            throw new IllegalStateException("No puedes eliminar tu propia cuenta.");
        }
        userRepository.delete(u);
    }




    private UserResponse toResponse(Users u) {
        List<String> roles = (u.getRoles() == null || u.getRoles().isEmpty())
                ? List.of()
                : u.getRoles().stream().map(Roles::getName).toList();

        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getAllName(),
                roles
        );
    }

}
