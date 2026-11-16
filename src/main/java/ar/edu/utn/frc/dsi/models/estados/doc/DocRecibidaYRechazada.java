package ar.edu.utn.frc.dsi.models.estados.doc;

import jakarta.persistence.Entity;

@Entity
public class DocRecibidaYRechazada extends EstadoDoc {

    public DocRecibidaYRechazada() {
        setNombre("Recibida y Rechazada");
        setDescripcion("...");
    }

}
