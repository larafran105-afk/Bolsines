package ar.edu.utn.frc.dsi.models;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frc.dsi.models.estados.EstadoRemito;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity 
@Table ( name="remito" )

public class Remito {

    @Id 
    private int nroRemito;

    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "nro_bolsin")
    private Bolsin bolsin;

    @ManyToOne
    @JoinColumn(name = "estado")
    private EstadoRemito estado;

    @OneToMany(mappedBy = "remito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleRemito> detalleRemito;

    public int getNroRemito() {
        return nroRemito;
    }

    public void setNroRemito(int nroRemito) {
        this.nroRemito = nroRemito;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Bolsin getBolsin() {
        return bolsin;
    }

    public void setBolsin(Bolsin bolsin) {
        this.bolsin = bolsin;
    }

    public EstadoRemito getEstado() {
        return estado;
    }

    public void setEstado(EstadoRemito estado) {
        this.estado = estado;
    }

    public List<DetalleRemito> getDetalleRemito() {
        return detalleRemito;
    }

    public void setDetalleRemito(List<DetalleRemito> detalleRemito) {
        this.detalleRemito = detalleRemito;
    }

    public List<String> buscarDocumentacion() {
        List<String> infoDetalles = new ArrayList<>();

        for (DetalleRemito detalle : this.detalleRemito) {
            infoDetalles.add(detalle.obtenerDocumentacion());
            // nombre en diagrama getDocumentacion, lo cambie por creo correcion del profe
        }

        return infoDetalles;
    }

    public void aceptarRemito(LocalDateTime fechaActual, Empleado empleadoLogueado) {
        this.estado.aceptarRemito(this);
    // en diagrama cambiar mientras haya documentacion por mientras hayan detalles 
        for (DetalleRemito detalle : this.detalleRemito) {
            detalle.aceptarDocumentacion(fechaActual, empleadoLogueado);
        }
    }


}
