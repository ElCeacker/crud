package practica.gimnasioBackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import practica.gimnasioBackend.entity.Rol;
import practica.gimnasioBackend.entity.Users;
import practica.gimnasioBackend.repository.RoleRepository;
import practica.gimnasioBackend.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    public Users register(Users user) {
        // Encriptar la contraseña antes de guardarla
        String encriptPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encriptPassword);



        return userRepository.save(user);
    }


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
