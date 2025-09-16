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

import static practica.gimnasioBackend.controller.UserController.getStringResponseEntity;

@Service
public class UserServiceImpl implements UserServices {

    private final RoleRepository roleRepo;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    @Autowired
    public UserServiceImpl(
            UserRepository userRepo,
            RoleRepository roleRepo,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository
    ) {
        this.roleRepo = roleRepo;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

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

    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse update(Long id, UserUpdateRequest req, String currentEmailOrNull) {

        Users u = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        boolean isSelf = currentEmailOrNull != null
                && currentEmailOrNull.equalsIgnoreCase(u.getEmail());

        if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
            u.setEmail(req.getEmail().trim());
        }

        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            u.setAllName(req.getName().trim());
        }

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
