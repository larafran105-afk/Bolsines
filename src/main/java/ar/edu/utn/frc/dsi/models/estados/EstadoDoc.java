package ar.edu.utn.frc.dsi.models.estados;

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

    public abstract void aceptarDocumentacion(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoDoc> cambioEstado);

}
