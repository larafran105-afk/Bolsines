package ar.edu.utn.frc.dsi.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.dsi.models.Sesion;

public interface SesionRepository extends JpaRepository<Sesion, Long> {
    Optional<Sesion> findFirstByFechaHoraFinIsNullOrderByFechaHoraInicioDesc();
    
}
