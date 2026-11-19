package ar.edu.utn.frc.dsi.models.estados.remito;

import ar.edu.utn.frc.dsi.models.Remito;
import jakarta.persistence.Entity;

@Entity
public class RemitoEnviado extends EstadoRemito {

    public RemitoEnviado() {
        setNombre("Enviado");
        setDescripcion("Remito enviado dentro de un bolsín");
    }

    // Flujo básico: Enviado -> Recibido y Aceptado
    @Override
    public void aceptarRemito(Remito remito) {
        EstadoRemito nuevoEstado = this.crearEstado();

        remito.setEstado(nuevoEstado);
    }

    // A3/A4: Enviado -> Recibido y Aceptado Parcial
    @Override
    public void aceptarParcialmente(Remito remito) {
        EstadoRemito nuevoEstado = this.crearEstadoParcial();

        remito.setEstado(nuevoEstado);
    }

    private EstadoRemito crearEstado() {
        return new RemitoRecibidoYAceptado();
    }

    private EstadoRemito crearEstadoParcial() {
        return new RemitoRecibidoYAceptadoParcialmente();
    }

}
