package ar.edu.utn.frc.dsi.models.estados.doc;

import jakarta.persistence.Entity;

@Entity
public class DocRecibidoYAceptado extends EstadoDoc {

    public DocRecibidoYAceptado() {
        setNombre("Recibida y Aceptada");
        setDescripcion("...");
    }

}
