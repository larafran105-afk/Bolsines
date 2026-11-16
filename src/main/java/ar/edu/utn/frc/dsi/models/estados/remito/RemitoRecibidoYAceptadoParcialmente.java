package ar.edu.utn.frc.dsi.models.estados.remito;

import jakarta.persistence.Entity;

@Entity
public class RemitoRecibidoYAceptadoParcialmente extends EstadoRemito {

    public RemitoRecibidoYAceptadoParcialmente() {
        setNombre("Recibido y Aceptado Parcial");
        setDescripcion("...");
    }

}
