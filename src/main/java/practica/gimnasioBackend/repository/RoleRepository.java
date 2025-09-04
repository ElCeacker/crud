package practica.gimnasioBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practica.gimnasioBackend.entity.Rol;

public interface RoleRepository extends JpaRepository<Rol, Long> {
    Rol findByName(String name);
}
