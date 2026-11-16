package ar.edu.utn.frc.dsi.models.estados.remito;

import jakarta.persistence.Entity;

@Entity
public class RemitoCancelado extends EstadoRemito {

    public RemitoCancelado() {
        setNombre("Cancelado");
        setDescripcion("...");
    }

}
