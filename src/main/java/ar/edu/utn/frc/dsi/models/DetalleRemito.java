package ar.edu.utn.frc.dsi.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity 
@Table (name = "detalle_remito")

public class DetalleRemito {
    @Id 
    private int id;
    
    @ManyToOne
    @JoinColumn (name = "nro_remito")
    private Remito remito;

    @ManyToOne
    @JoinColumn(name = "nro_doc")
    private Documentacion documentacion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Remito getRemito() {
        return remito;
    }

    public void setRemito(Remito remito) {
        this.remito = remito;
    }

    public Documentacion getDocumentacion() {
        return documentacion;
    }

    public void setDocumentacion(Documentacion documentacion) {
        this.documentacion = documentacion;
    }

    public String obtenerDocumentacion() {
        String asunto = this.documentacion.getAsunto();
        String tipoDoc = this.documentacion.getTipoDoc();
        return asunto + "," + tipoDoc;
    }

    public void aceptarDocumentacion(LocalDateTime fechaActual, Empleado empleadoLogueado) {
        this.documentacion.aceptarDocumentacion(fechaActual, empleadoLogueado);
    }
    
}
