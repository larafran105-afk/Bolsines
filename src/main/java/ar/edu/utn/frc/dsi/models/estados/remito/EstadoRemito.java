package ar.edu.utn.frc.dsi.models.estados.remito;

import ar.edu.utn.frc.dsi.models.Remito;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity 
@Table (name = "estado_remito")
@Inheritance (strategy = InheritanceType.SINGLE_TABLE)
public abstract class EstadoRemito {

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
    public void aceptarRemito(Remito remito) {
        throw new IllegalStateException("El remito en estado " + getNombre() + " no puede aceptarse");
    }
        

 
}
