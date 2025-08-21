package practica.tiendaBackend.repository;

import org.springframework.stereotype.Repository;
import practica.tiendaBackend.entity.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuarios, Long> {
    Optional<Usuarios> findByCorreo(String correo);
}
