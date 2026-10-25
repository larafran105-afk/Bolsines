package ar.edu.utn.frc.dsi.models.estados;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.CambioEstadoDoc;
import ar.edu.utn.frc.dsi.models.Documentacion;
import ar.edu.utn.frc.dsi.models.Empleado;
import jakarta.persistence.Entity;

@Entity 
public class EstadoDocRecibidoYAceptado extends EstadoDoc{

    @Override
    public void aceptarDocumentacion(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleadoLogueado,
            List<CambioEstadoDoc> cambioEstado) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'aceptarDocumentacion'");
    }
    
}
