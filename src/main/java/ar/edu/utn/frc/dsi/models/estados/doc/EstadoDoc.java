package ar.edu.utn.frc.dsi.models.estados.doc;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.CambioEstadoDoc;
import ar.edu.utn.frc.dsi.models.Documentacion;
import ar.edu.utn.frc.dsi.models.Empleado;
import jakarta.persistence.*;

@Entity
@Table(name = "estado_doc")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class EstadoDoc {
    @Id
    private String nombre;

    private String descripcion;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Por defecto la transición no está permitida: solo la sobrescriben
    // los estados que tienen la flecha de salida en la máquina de estados.
    public void aceptarDocumentacion(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoDoc> cambioEstado) {
        throw new IllegalStateException("La documentación en estado " + getNombre() + " no puede aceptarse");
    }

    // A3: la documentación no llegó en el bolsín.
    public void marcarNoRecibida(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoDoc> cambioEstado) {
        throw new IllegalStateException(
                "La documentación en estado " + getNombre() + " no puede marcarse como no recibida");
    }

    // A4: la documentación no corresponde a la CM destino.
    public void rechazarDocumentacion(Documentacion documentacion, LocalDateTime fechaActual,
            Empleado empleadoLogueado, List<CambioEstadoDoc> cambioEstado) {
        throw new IllegalStateException("La documentación en estado " + getNombre() + " no puede rechazarse");
    }

}
