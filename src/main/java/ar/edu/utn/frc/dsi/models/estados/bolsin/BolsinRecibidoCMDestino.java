package ar.edu.utn.frc.dsi.models.estados.bolsin;

import jakarta.persistence.Entity;

@Entity
public class BolsinRecibidoCMDestino extends EstadoBolsin {

    public BolsinRecibidoCMDestino() {
        setNombre("Recibido en CM destino");
        setDescripcion("...");
    }

}
