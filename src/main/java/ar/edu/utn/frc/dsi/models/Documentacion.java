package ar.edu.utn.frc.dsi.models;

import java.util.List;

import ar.edu.utn.frc.dsi.models.estados.EstadoDoc;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity 
@Table (name = "documentacion")

public class Documentacion {
    @Id 
    private int nroDoc;

    private String asunto;
    private LocalDate fechaPase;
    private String descr;

    @ManyToOne 
    @JoinColumn (name = "tipoDoc")
    private TipoDoc tipoDoc;

    @ManyToOne
    @JoinColumn (name = "cm_origen")
    private ComisionMedica comisionMedicaOrigen;

    @OneToMany (mappedBy = "documentacion", cascade = CascadeType.ALL)
    private List<CambioEstadoDoc> cambioEstado;

    public int getNroDoc() {
        return nroDoc;
    }

    public void setNroDoc(int nroDoc) {
        this.nroDoc = nroDoc;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public LocalDate getFechaPase() {
        return fechaPase;
    }

    public void setFechaPase(LocalDate fechaPase) {
        this.fechaPase = fechaPase;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getTipoDoc() {
        return tipoDoc.getNombre();
    }

    public void setTipoDoc(TipoDoc tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public ComisionMedica getComisionMedicaOrigen() {
        return comisionMedicaOrigen;
    }

    public void setComisionMedicaOrigen(ComisionMedica comisionMedicaOrigen) {
        this.comisionMedicaOrigen = comisionMedicaOrigen;
    }

    public List<CambioEstadoDoc> getCambioEstado() {
        return cambioEstado;
    }

    public void setCambioEstado(List<CambioEstadoDoc> cambioEstado) {
        this.cambioEstado = cambioEstado;
    }

    public void aceptarDocumentacion(LocalDateTime fechaActual, Empleado empleadoLogueado) {
        for (CambioEstadoDoc ced : cambioEstado) {
            if(ced.sosActual()) {
                EstadoDoc estadoActual = ced.getEstado();
                estadoActual.aceptarDocumentacion(this, fechaActual, empleadoLogueado, this.cambioEstado);
                break;
            }
        }
    }

    public void agregarCambioEstado(CambioEstadoDoc nuevoCambioEstado) {
        this.cambioEstado.add(nuevoCambioEstado);
        // Como antes, tampoco implemento el setestado, porque no tenemos un estado como variable que setear.
    }
    
    
}
