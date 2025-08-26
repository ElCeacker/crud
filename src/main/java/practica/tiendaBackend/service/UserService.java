package practica.tiendaBackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import practica.tiendaBackend.entity.Users;
import practica.tiendaBackend.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Users registrarUsuario(Users user) {
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
