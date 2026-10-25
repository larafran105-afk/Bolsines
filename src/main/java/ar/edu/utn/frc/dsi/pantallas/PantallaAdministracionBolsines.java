package ar.edu.utn.frc.dsi.pantallas;

import ar.edu.utn.frc.dsi.models.Bolsin;
import ar.edu.utn.frc.dsi.models.ComisionMedica;
import ar.edu.utn.frc.dsi.models.Sesion;

import java.util.List;
import java.util.Scanner;

import ar.edu.utn.frc.dsi.gestores.GestorRecepcionBolsin;

public class PantallaAdministracionBolsines {
    private GestorRecepcionBolsin gestor;
    private Scanner scanner;

    private List<Bolsin> bolsinesParaMostrar;

    public PantallaAdministracionBolsines() {
        this.gestor = new GestorRecepcionBolsin();
        this.scanner = new Scanner(System.in);
    }

    public void opcionRegRecepBolsin(Sesion sesionActual) {
        this.habilitarVentana();
        this.gestor.iniciarRegistro(sesionActual, this);
    }

    public void habilitarVentana() {
        System.out.println("Ventana");
        // Interefaz a implementar
    }

    public void mostrarCMEmpLogueado(ComisionMedica comisionMedica) {
        System.out.println("Comisión Médica del empleado logueado: " + comisionMedica.getNombre());
        System.out.println("Buscando bolsines enviados a esta CM...\n");
    }

    public void mostraCmOrigen(String cmOrigenTemporal) {
        System.out.println("Comisión Médica de origen del bolsín: " + cmOrigenTemporal);
    }

    public void mostrarNumPrecinto(int nroPrecintoTemporal) {
        System.out.println("Número de precinto del bolsín: " + nroPrecintoTemporal);
    }

    public void solicitarSeleccionBolsin(List<Bolsin> bolsinesEnviados) {


        // TODO Auto-generated method stub
        // Hay que implementar la logica/interfaz para que el usuario selecciones el
        // bolsin (me quedo la duda de si es necesario que le pase la lista a la
        // funcion)
        Bolsin bolsinElegido = new Bolsin(); // Simulamos que el usuario eligio un bolsin de la lista
        this.tomarSeleccionBolsin(bolsinElegido);
        
    }

    public void tomarSeleccionBolsin(Bolsin bolsinElegido) {
        this.gestor.tomarSeleccionBolsin(bolsinElegido);
    }

    public void mostrarDatosRemitos(List<String> datosRemitos) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mostrarDatosRemitos'");
    }

    public void solicitarOpcionRecepcion() {
        
        System.out.println("Seleccione una opción de recepción:\r\n1. El contenido del bolsín es igual al registrado\r\n" + //
                        "2. No se recibe toda la documentación asociada a los remitos que contiene el bolsín\r\n" + //
                        "3. Existe documentación que no corresponde al destino\r\n" + //
                        "4. La documentación se debe redirigir a otra área. ");
        int opcion = this.scanner.nextInt();
        this.tomarSelecRecepcion(opcion);
        
    }

    public void tomarSelecRecepcion(int opcion) {
        this.gestor.tomarSelecRecepcion(opcion);
    }

    public void solicitarConfirmacion() {
        System.out.println("¿Desea confirmar la recepción del bolsín? (S/N)");
        this.tomarConfirmacion(this.scanner.nextLine().toUpperCase());
    }

    private void tomarConfirmacion(String nextLine) {
        this.gestor.tomarConfirmacion(nextLine);
    }

    public void mostrarMensajeExito() {
        System.out.println("LISTO EXITOSO");
    }

    

}
