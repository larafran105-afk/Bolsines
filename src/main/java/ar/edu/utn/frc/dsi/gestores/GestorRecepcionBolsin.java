package ar.edu.utn.frc.dsi.gestores;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.*;
import ar.edu.utn.frc.dsi.pantallas.PantallaAdministracionBolsines;
import java.util.ArrayList;

public class GestorRecepcionBolsin {

    private LocalDateTime fechaActual;
    private Empleado empleadoLogueado;
    private ComisionMedica CMDestino;
    private String CMOrigen;
    private List<Bolsin> bolsinesEnviados = new ArrayList<>();
    private Bolsin bolsinSeleccionado;

    private Sesion sesionActual;
    private PantallaAdministracionBolsines pantalla;

    public void iniciarRegistro(Sesion sesionActual, PantallaAdministracionBolsines pantalla) {
        this.sesionActual = sesionActual;
        this.pantalla = pantalla;

        this.buscarUser();

        this.buscarCMEmp();

        this.pantalla.mostrarCMEmpLogueado(this.CMDestino);

        this.buscarBolsinEnviado();

    }

    private void buscarUser() {
        this.empleadoLogueado = this.sesionActual.buscarEmpleado();
    }

    private void buscarCMEmp() {
        this.CMDestino = this.empleadoLogueado.getCm();
    }

    private void buscarBolsinEnviado() {
        List<Bolsin> todosLosBolsines = new ArrayList<>(); // SIMULAMOS QUE TRAEMOS DE LA DB TODOS LOS BOLSINES

        for (Bolsin bolsin : todosLosBolsines) {
            if (bolsin.buscarBolsinEnviado(this.CMDestino)) {
                this.bolsinesEnviados.add(bolsin);

                // String cmOrigenTemporal = bolsin.getCmOrigen();
                // int nroPrecintoTemporal = bolsin.getNroPrecinto();

                // this.pantalla.mostraCmOrigen(cmOrigenTemporal);
                // this.pantalla.mostrarNumPrecinto(nroPrecintoTemporal);
                // ----- Lo quito ahora porque estoy implementando interfaz y solo uso la lista de los bolsine --------- 

                // Aca duda en cuanto a como implementar la muestra de datos (En diagrama lo
                // muestra fuera del loop pero tendria que hacer una lista )

            }
        }

        this.pantalla.solicitarSeleccionBolsin(this.bolsinesEnviados);
    }

    public void tomarSeleccionBolsin(Bolsin bolsinElegido) {
        this.bolsinSeleccionado = bolsinElegido;
        // Seteo el cmorigen pero me quedo con duda de porque lo seteo(creo que no lo
        // uso, pero esta como atributo en el diagrama de clases)
        this.CMOrigen = this.bolsinSeleccionado.getCmOrigen();

        this.buscarInfoRemitos();
    }

    private void buscarInfoRemitos() {
        List<String> datosRemitos = this.bolsinSeleccionado.obtenerInformacionRemitos();

        this.pantalla.mostrarDatosRemitos(datosRemitos);

        this.pantalla.solicitarOpcionRecepcion();

    }

    public void tomarSelecRecepcion(int opcion) {
        switch (opcion) {
            case 1:
                this.pantalla.solicitarConfirmacion();
                break;
            default:
                break;
        }

    }

    public void tomarConfirmacion(String nextLine) {
        switch (nextLine) {
            case "S":
                this.getFechaHoraActual();
                this.registrarRecepcionBolsin();
                this.notificarRec();
                this.pantalla.mostrarMensajeExito();
                this.finCU();
                break;

            default:
                break;
        }
    }

    private void getFechaHoraActual() {

        this.fechaActual = LocalDateTime.now();
    }

    private void registrarRecepcionBolsin() {
        this.bolsinSeleccionado.registrarRecepcion(this.fechaActual, this.empleadoLogueado);

    }

    private void notificarRec() {
        System.out.println("Ejecutando gestor del cu 29....");
    }

    private void finCU() {
        this.bolsinSeleccionado = null;
        this.fechaActual = null;
        System.out.println("fin cu");

    }
}
