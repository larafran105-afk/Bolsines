package ar.edu.utn.frc.dsi.gestores;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.utn.frc.dsi.models.*;
import ar.edu.utn.frc.dsi.pantallas.PantallaAdministracionBolsines;
import ar.edu.utn.frc.dsi.repositories.BolsinRepository;
import ar.edu.utn.frc.dsi.repositories.SesionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GestorRecepcionBolsin {

    private LocalDateTime fechaActual;
    private Empleado empleadoLogueado;
    private ComisionMedica CMDestino;
    private String CMOrigen;
    private List<Bolsin> bolsinesEnviados = new ArrayList<>();
    private Bolsin bolsinSeleccionado;
    private Integer opcionSeleccionada;
    private List<Integer> nrosDocSeleccionados = new ArrayList<>();

    // Opciones de recepción del paso 7 del CU 28
    private static final int OPCION_CONTENIDO_IGUAL = 1;
    private static final int OPCION_NO_SE_RECIBE_TODA = 2;
    private static final int OPCION_NO_CORRESPONDE_DESTINO = 3;

    private PantallaAdministracionBolsines pantalla;

    @Autowired
    private BolsinRepository bolsinRepository;
    @Autowired
    private SesionRepository sesionRepository;

    public void iniciarRegistro(PantallaAdministracionBolsines pantalla) {
        this.pantalla = pantalla;

        this.buscarUser();

        this.buscarCMEmp();

        this.pantalla.mostrarCmEmpLogueado(this.CMDestino);

        this.buscarBolsinEnviado();

    }

    private void buscarUser() {
        Sesion sesionActual = this.sesionRepository
                .findFirstByFechaHoraFinIsNullOrderByFechaHoraInicioDesc()
                .orElseThrow(() -> new RuntimeException("No hay sesión activa"));

        this.empleadoLogueado = sesionActual.buscarEmpleado();
    }

    private void buscarCMEmp() {
        this.CMDestino = this.empleadoLogueado.getCm();
    }

    private void buscarBolsinEnviado() {
        this.bolsinesEnviados.clear();
        List<Bolsin> todosLosBolsines = this.bolsinRepository.findAll();

        for (Bolsin bolsin : todosLosBolsines) {
            if (bolsin.buscarBolsinEnviado(this.CMDestino)) {
                this.bolsinesEnviados.add(bolsin);

                // String cmOrigenTemporal = bolsin.getCmOrigen();
                // int nroPrecintoTemporal = bolsin.getNroPrecinto();

                // this.pantalla.mostraCmOrigen(cmOrigenTemporal);
                // this.pantalla.mostrarNumPrecinto(nroPrecintoTemporal);
                // ----- Lo quito ahora porque estoy implementando interfaz y solo uso la lista
                // de los bolsine ---------

                // Aca duda en cuanto a como implementar la muestra de datos (En diagrama lo
                // muestra fuera del loop pero tendria que hacer una lista )

            }
        }

        this.pantalla.solicitarSeleccionBolsin(this.bolsinesEnviados);
    }

    public void tomarSeleccionBolsin(Bolsin bolsinElegido) {
        this.bolsinSeleccionado = this.bolsinRepository
                .findById(bolsinElegido.getNroBolsin())
                .orElseThrow();
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
        if (this.bolsinSeleccionado == null) {
            throw new IllegalStateException("No hay un bolsín seleccionado.");
        }

        this.opcionSeleccionada = opcion;
        this.nrosDocSeleccionados.clear();

        switch (opcion) {
            case OPCION_CONTENIDO_IGUAL:
                this.pantalla.solicitarConfirmacion();
                break;
            case OPCION_NO_SE_RECIBE_TODA: // A3
            case OPCION_NO_CORRESPONDE_DESTINO: // A4
                this.pantalla.solicitarSeleccionDocumentacion();
                break;
            default: // A5 (redirigir a otra área) no está implementado
                this.opcionSeleccionada = null;
                throw new IllegalArgumentException("La opción de recepción " + opcion + " no está disponible.");
        }
    }

    // A3/A4: el EB indica qué documentación no llegó o no corresponde.
    public void tomarSeleccionDocumentacion(List<Integer> nrosDoc) {
        if (this.opcionSeleccionada == null
                || this.opcionSeleccionada == OPCION_CONTENIDO_IGUAL) {
            throw new IllegalStateException("La opción elegida no requiere seleccionar documentación.");
        }
        if (nrosDoc == null || nrosDoc.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una documentación.");
        }
        for (Integer nroDoc : nrosDoc) {
            if (!this.bolsinSeleccionado.contieneDocumentacion(nroDoc)) {
                throw new IllegalArgumentException("La documentación " + nroDoc + " no pertenece al bolsín.");
            }
        }

        this.nrosDocSeleccionados.clear();
        this.nrosDocSeleccionados.addAll(nrosDoc);

        this.pantalla.solicitarConfirmacion();
    }

    @Transactional
    public void tomarConfirmacion(String nextLine) {
        if (this.bolsinSeleccionado == null || this.opcionSeleccionada == null) {
            throw new IllegalStateException("No hay una recepción pendiente de confirmar.");
        }

        switch (nextLine) {
            case "S":
                this.getFechaHoraActual();
                this.registrarRecepcionBolsin();
                this.notificarRec();
                this.pantalla.mostrarMensajeExito();
                this.finCU();
                break;

            default:
                // A6: el EB no confirma; vuelve a elegir la opción de recepción.
                this.opcionSeleccionada = null;
                this.nrosDocSeleccionados.clear();
                this.pantalla.solicitarOpcionRecepcion();
                break;
        }
    }

    private void getFechaHoraActual() {

        this.fechaActual = LocalDateTime.now();
    }

    private void registrarRecepcionBolsin() {
        this.bolsinSeleccionado = this.bolsinRepository
                .findById(this.bolsinSeleccionado.getNroBolsin())
                .orElseThrow();

        switch (this.opcionSeleccionada) {
            case OPCION_CONTENIDO_IGUAL:
                this.bolsinSeleccionado.registrarRecepcion(this.fechaActual, this.empleadoLogueado);
                break;
            case OPCION_NO_SE_RECIBE_TODA:
                this.bolsinSeleccionado.registrarRecepcionConFaltantes(this.fechaActual, this.empleadoLogueado,
                        this.nrosDocSeleccionados);
                break;
            case OPCION_NO_CORRESPONDE_DESTINO:
                this.bolsinSeleccionado.registrarRecepcionConRechazos(this.fechaActual, this.empleadoLogueado,
                        this.nrosDocSeleccionados);
                break;
            default:
                throw new IllegalStateException("Opción de recepción inválida.");
        }

        this.bolsinRepository.save(this.bolsinSeleccionado);

    }

    private void notificarRec() {
        System.out.println("Ejecutando gestor del cu 29....");
    }

    private void finCU() {
        this.bolsinSeleccionado = null;
        this.fechaActual = null;
        this.opcionSeleccionada = null;
        this.nrosDocSeleccionados.clear();
        System.out.println("fin cu");

    }
}
