package ar.edu.utn.frc.dsi.models.estados.bolsin;

import jakarta.persistence.Entity;

@Entity
public class BolsinCerrado extends EstadoBolsin {

    public BolsinCerrado() {
        setNombre("Cerrado");
        setDescripcion("...");
    }

}
