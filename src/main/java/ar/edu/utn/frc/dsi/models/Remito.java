package ar.edu.utn.frc.dsi.models;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frc.dsi.models.estados.remito.EstadoRemito;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "remito")

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

    public int getNumero() {
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

    // A3: la documentación indicada no llegó; el resto se acepta.
    public void registrarFaltantes(LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<Integer> nrosDocFaltantes) {
        this.actualizarEstadoSegun(nrosDocFaltantes);

        for (DetalleRemito detalle : this.detalleRemito) {
            if (detalle.contieneDocumentacion(nrosDocFaltantes)) {
                detalle.marcarNoRecibida(fechaActual, empleadoLogueado);
            } else {
                detalle.aceptarDocumentacion(fechaActual, empleadoLogueado);
            }
        }
    }

    // A4: la documentación indicada no corresponde a la CM destino; el resto se
    // acepta.
    public void registrarRechazos(LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<Integer> nrosDocRechazados) {
        this.actualizarEstadoSegun(nrosDocRechazados);

        for (DetalleRemito detalle : this.detalleRemito) {
            if (detalle.contieneDocumentacion(nrosDocRechazados)) {
                detalle.rechazarDocumentacion(fechaActual, empleadoLogueado);
            } else {
                detalle.aceptarDocumentacion(fechaActual, empleadoLogueado);
            }
        }
    }

    // Si alguna documentación del remito no se acepta, el remito queda como
    // Recibido y Aceptado Parcial; si no, Recibido y Aceptado.
    private void actualizarEstadoSegun(List<Integer> nrosDocNoAceptados) {
        if (this.tieneDocumentacion(nrosDocNoAceptados)) {
            this.estado.aceptarParcialmente(this);
        } else {
            this.estado.aceptarRemito(this);
        }
    }

    private boolean tieneDocumentacion(List<Integer> nrosDoc) {
        for (DetalleRemito detalle : this.detalleRemito) {
            if (detalle.contieneDocumentacion(nrosDoc)) {
                return true;
            }
        }
        return false;
    }

}
