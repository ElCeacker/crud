package practica.gimnasioBackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import practica.gimnasioBackend.entity.Roles;
import practica.gimnasioBackend.entity.Users;
import practica.gimnasioBackend.repository.RoleRepository;
import practica.gimnasioBackend.repository.UserRepository;

import java.util.Optional;

import static practica.gimnasioBackend.controller.UserController.getStringResponseEntity;

@Service
public class UserServiceImpl implements UserServices {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
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

        // Guardar nuevo usuario
        Users newUser = userRepository.save(user);
        return ResponseEntity.ok(newUser);
    }

    public Users rolDefault(Users user) {

        Roles defaultRole = roleRepository.findByName("USER");
        if (defaultRole == null) {
            throw new RuntimeException("Rol USER no existe en la base de datos");
        }

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
        // En este punto `u.getRoles()` ya viene cargado por @EntityGraph
        return u;
    }

@Override
public boolean login(String email, String password) {
        // Buscar usuario por correo
        Optional<Users> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return false; // No existe el correo
        }

        Users user = userOpt.get();

        // Comparar la contraseña en texto plano con la encriptada
        return passwordEncoder.matches(password, user.getPassword());
    }
}
