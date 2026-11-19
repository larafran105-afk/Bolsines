package ar.edu.utn.frc.dsi.models;

import java.time.LocalDateTime;
import java.util.List;

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

    // Formato "nroDoc|asunto|tipoDoc". Se usa "|" porque el asunto puede tener comas.
    public String obtenerDocumentacion() {
        int nroDoc = this.documentacion.getNroDoc();
        String asunto = this.documentacion.getAsunto();
        String tipoDoc = this.documentacion.getTipoDoc();
        return nroDoc + "|" + asunto + "|" + tipoDoc;
    }

    public boolean contieneDocumentacion(List<Integer> nrosDoc) {
        return nrosDoc.contains(this.documentacion.getNroDoc());
    }

    public void aceptarDocumentacion(LocalDateTime fechaActual, Empleado empleadoLogueado) {
        this.documentacion.aceptarDocumentacion(fechaActual, empleadoLogueado);
    }

    public void marcarNoRecibida(LocalDateTime fechaActual, Empleado empleadoLogueado) {
        this.documentacion.marcarNoRecibida(fechaActual, empleadoLogueado);
    }

    public void rechazarDocumentacion(LocalDateTime fechaActual, Empleado empleadoLogueado) {
        this.documentacion.rechazarDocumentacion(fechaActual, empleadoLogueado);
    }

}
