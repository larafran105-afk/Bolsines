package ar.edu.utn.frc.dsi.models.estados.doc;

import jakarta.persistence.Entity;

@Entity
public class DocParaRedirigir extends EstadoDoc {

    public DocParaRedirigir() {
        setNombre("Para Redirigir");
        setDescripcion("...");
    }

}
