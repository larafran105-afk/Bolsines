package ar.edu.utn.frc.dsi.models.estados.bolsin;

import jakarta.persistence.Entity;

@Entity
public class BolsinCreado extends EstadoBolsin {

    public BolsinCreado() {
        setNombre("Creado");
        setDescripcion("...");
    }

}
