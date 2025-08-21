package practica.tiendaBackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import practica.tiendaBackend.entity.Usuarios;
import practica.tiendaBackend.repository.UsuarioRepositorio;

import java.util.Optional;

@Service
public class ServicioUsuario {

    @Autowired
    private UsuarioRepositorio usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuarios registrarUsuario(Usuarios usuario) {
        // Encriptar la contraseña antes de guardarla
        String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptada);

        return usuarioRepository.save(usuario);
    }


    public boolean login(String correo, String password) {
        // Buscar usuario por correo
        Optional<Usuarios> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isEmpty()) {
            return false; // No existe el correo
        }

        Usuarios usuario = usuarioOpt.get();

        // Comparar la contraseña en texto plano con la encriptada
        return passwordEncoder.matches(password, usuario.getPassword());
    }
}
