package ar.edu.utn.frc.dsi.models.estados.doc;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.CambioEstadoDoc;
import ar.edu.utn.frc.dsi.models.Documentacion;
import ar.edu.utn.frc.dsi.models.Empleado;
import jakarta.persistence.Entity;

// Estado concreto con tres transiciones de salida en la máquina de estados de
// Documentación. Cada transición sigue los mismos pasos (cerrar el cambio de
// estado actual, crear el estado siguiente, crear el nuevo cambio de estado),
// lo único que cambia es el estado destino.
@Entity
public class DocEnBolsinEnviado extends EstadoDoc {

    public DocEnBolsinEnviado() {
        setNombre("En Bolsin Enviado");
        setDescripcion("...");
    }

    // Flujo básico: En bolsín enviado -> Recibida y Aceptada
    @Override
    public void aceptarDocumentacion(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoDoc> cambioEstado) {

        this.cambiarEstado(fechaActual, cambioEstado);

        EstadoDoc estado = this.crearEstadoRecibidaYAceptada();

        this.registrarNuevoEstado(documentacion, fechaActual, estado, empleadoLogueado);
    }

    // A3: En bolsín enviado -> No Recibida
    @Override
    public void marcarNoRecibida(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoDoc> cambioEstado) {

        this.cambiarEstado(fechaActual, cambioEstado);

        EstadoDoc estado = this.crearEstadoNoRecibida();

        this.registrarNuevoEstado(documentacion, fechaActual, estado, empleadoLogueado);
    }

    // A4: En bolsín enviado -> Recibida y Rechazada
    @Override
    public void rechazarDocumentacion(Documentacion documentacion, LocalDateTime fechaActual,
            Empleado empleadoLogueado, List<CambioEstadoDoc> cambioEstado) {

        this.cambiarEstado(fechaActual, cambioEstado);

        EstadoDoc estado = this.crearEstadoRecibidaYRechazada();

        this.registrarNuevoEstado(documentacion, fechaActual, estado, empleadoLogueado);
    }

    private void cambiarEstado(LocalDateTime fechaActual, List<CambioEstadoDoc> cambioEstado) {
        for (CambioEstadoDoc ced : cambioEstado) {
            if (ced.sosActual()) {
                ced.setFechaHoraFin(fechaActual);
                break;
            }
        }
    }

    private void registrarNuevoEstado(Documentacion documentacion, LocalDateTime fechaActual, EstadoDoc estado,
            Empleado empleadoLogueado) {
        CambioEstadoDoc nuevoCambioEstado = this.crearCambioEstado(fechaActual, estado, empleadoLogueado,
                documentacion);

        documentacion.agregarCambioEstado(nuevoCambioEstado);
        documentacion.setEstado(estado);
    }

    private EstadoDoc crearEstadoRecibidaYAceptada() {
        return new DocRecibidoYAceptado();
    }

    private EstadoDoc crearEstadoNoRecibida() {
        return new DocNoRecibida();
    }

    private EstadoDoc crearEstadoRecibidaYRechazada() {
        return new DocRecibidaYRechazada();
    }

    private CambioEstadoDoc crearCambioEstado(LocalDateTime fechaActual, EstadoDoc estado,
            Empleado empleadoLogueado, Documentacion documentacion) {
        return new CambioEstadoDoc(fechaActual, estado, empleadoLogueado, documentacion);
    }

}
