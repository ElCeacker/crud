package practica.gimnasioBackend.service;

import org.springframework.http.ResponseEntity;
import practica.gimnasioBackend.entity.Users;

import java.util.List;

public interface UserServices {
    ResponseEntity<?> register(Users user);

    Users authenticate(String email, String rawPassword);

    List<Users> getAllUsers();

    boolean login(String email, String password);
}
