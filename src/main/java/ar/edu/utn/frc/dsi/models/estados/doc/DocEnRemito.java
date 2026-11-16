package ar.edu.utn.frc.dsi.models.estados.doc;

import jakarta.persistence.Entity;

@Entity
public class DocEnRemito extends EstadoDoc {

    public DocEnRemito() {
        setNombre("En Remito");
        setDescripcion("...");
    }

}
