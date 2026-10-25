package ar.edu.utn.frc.dsi.models.estados;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.CambioEstadoDoc;
import ar.edu.utn.frc.dsi.models.Documentacion;
import ar.edu.utn.frc.dsi.models.Empleado;
import jakarta.persistence.Entity;

@Entity 
public class EstadoDocEnBolsinEnviado extends EstadoDoc{

    @Override
    public void aceptarDocumentacion(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoDoc> cambioEstado) {
            this.cambiarEstado(fechaActual, cambioEstado);

            EstadoDoc nuevoEstado = this.crearEstado();

            CambioEstadoDoc nuevoCambioEstado = this.creaCambioEstado(fechaActual, nuevoEstado, empleadoLogueado, documentacion);
            // Añadir en el diagrama de secuencia que se pasa documentacion como parametro y tambien empleado

            documentacion.agregarCambioEstado(nuevoCambioEstado);
       
    }

    
    
    private void cambiarEstado(LocalDateTime fechaActual, List<CambioEstadoDoc> cambioEstado) {
        for (CambioEstadoDoc ced : cambioEstado) {
            if(ced.sosActual()) {
                ced.setFechaHoraFin(fechaActual);
                break;
            }
        }
    }
    
    private EstadoDoc crearEstado() {
        return new EstadoDocRecibidoYAceptado();
    }
    
    private CambioEstadoDoc creaCambioEstado(LocalDateTime fechaActual, EstadoDoc nuevoEstado,
            Empleado empleadoLogueado, Documentacion documentacion) {
                return new CambioEstadoDoc(fechaActual, nuevoEstado, empleadoLogueado, documentacion);
            }
    
}
