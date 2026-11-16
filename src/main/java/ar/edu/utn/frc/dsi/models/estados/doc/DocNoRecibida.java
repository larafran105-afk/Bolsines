package ar.edu.utn.frc.dsi.models.estados.doc;

import jakarta.persistence.Entity;

@Entity
public class DocNoRecibida extends EstadoDoc {

    public DocNoRecibida() {
        setNombre("No Recibida");
        setDescripcion("...");
    }

}
