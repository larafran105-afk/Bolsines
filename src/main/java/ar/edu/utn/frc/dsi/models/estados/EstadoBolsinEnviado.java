package ar.edu.utn.frc.dsi.models.estados;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.Bolsin;
import ar.edu.utn.frc.dsi.models.CambioEstadoBolsin;
import ar.edu.utn.frc.dsi.models.Empleado;
import jakarta.persistence.Entity;

@Entity
public class EstadoBolsinEnviado extends EstadoBolsin {

    @Override
    public boolean esEnviado() {
        return true;
    }

    @Override
    public void registrarRecepcion(Bolsin bolsin, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoBolsin> historialCambiosEstado) {
        // Duda en cuanto al nombre del historial, por diagrama de secuencia y diagrama
        // de clases
        
        this.cambiarEstado(fechaActual, historialCambiosEstado);

        EstadoBolsin nuevoEstado = this.crearEstado();

        CambioEstadoBolsin nuevoCambioEstado = this.crearCambioEstado(fechaActual, nuevoEstado, empleadoLogueado, bolsin);
        // Añadir en el diagrama de secuencia que se pasa bolsin como parametro y tambien empleado

        bolsin.agregarCambioEstado(nuevoCambioEstado);

    }

    
    
    private void cambiarEstado(LocalDateTime fechaActual, List<CambioEstadoBolsin> historialCambiosEstado) {
        
        for (CambioEstadoBolsin ceb : historialCambiosEstado) {
            
            if (ceb.sosActual()) {
                
                ceb.setFechaHoraFin(fechaActual);
                
            }
        }
    }
    
    private EstadoBolsin crearEstado() {
        return new EstadoBolsinRecibidoCMDestino();
    }
    
    private CambioEstadoBolsin crearCambioEstado(LocalDateTime fechaActual, EstadoBolsin nuevoEstado,
            Empleado empleadoLogueado, Bolsin bolsin) {
        return new CambioEstadoBolsin(fechaActual, nuevoEstado, empleadoLogueado, bolsin);        
    }
    
}
