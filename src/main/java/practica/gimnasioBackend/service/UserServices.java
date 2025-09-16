package practica.gimnasioBackend.service;

import org.springframework.http.ResponseEntity;
import practica.gimnasioBackend.dto.UserResponse;
import practica.gimnasioBackend.dto.UserUpdateRequest;
import practica.gimnasioBackend.entity.Users;

import java.util.List;

public interface UserServices {
    ResponseEntity<?> register(Users user);

    Users authenticate(String email, String rawPassword);

    List<UserResponse> getAll();

    UserResponse update(Long id, UserUpdateRequest req, String currentEmailOrNull);

    void deleteUser(Long id, String currentEmail);
}
