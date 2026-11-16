package ar.edu.utn.frc.dsi.models.estados.doc;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.CambioEstadoDoc;
import ar.edu.utn.frc.dsi.models.Documentacion;
import ar.edu.utn.frc.dsi.models.Empleado;
import jakarta.persistence.Entity;

@Entity
public class DocEnBolsinEnviado extends EstadoDoc {

    @Override
    public void aceptarDocumentacion(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoDoc> cambioEstado) {

        this.cambiarEstado(fechaActual, cambioEstado);

        EstadoDoc estado = this.crearEstado();

        CambioEstadoDoc nuevoCambioEstado = this.creaCambioEstado(fechaActual, estado, empleadoLogueado,
                documentacion);
        // Añadir en el diagrama de secuencia que se pasa documentacion como parametro y
        // tambien empleado

        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(estado);

    }

    private void cambiarEstado(LocalDateTime fechaActual, List<CambioEstadoDoc> cambioEstado) {
        for (CambioEstadoDoc ced : cambioEstado) {
            if (ced.sosActual()) {
                ced.setFechaHoraFin(fechaActual);
                break;
            }
        }
    }

    private EstadoDoc crearEstado() {
        return new DocRecibidoYAceptado();
    }

    private CambioEstadoDoc creaCambioEstado(LocalDateTime fechaActual, EstadoDoc estado,
            Empleado empleadoLogueado, Documentacion documentacion) {
        return new CambioEstadoDoc(fechaActual, estado, empleadoLogueado, documentacion);
    }

}
