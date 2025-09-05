package practica.gimnasioBackend.service;

import org.springframework.http.ResponseEntity;
import practica.gimnasioBackend.entity.Users;

public interface UserServices {
    ResponseEntity<?> register(Users user);

    Users authenticate(String email, String rawPassword);

    boolean login(String email, String password);
}
