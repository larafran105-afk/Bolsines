package ar.edu.utn.frc.dsi.models.estados;

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

    public abstract void aceptarRemito(Remito remito);
        

 
}
