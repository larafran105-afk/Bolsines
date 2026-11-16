package ar.edu.utn.frc.dsi.models.estados.remito;

import ar.edu.utn.frc.dsi.models.Remito;
import jakarta.persistence.Entity;

@Entity
public class RemitoEnviado extends EstadoRemito {

    @Override
    public void aceptarRemito(Remito remito) {
        EstadoRemito nuevoEstado = this.crearEstado();

        remito.setEstado(nuevoEstado);
    }

    private EstadoRemito crearEstado() {
        return new RemitoRecibidoYAceptado();
    }

}
