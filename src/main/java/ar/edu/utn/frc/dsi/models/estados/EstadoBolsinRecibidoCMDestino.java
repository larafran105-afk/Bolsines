package ar.edu.utn.frc.dsi.models.estados;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.Bolsin;
import ar.edu.utn.frc.dsi.models.CambioEstadoBolsin;
import ar.edu.utn.frc.dsi.models.Empleado;
import jakarta.persistence.Entity;

@Entity
public class EstadoBolsinRecibidoCMDestino extends EstadoBolsin {

    public EstadoBolsinRecibidoCMDestino() {
        setNombre("Recibido en CM destino");
        setDescripcion("...");
    }

    @Override
    public void registrarRecepcion(Bolsin bolsin, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoBolsin> historialCambiosEstado) {
        throw new IllegalStateException("El bolsin ya se encuentra en estado recibido.");

    }

}
