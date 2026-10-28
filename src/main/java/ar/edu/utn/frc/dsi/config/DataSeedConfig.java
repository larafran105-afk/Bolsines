package ar.edu.utn.frc.dsi.config;

import ar.edu.utn.frc.dsi.models.*;
import ar.edu.utn.frc.dsi.models.estados.*;
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

    public DataSeedConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Verificamos si ya hay datos para no duplicarlos cada vez que reiniciás el backend
        Long count = entityManager.createQuery("SELECT count(c) FROM ComisionMedica c", Long.class).getSingleResult();
        if (count > 0) {
            return; 
        }

        System.out.println("Inyectando datos de prueba para Bolsines...");

        // 2. Comisiones Médicas
        ComisionMedica cmCordoba = new ComisionMedica();
        cmCordoba.setCodigo(1L);
        cmCordoba.setNombre("Comisión Médica Córdoba");
        
        ComisionMedica cmMendoza = new ComisionMedica();
        cmMendoza.setCodigo(2L);
        cmMendoza.setNombre("Comisión Médica Mendoza");
        
        entityManager.persist(cmCordoba);
        entityManager.persist(cmMendoza);

        // 3. Empleado Logueado, Usuario y Sesión
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

        // 4. Tipos de Documento
        TipoDoc tdDictamen = new TipoDoc();
        tdDictamen.setNombre("Dictamen");
        
        TipoDoc tdEstudio = new TipoDoc();
        tdEstudio.setNombre("Estudio médico");
        
        TipoDoc tdCarta = new TipoDoc();
        tdCarta.setNombre("Carta documento");
        
        entityManager.persist(tdDictamen);
        entityManager.persist(tdEstudio);
        entityManager.persist(tdCarta);

        // 5. Estados (Asumiendo que las clases hijas de Estado ya son @Entity)
        EstadoBolsin estadoBolsinEnviado = new EstadoBolsinEnviado();
        entityManager.persist(estadoBolsinEnviado);

        EstadoRemito estadoRemitoEnviado = new EstadoRemitoEnviado();
        entityManager.persist(estadoRemitoEnviado);

        // 6. Bolsín 1042 (De Mendoza a Córdoba)
        Bolsin bolsin = new Bolsin();
        bolsin.setNroBolsin(1042);
        bolsin.setNroPrecinto(508231);
        bolsin.setFecha(LocalDate.now());
        bolsin.setComisionMedicaOrigen(cmMendoza);
        bolsin.setComisionMedicaDestino(cmCordoba);
        bolsin.setRemito(new ArrayList<>());
        bolsin.setCambioEstadoBolsin(new ArrayList<>());

        // Cambio de Estado del Bolsín a "Enviado"
        CambioEstadoBolsin ceb = new CambioEstadoBolsin(
            LocalDateTime.now().minusDays(2), 
            estadoBolsinEnviado, 
            empLogueado, 
            bolsin
        );
        // Como CEB tiene el boolean sosActual() basado en fechaHoraFin == null, no seteamos fecha fin.
        bolsin.getCambioEstadoBolsin().add(ceb);
        
        entityManager.persist(bolsin); 

        // 7. Documentación
        Documentacion doc1 = new Documentacion();
        doc1.setNroDoc(1);
        doc1.setAsunto("Dictamen médico - Exp. 44.120/26");
        doc1.setTipoDoc(tdDictamen);
        
        Documentacion doc2 = new Documentacion();
        doc2.setNroDoc(2);
        doc2.setAsunto("Estudio de columna lumbosacra");
        doc2.setTipoDoc(tdEstudio);

        Documentacion doc3 = new Documentacion();
        doc3.setNroDoc(3);
        doc3.setAsunto("Notificación al empleador");
        doc3.setTipoDoc(tdCarta);

        entityManager.persist(doc1);
        entityManager.persist(doc2);
        entityManager.persist(doc3);

        // 8. Remito 3318 (Tiene Doc 1 y 2)
        Remito r1 = new Remito();
        r1.setNroRemito(3318);
        r1.setBolsin(bolsin);
        r1.setEstado(estadoRemitoEnviado);
        r1.setDetalleRemito(new ArrayList<>());

        DetalleRemito dr1 = new DetalleRemito();
        dr1.setId(1);
        dr1.setRemito(r1);
        dr1.setDocumentacion(doc1);
        
        DetalleRemito dr2 = new DetalleRemito();
        dr2.setId(2);
        dr2.setRemito(r1);
        dr2.setDocumentacion(doc2);
        
        r1.getDetalleRemito().add(dr1);
        r1.getDetalleRemito().add(dr2);
        entityManager.persist(r1);

        // 9. Remito 3321 (Tiene Doc 3)
        Remito r2 = new Remito();
        r2.setNroRemito(3321);
        r2.setBolsin(bolsin);
        r2.setEstado(estadoRemitoEnviado);
        r2.setDetalleRemito(new ArrayList<>());

        DetalleRemito dr3 = new DetalleRemito();
        dr3.setId(3);
        dr3.setRemito(r2);
        dr3.setDocumentacion(doc3);
        
        r2.getDetalleRemito().add(dr3);
        entityManager.persist(r2);
        
        System.out.println("Datos inyectados con éxito.");
    }
}