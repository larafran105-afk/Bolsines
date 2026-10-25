package ar.edu.utn.frc.dsi.models.estados;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.Bolsin;
import ar.edu.utn.frc.dsi.models.CambioEstadoBolsin;
import ar.edu.utn.frc.dsi.models.Empleado;
import jakarta.persistence.*;

@Entity 
@Table (name = "estado_bolsin")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class EstadoBolsin {
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

    public boolean esEnviado() {
        return false;
    }

    public abstract void registrarRecepcion(Bolsin bolsin, LocalDateTime fechaActual, Empleado empleadoLogueado, List<CambioEstadoBolsin> historialCambiosEstado);

}
