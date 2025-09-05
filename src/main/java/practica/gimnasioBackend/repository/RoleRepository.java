package practica.gimnasioBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practica.gimnasioBackend.entity.Roles;

public interface RoleRepository extends JpaRepository<Roles, Long> {
    Roles findByName(String name);
}
