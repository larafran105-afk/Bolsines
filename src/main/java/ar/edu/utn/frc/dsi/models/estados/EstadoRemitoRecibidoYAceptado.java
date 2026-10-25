package ar.edu.utn.frc.dsi.models.estados;

import ar.edu.utn.frc.dsi.models.Remito;
import jakarta.persistence.Entity;

@Entity 
public class EstadoRemitoRecibidoYAceptado extends EstadoRemito {

    @Override
    public void aceptarRemito(Remito remito) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'aceptarRemito'");
    }
    
}
