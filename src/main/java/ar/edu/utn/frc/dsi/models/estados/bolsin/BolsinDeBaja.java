package ar.edu.utn.frc.dsi.models.estados.bolsin;

import jakarta.persistence.Entity;

@Entity
public class BolsinDeBaja extends EstadoBolsin {

    public BolsinDeBaja() {
        setNombre("De Baja");
        setDescripcion("...");
    }

}
