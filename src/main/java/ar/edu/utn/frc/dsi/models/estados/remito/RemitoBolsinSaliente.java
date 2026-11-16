package ar.edu.utn.frc.dsi.models.estados.remito;

import jakarta.persistence.Entity;

@Entity
public class RemitoBolsinSaliente extends EstadoRemito {

    public RemitoBolsinSaliente() {
        setNombre("En Bolsín Saliente");
        setDescripcion("...");
    }

}
