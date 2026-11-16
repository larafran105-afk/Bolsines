package ar.edu.utn.frc.dsi.models.estados.remito;

import jakarta.persistence.Entity;

@Entity
public class RemitoRecibidoYAceptado extends EstadoRemito {

    public RemitoRecibidoYAceptado() {
        setNombre("Recibido y Aceptado");
        setDescripcion("...");
    }

}
