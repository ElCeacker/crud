package practica.gimnasioBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practica.gimnasioBackend.entity.Role;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRol(String rol);
}
