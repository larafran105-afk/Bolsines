package ar.edu.utn.frc.dsi.models.estados.doc;

import jakarta.persistence.Entity;

@Entity
public class DocDeBaja extends EstadoDoc {

    public DocDeBaja() {
        setNombre("De Baja");
        setDescripcion("...");
    }

}
