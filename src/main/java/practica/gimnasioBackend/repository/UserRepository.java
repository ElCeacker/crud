package practica.gimnasioBackend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import practica.gimnasioBackend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    @EntityGraph(attributePaths = "roles") //trae el rol en la misma consulta
    Optional<Users> findByEmail(String email);
}
