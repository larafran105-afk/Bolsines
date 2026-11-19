package ar.edu.utn.frc.dsi.models.estados.bolsin;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.Bolsin;
import ar.edu.utn.frc.dsi.models.CambioEstadoBolsin;
import ar.edu.utn.frc.dsi.models.Empleado;
import jakarta.persistence.Entity;

@Entity
public class BolsinEnviado extends EstadoBolsin {

    public BolsinEnviado() {
        setNombre("Enviado");
        setDescripcion("El bolsín enviado a la comision médica de destino");
    }

    @Override
    public boolean esEnviado() {
        return true;
    }

    @Override
    public void registrarRecepcion(Bolsin bolsin, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoBolsin> cambioEstadoBolsin) {
        // Duda en cuanto al nombre del historial, por diagrama de secuencia y diagrama
        // de clases

        this.cambiarEstado(fechaActual, cambioEstadoBolsin);

        EstadoBolsin nuevoEstado = this.crearEstado();

        CambioEstadoBolsin nuevoCambioEstado = this.crearCambioEstado(fechaActual, nuevoEstado, empleadoLogueado,
                bolsin);
        // Añadir en el diagrama de secuencia que se pasa bolsin como parametro y
        // tambien empleado

        bolsin.agregarCambioEstado(nuevoCambioEstado);
        bolsin.setEstado(nuevoEstado);

    }

    private void cambiarEstado(LocalDateTime fechaActual, List<CambioEstadoBolsin> cambioEstadoBolsin) {

        for (CambioEstadoBolsin ceb : cambioEstadoBolsin) {

            if (ceb.sosActual()) {

                ceb.setFechaHoraFin(fechaActual);
                break;
            }
        }
    }

    private EstadoBolsin crearEstado() {
        return new BolsinRecibidoCMDestino();
    }

    private CambioEstadoBolsin crearCambioEstado(LocalDateTime fechaActual, EstadoBolsin nuevoEstado,
            Empleado empleadoLogueado, Bolsin bolsin) {
        return new CambioEstadoBolsin(fechaActual, nuevoEstado, empleadoLogueado, bolsin);
    }

}
