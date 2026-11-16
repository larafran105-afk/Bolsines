package ar.edu.utn.frc.dsi.models.estados.doc;

import jakarta.persistence.Entity;

@Entity
public class DocBolsinSaliente extends EstadoDoc {

    public DocBolsinSaliente() {
        setNombre("En Bolsín Saliente");
        setDescripcion("...");
    }

}
