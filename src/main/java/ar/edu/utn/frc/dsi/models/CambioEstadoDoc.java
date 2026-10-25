package ar.edu.utn.frc.dsi.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import ar.edu.utn.frc.dsi.models.estados.EstadoDoc;

@Entity
@Table(name = "cambio_estado_doc")

public class CambioEstadoDoc {

    @Id
    private int id;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    @ManyToOne
    @JoinColumn(name = "estado")
    private EstadoDoc estado;

    @ManyToOne
    @JoinColumn(name = "nro_doc")
    private Documentacion documentacion;

    @ManyToOne
    @JoinColumn(name = "id_empleado")

    
    private Empleado empleado;

    public CambioEstadoDoc(LocalDateTime fechaHoraInicio, EstadoDoc estado,
            Empleado empleado, Documentacion documentacion) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
        this.empleado = empleado;
        this.documentacion = documentacion;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public EstadoDoc getEstado() {
        return estado;
    }

    public void setEstado(EstadoDoc estado) {
        this.estado = estado;
    }

    public Documentacion getDocumentacion() {
        return documentacion;
    }

    public void setDocumentacion(Documentacion documentacion) {
        this.documentacion = documentacion;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public boolean sosActual() {
        return this.fechaHoraFin == null;
    }

}
