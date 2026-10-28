package ar.edu.utn.frc.dsi.pantallas;

import ar.edu.utn.frc.dsi.gestores.GestorRecepcionBolsin;
import ar.edu.utn.frc.dsi.models.Bolsin;
import ar.edu.utn.frc.dsi.models.ComisionMedica;
import ar.edu.utn.frc.dsi.models.Sesion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recepcion-bolsin")
@CrossOrigin(origins = "*")

public class PantallaAdministracionBolsines {
    @Autowired
    private GestorRecepcionBolsin gestor;

    private Map<String, Object> respuestaParaVue;

    @PostMapping("/iniciar")
    public Map<String, Object> opcionRegRecepBolsin(Sesion sesionActual) {

        this.respuestaParaVue = new HashMap<>();

        this.gestor.iniciarRegistro(sesionActual, this);

        return this.respuestaParaVue;
    }

    public void habilitarVentana() {
        System.out.println("Ventana");
        // Interefaz a implementar
    }

    public void mostrarCMEmpLogueado(ComisionMedica comisionMedica) {

        this.respuestaParaVue.put("comision medica", comisionMedica);

        // System.out.println("Comisión Médica del empleado logueado: " +
        // comisionMedica.getNombre());
        // System.out.println("Buscando bolsines enviados a esta CM...\n");
    }

    // ----- Lo quito ahora porque estoy implementando interfaz y solo uso la lista
    // de los bolsine ---------
    // public void mostraCmOrigen(String cmOrigenTemporal) {
    // System.out.println("Comisión Médica de origen del bolsín: " +
    // cmOrigenTemporal);
    // }

    // public void mostrarNumPrecinto(int nroPrecintoTemporal) {
    // System.out.println("Número de precinto del bolsín: " + nroPrecintoTemporal);
    // }

    public void solicitarSeleccionBolsin(List<Bolsin> bolsinesEnviados) {

        // ------------ ANTERIOR -------------
        // Hay que implementar la logica/interfaz para que el usuario selecciones el
        // bolsin (me quedo la duda de si es necesario que le pase la lista a la
        // funcion)
        // Bolsin bolsinElegido = new Bolsin(); // Simulamos que el usuario eligio un
        // bolsin de la lista
        // this.tomarSeleccionBolsin(bolsinElegido);
        // ------------ ANTERIOR -------------

        this.respuestaParaVue.put("bolsines", bolsinesEnviados);
    }

    @PutMapping("/seleccion")
    public Map<String, Object> tomarSeleccionBolsin(@RequestBody Map<String, Long> payload) {
        this.respuestaParaVue = new HashMap<>();

        Long nroBolsin = payload.get("nroBolsin");

        // ACA LO TENEMOS QUE BUSCAR DE LA BASE DE DATOS --- A IMPLEMENTAR ----
        Bolsin bolsinElegido = new Bolsin();

        this.gestor.tomarSeleccionBolsin(bolsinElegido);

        return this.respuestaParaVue;
    }

    public void mostrarDatosRemitos(List<String> datosRemitos) {

        List<Map<String, Object>> remitosParaVue = new ArrayList<>();

        this.respuestaParaVue.put("remitos", datosRemitos);

        for (String dato : datosRemitos) {
            String[] partes = dato.split(",");

            if (partes.length >= 3) {
                Map<String, Object> documento = new HashMap<>();
                documento.put("asunto", partes[1].trim());
                documento.put("tipoDocumento", partes[2].trim());

                Map<String, Object> remito = new HashMap<>();
                remito.put("nroRemito", partes[0].trim());
                remito.put("documentos", List.of(documento));

                remitosParaVue.add(remito);

            }
        }

        this.respuestaParaVue.put("remitos", remitosParaVue);

    }

    public void solicitarOpcionRecepcion() {

        // LO HACE EL FRONT
        // System.out.println(
        // "Seleccione una opción de recepción:\r\n1. El contenido del bolsín es igual
        // al registrado\r\n" + //
        // "2. No se recibe toda la documentación asociada a los remitos que contiene el
        // bolsín\r\n" + //
        // "3. Existe documentación que no corresponde al destino\r\n" + //
        // "4. La documentación se debe redirigir a otra área. ");
        // int opcion = this.scanner.nextInt();
        // this.tomarSelecRecepcion(opcion);

    }

    @PostMapping("/opcion")
    public Map<String, Object> tomarSelecRecepcion(@RequestBody Map<String, Integer> payload) {
        this.respuestaParaVue = new HashMap<>();

        Integer opcion = payload.get("opcion");

        this.gestor.tomarSelecRecepcion(opcion);

        return this.respuestaParaVue;
    }

    public void solicitarConfirmacion() {
        // System.out.println("¿Desea confirmar la recepción del bolsín? (S/N)");
        // this.tomarConfirmacion(this.scanner.nextLine().toUpperCase());

        this.respuestaParaVue.put("requiereConfirmacion", true);

    }

    @PostMapping("/confirmacion")
    private Map<String, Object> tomarConfirmacion(@RequestBody Map<String, String> payload) {
        this.respuestaParaVue = new HashMap<>();

        String respuesta = payload.get("respuesta");

        this.gestor.tomarConfirmacion(respuesta);

        if ("N".equalsIgnoreCase(respuesta)) {
            this.respuestaParaVue.put("exito", false);
            this.respuestaParaVue.put("mensaje", "Registro cancelado.");
        }

        return this.respuestaParaVue;
    }

    public void mostrarMensajeExito() {
        System.out.println("LISTO EXITOSO");
    }

}
