package ar.edu.utn.frc.dsi.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import ar.edu.utn.frc.dsi.models.estados.EstadoBolsin;

@Entity 
@Table (name = "cambio_estado_bolsin")

public class CambioEstadoBolsin {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id;
    
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    @ManyToOne
    @JoinColumn (name = "estado")
    private EstadoBolsin estado;

    @ManyToOne
    @JoinColumn (name = "nro_bolsin")
    private Bolsin bolsin;
    
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    private Empleado empleado;

    public CambioEstadoBolsin(LocalDateTime fechaHoraInicio, EstadoBolsin estado, Empleado empleado, Bolsin bolsin) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
        this.bolsin = bolsin;
        this.empleado = empleado;
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

    public EstadoBolsin getEstado() {
        return estado;
    }

    public void setEstado(EstadoBolsin estado) {
        this.estado = estado;
    }

    public Bolsin getBolsin() {
        return bolsin;
    }

    public void setBolsin(Bolsin bolsin) {
        this.bolsin = bolsin;
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

    public boolean sosEnviado() {
        return this.estado.esEnviado();
    }
    
    
}
