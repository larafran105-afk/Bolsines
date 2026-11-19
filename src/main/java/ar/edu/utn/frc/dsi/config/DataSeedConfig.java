package ar.edu.utn.frc.dsi.config;

import ar.edu.utn.frc.dsi.models.*;
import ar.edu.utn.frc.dsi.models.estados.bolsin.EstadoBolsin;
import ar.edu.utn.frc.dsi.models.estados.bolsin.BolsinEnviado;
import ar.edu.utn.frc.dsi.models.estados.bolsin.BolsinRecibidoCMDestino;
import ar.edu.utn.frc.dsi.models.estados.doc.EstadoDoc;
import ar.edu.utn.frc.dsi.models.estados.doc.DocEnBolsinEnviado;
import ar.edu.utn.frc.dsi.models.estados.doc.DocNoRecibida;
import ar.edu.utn.frc.dsi.models.estados.doc.DocRecibidaYRechazada;
import ar.edu.utn.frc.dsi.models.estados.doc.DocRecibidoYAceptado;
import ar.edu.utn.frc.dsi.models.estados.remito.EstadoRemito;
import ar.edu.utn.frc.dsi.models.estados.remito.RemitoEnviado;
import ar.edu.utn.frc.dsi.models.estados.remito.RemitoRecibidoYAceptado;
import ar.edu.utn.frc.dsi.models.estados.remito.RemitoRecibidoYAceptadoParcialmente;
import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class DataSeedConfig implements CommandLineRunner {

    private final EntityManager entityManager;

    // Los ids de DetalleRemito se asignan a mano
    private int proximoIdDetalle = 1;

    public DataSeedConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Estados: se verifican siempre, así una base ya cargada recibe los
        // estados nuevos (No Recibida, Recibida y Rechazada, Aceptado Parcial).
        EstadoBolsin estadoBolsinEnviado = asegurarEstado(EstadoBolsin.class, "Enviado", () -> new BolsinEnviado());
        asegurarEstado(EstadoBolsin.class, "Recibido en CM destino", BolsinRecibidoCMDestino::new);

        EstadoRemito estadoRemitoEnviado = asegurarEstado(EstadoRemito.class, "Enviado", () -> new RemitoEnviado());
        asegurarEstado(EstadoRemito.class, "Recibido y Aceptado", RemitoRecibidoYAceptado::new);
        asegurarEstado(EstadoRemito.class, "Recibido y Aceptado Parcial", RemitoRecibidoYAceptadoParcialmente::new);

        EstadoDoc estadoDocEnviado = asegurarEstado(EstadoDoc.class, "En Bolsin Enviado", () -> new DocEnBolsinEnviado());
        asegurarEstado(EstadoDoc.class, "Recibida y Aceptada", DocRecibidoYAceptado::new);
        asegurarEstado(EstadoDoc.class, "No Recibida", DocNoRecibida::new);
        asegurarEstado(EstadoDoc.class, "Recibida y Rechazada", DocRecibidaYRechazada::new);

        // 2. Verificamos si ya hay datos para no duplicarlos cada vez que reiniciás el
        // backend
        Long count = entityManager.createQuery("SELECT count(c) FROM ComisionMedica c", Long.class).getSingleResult();
        if (count > 0) {
            return;
        }

        System.out.println("Inyectando datos de prueba para Bolsines...");

        // 3. Comisiones Médicas
        ComisionMedica cmCordoba = crearComisionMedica(1L, "Comisión Médica Córdoba");
        ComisionMedica cmMendoza = crearComisionMedica(2L, "Comisión Médica Mendoza");
        ComisionMedica cmRosario = crearComisionMedica(3L, "Comisión Médica Rosario");

        // 4. Empleado Logueado, Usuario y Sesión
        Empleado empLogueado = new Empleado();
        empLogueado.setId(1L);
        empLogueado.setNombre("Hugo Francisco");
        empLogueado.setApellido("Lara");
        empLogueado.setComisionMedica(cmCordoba); // Pertenece a Córdoba
        entityManager.persist(empLogueado);

        Usuario usu = new Usuario();
        usu.setId(1L);
        usu.setNombre("larafran105");
        usu.setEmpleado(empLogueado);
        entityManager.persist(usu);

        Sesion sesion = new Sesion();
        sesion.setId(1L);
        sesion.setUsuario(usu);
        sesion.setFechaHoraInicio(LocalDateTime.now());
        entityManager.persist(sesion);

        // 5. Tipos de Documento
        TipoDoc tdDictamen = crearTipoDoc("Dictamen");
        TipoDoc tdEstudio = crearTipoDoc("Estudio médico");
        TipoDoc tdCarta = crearTipoDoc("Carta documento");

        // 6. Bolsín 1042 (Mendoza -> Córdoba). Pensado para el flujo básico.
        Bolsin b1042 = crearBolsin(1042, 508231, cmMendoza, cmCordoba, estadoBolsinEnviado, empLogueado);
        Documentacion doc1 = crearDocumentacion(1, "Dictamen médico - Exp. 44.120/26", tdDictamen, cmMendoza,
                estadoDocEnviado, empLogueado);
        Documentacion doc2 = crearDocumentacion(2, "Estudio de columna lumbosacra", tdEstudio, cmMendoza,
                estadoDocEnviado, empLogueado);
        Documentacion doc3 = crearDocumentacion(3, "Notificación al empleador", tdCarta, cmMendoza,
                estadoDocEnviado, empLogueado);
        crearRemito(3318, b1042, estadoRemitoEnviado, doc1, doc2);
        crearRemito(3321, b1042, estadoRemitoEnviado, doc3);

        // 7. Bolsín 1047 (Rosario -> Córdoba). Pensado para A3: falta documentación.
        Bolsin b1047 = crearBolsin(1047, 508377, cmRosario, cmCordoba, estadoBolsinEnviado, empLogueado);
        Documentacion doc4 = crearDocumentacion(4, "Dictamen de incapacidad - Exp. 45.002/26", tdDictamen,
                cmRosario, estadoDocEnviado, empLogueado);
        Documentacion doc5 = crearDocumentacion(5, "Resonancia de rodilla derecha", tdEstudio, cmRosario,
                estadoDocEnviado, empLogueado);
        Documentacion doc6 = crearDocumentacion(6, "Citación a junta médica", tdCarta, cmRosario,
                estadoDocEnviado, empLogueado);
        crearRemito(3325, b1047, estadoRemitoEnviado, doc4, doc5);
        crearRemito(3326, b1047, estadoRemitoEnviado, doc6);

        // 8. Bolsín 1051 (Mendoza -> Córdoba). Pensado para A4: documentación que
        // no corresponde al destino.
        Bolsin b1051 = crearBolsin(1051, 509012, cmMendoza, cmCordoba, estadoBolsinEnviado, empLogueado);
        Documentacion doc7 = crearDocumentacion(7, "Dictamen médico - Exp. 44.388/26", tdDictamen, cmMendoza,
                estadoDocEnviado, empLogueado);
        Documentacion doc8 = crearDocumentacion(8, "Estudio audiométrico", tdEstudio, cmMendoza,
                estadoDocEnviado, empLogueado);
        crearRemito(3330, b1051, estadoRemitoEnviado, doc7, doc8);

        System.out.println("Datos inyectados con éxito.");
    }

    private <T> T asegurarEstado(Class<T> claseRaiz, String nombre, java.util.function.Supplier<T> crear) {
        T existente = entityManager.find(claseRaiz, nombre);
        if (existente != null) {
            return existente;
        }
        T nuevo = crear.get();
        entityManager.persist(nuevo);
        return nuevo;
    }

    private ComisionMedica crearComisionMedica(Long codigo, String nombre) {
        ComisionMedica cm = new ComisionMedica();
        cm.setCodigo(codigo);
        cm.setNombre(nombre);
        entityManager.persist(cm);
        return cm;
    }

    private TipoDoc crearTipoDoc(String nombre) {
        TipoDoc td = new TipoDoc();
        td.setNombre(nombre);
        entityManager.persist(td);
        return td;
    }

    private Bolsin crearBolsin(int nroBolsin, int nroPrecinto, ComisionMedica origen, ComisionMedica destino,
            EstadoBolsin estado, Empleado empleado) {
        Bolsin bolsin = new Bolsin();
        bolsin.setNroBolsin(nroBolsin);
        bolsin.setNroPrecinto(nroPrecinto);
        bolsin.setFecha(LocalDate.now());
        bolsin.setComisionMedicaOrigen(origen);
        bolsin.setComisionMedicaDestino(destino);
        bolsin.setRemito(new ArrayList<>());
        bolsin.setCambioEstadoBolsin(new ArrayList<>());

        // Cambio de estado actual (sin fecha fin) a "Enviado"
        bolsin.getCambioEstadoBolsin().add(new CambioEstadoBolsin(
                LocalDateTime.now().minusDays(2), estado, empleado, bolsin));
        bolsin.setEstado(estado);

        entityManager.persist(bolsin);
        return bolsin;
    }

    private Documentacion crearDocumentacion(int nroDoc, String asunto, TipoDoc tipo, ComisionMedica origen,
            EstadoDoc estado, Empleado empleado) {
        Documentacion doc = new Documentacion();
        doc.setNroDoc(nroDoc);
        doc.setAsunto(asunto);
        doc.setTipoDoc(tipo);
        doc.setComisionMedicaOrigen(origen);
        doc.setCambioEstado(new ArrayList<>());

        // Cambio de estado actual (sin fecha fin) a "En Bolsín Enviado"
        doc.getCambioEstado().add(new CambioEstadoDoc(
                LocalDateTime.now().minusDays(2), estado, empleado, doc));
        doc.setEstado(estado);

        entityManager.persist(doc);
        return doc;
    }

    private Remito crearRemito(int nroRemito, Bolsin bolsin, EstadoRemito estado, Documentacion... docs) {
        Remito remito = new Remito();
        remito.setNroRemito(nroRemito);
        remito.setFecha(LocalDate.now().minusDays(3));
        remito.setBolsin(bolsin);
        remito.setEstado(estado);
        remito.setDetalleRemito(new ArrayList<>());

        for (Documentacion doc : docs) {
            DetalleRemito detalle = new DetalleRemito();
            detalle.setId(proximoIdDetalle++);
            detalle.setRemito(remito);
            detalle.setDocumentacion(doc);
            remito.getDetalleRemito().add(detalle);
        }

        bolsin.getRemito().add(remito);
        entityManager.persist(remito);
        return remito;
    }
}
