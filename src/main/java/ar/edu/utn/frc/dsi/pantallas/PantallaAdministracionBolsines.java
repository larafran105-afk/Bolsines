package ar.edu.utn.frc.dsi.pantallas;

import ar.edu.utn.frc.dsi.gestores.GestorRecepcionBolsin;
import ar.edu.utn.frc.dsi.models.Bolsin;
import ar.edu.utn.frc.dsi.models.ComisionMedica;
import ar.edu.utn.frc.dsi.models.Sesion;
import ar.edu.utn.frc.dsi.repositories.bolsinRepository;
import ar.edu.utn.frc.dsi.repositories.sesionRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recepcion-bolsin")

public class PantallaAdministracionBolsines {
    @Autowired
    private GestorRecepcionBolsin gestor;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private BolsinRepository bolsinRepository;

    private Map<String, Object> respuestaParaVue;

    @PostMapping("/iniciar")
    public Map<String, Object> opcionRegRecepBolsin() {

        this.respuestaParaVue = new HashMap<>();
        this.habilitarVentana();

        Sesion sesionActual = this.sesionRepository
                .findFirstByFechaHoraFinIsNullOrderByFechaHoraInicioDesc()
                .orElseThrow(() -> new RuntimeException("No hay sesión activa"));

        this.gestor.iniciarRegistro(sesionActual, this);

        return this.respuestaParaVue;
    }

    public void habilitarVentana() {
        System.out.println("Ventana");
        // Interefaz a implementar
    }

    public void mostrarCMEmpLogueado(ComisionMedica comisionMedica) {

        this.respuestaParaVue.put("comisionMedica", comisionMedica.getNombre());

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

        List<Map<String, Object>> bolsinesParaVue = new ArrayList<>();

        for (Bolsin bolsin : bolsinesEnviados) {
            Map<String, Object> mapa = new HashMap<>();
            mapa.put("nroBolsin", bolsin.getNroBolsin());
            mapa.put("cmOrigen", bolsin.getCmOrigen());
            mapa.put("nroPrecinto", bolsin.getNroPrecinto());
            mapa.put("fechaEnvio", bolsin.getFecha() != null ? bolsin.getFecha().toString() : null);
            mapa.put("cantRemitos", bolsin.getRemito() != null ? bolsin.getRemito().size() : 0);
            bolsinesParaVue.add(mapa);

        }

        this.respuestaParaVue.put("bolsines", bolsinesParaVue);
    }

    @PostMapping("/seleccion")
    public Map<String, Object> tomarSeleccionBolsin(@RequestBody Map<String, Integer> payload) {
        this.respuestaParaVue = new HashMap<>();

        Integer nroBolsin = payload.get("nroBolsin");
        if (nroBolsin == null) {
            throw new IllegalArgumentException("El número de bolsín es obligatorio");
        }

        Bolsin bolsinElegido = this.bolsinRepository.findById(nroBolsin)
                .orElseThrow(() -> new RuntimeException("No se encontró el bolsín con número: " + nroBolsin));

        this.gestor.tomarSeleccionBolsin(bolsinElegido);
        return this.respuestaParaVue;
    }

    public void mostrarDatosRemitos(List<String> datosRemitos) {
        Map<String, List<Map<String, Object>>> documentosPorRemito = new LinkedHashMap<>();

        for (String dato : datosRemitos) {
            int primeraComa = dato.indexOf(',');
            int ultimaComa = dato.lastIndexOf(',');
            if (primeraComa < 0 || primeraComa == ultimaComa) {
                continue;
            }

            String nroRemito = dato.substring(0, primeraComa).trim();

            Map<String, Object> documento = new HashMap<>();
            documento.put("asunto", dato.substring(primeraComa + 1, ultimaComa).trim());
            documento.put("tipoDocumento", dato.substring(ultimaComa + 1).trim());

            documentosPorRemito.computeIfAbsent(nroRemito, k -> new ArrayList<>()).add(documento);
        }

        List<Map<String, Object>> remitosParaVue = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entrada : documentosPorRemito.entrySet()) {
            Map<String, Object> remito = new HashMap<>();
            remito.put("nroRemito", entrada.getKey());
            remito.put("documentos", entrada.getValue());
            remitosParaVue.add(remito);
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

        if (opcion == null) {
            throw new IllegalArgumentException("La opción de recepción es obligatoria");
        }

        this.gestor.tomarSelecRecepcion(opcion);

        return this.respuestaParaVue;
    }

    public void solicitarConfirmacion() {
        // System.out.println("¿Desea confirmar la recepción del bolsín? (S/N)");
        // this.tomarConfirmacion(this.scanner.nextLine().toUpperCase());

        this.respuestaParaVue.put("requiereConfirmacion", true);

    }

    @PostMapping("/confirmacion")
    public Map<String, Object> tomarConfirmacion(@RequestBody Map<String, String> payload) {
        this.respuestaParaVue = new HashMap<>();

        String respuesta = payload.get("respuesta");

        if (respuesta == null) {
            throw new IllegalArgumentException("Falta la respuesta de confirmación.");
        }

        this.gestor.tomarConfirmacion(respuesta.toUpperCase());

        if ("N".equalsIgnoreCase(respuesta)) {
            this.respuestaParaVue.put("exito", false);
            this.respuestaParaVue.put("mensaje", "Registro cancelado.");
        }

        return this.respuestaParaVue;
    }

    public void mostrarMensajeExito() {
        this.respuestaParaVue.put("exito", true);
        this.respuestaParaVue.put("mensaje", "Recepción registrada correctamente.");
    }

}
