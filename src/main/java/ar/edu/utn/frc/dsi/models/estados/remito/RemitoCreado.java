package ar.edu.utn.frc.dsi.models.estados.remito;

import jakarta.persistence.Entity;

@Entity
public class RemitoCreado extends EstadoRemito {

    public RemitoCreado() {
        setNombre("Creado");
        setDescripcion("...");
    }

}
