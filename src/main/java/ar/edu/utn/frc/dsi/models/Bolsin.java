package ar.edu.utn.frc.dsi.models;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frc.dsi.models.estados.EstadoBolsin;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bolsin")

public class Bolsin {

    @Id
    private int nroBolsin;

    private int nroPrecinto;
    private LocalDate fecha;
    private double peso;

    @ManyToOne
    @JoinColumn(name = "cm_destino")
    private ComisionMedica comisionMedicaDestino;

    @ManyToOne
    @JoinColumn(name = "cm_origen")
    private ComisionMedica comisionMedicaOrigen;

    @OneToMany(mappedBy = "bolsin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Remito> remito;

    @OneToMany(mappedBy = "bolsin", cascade = CascadeType.ALL)
    private List<CambioEstadoBolsin> cambioEstadoBolsin;

    public int getNroBolsin() {
        return nroBolsin;
    }

    public void setNroBolsin(int nroBolsin) {
        this.nroBolsin = nroBolsin;
    }

    public int getNroPrecinto() {
        return nroPrecinto;
    }

    public void setNroPrecinto(int nroPrecinto) {
        this.nroPrecinto = nroPrecinto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public ComisionMedica getComisionMedicaDestino() {
        return comisionMedicaDestino;
    }

    public void setComisionMedicaDestino(ComisionMedica comisionMedicaDestino) {
        this.comisionMedicaDestino = comisionMedicaDestino;
    }

    public ComisionMedica getComisionMedicaOrigen() {
        return comisionMedicaOrigen;
    }

    public void setComisionMedicaOrigen(ComisionMedica comisionMedicaOrigen) {
        this.comisionMedicaOrigen = comisionMedicaOrigen;
    }

    public List<Remito> getRemito() {
        return remito;
    }

    public void setRemito(List<Remito> remito) {
        this.remito = remito;
    }

    public List<CambioEstadoBolsin> getCambioEstadoBolsin() {
        return cambioEstadoBolsin;
    }

    public void setCambioEstadoBolsin(List<CambioEstadoBolsin> cambioEstadoBolsin) {
        this.cambioEstadoBolsin = cambioEstadoBolsin;
    }

    public boolean esTuCmDestino(ComisionMedica cmGestor) {
        if (!this.comisionMedicaDestino.equals(cmGestor)) {
            return false;
        }

        for (CambioEstadoBolsin ceb : this.cambioEstadoBolsin) {

            if (ceb.sosActual()) {

                return ceb.sosEnviado();

            }
        }
        return false;
    }

    public String getCmOrigen() {
        return this.comisionMedicaOrigen.getNombre();
    }

    public List<String> obtenerInformacionRemitos() {
        List<String> datosFormateados = new ArrayList<>();

        for (Remito remito : this.remito) {
            String nroRemito = String.valueOf(remito.getNroRemito());

            List<String> detallesProcesados = remito.buscarDocumentacion();

            for (String detalle : detallesProcesados) {
                datosFormateados.add(nroRemito + "," + detalle);
            }
        }
        return datosFormateados;

    }

    public void registrarRecepcion(LocalDateTime fechaActual, Empleado empleadoLogueado) {
        for (CambioEstadoBolsin ceb : this.cambioEstadoBolsin) {

            if (ceb.sosActual()) {
                EstadoBolsin estadoActual = ceb.getEstado();
                estadoActual.registrarRecepcion(this, fechaActual, empleadoLogueado, this.cambioEstadoBolsin);

            }
        }

        for (Remito r : this.remito) {
            r.aceptarRemito(fechaActual, empleadoLogueado);
            
        }
    }

    public void agregarCambioEstado(CambioEstadoBolsin nuevoCambioEstado) {
        this.cambioEstadoBolsin.add(nuevoCambioEstado);

        // No implemento el setEstado del diagrama, lo veo redundante y no tenemos un
        // estado que setear aca
    }
}
