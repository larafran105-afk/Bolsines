package ar.edu.utn.frc.dsi.models.estados.doc;

import jakarta.persistence.Entity;

@Entity
public class DocRegistrada extends EstadoDoc {

    public DocRegistrada() {
        setNombre("Registrada");
        setDescripcion("...");
    }

}
