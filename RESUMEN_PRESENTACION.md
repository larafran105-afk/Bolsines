# Resumen para la defensa – PPAI Bolsines, CU 28 "Registrar recepción de bolsín"

## 1. Qué es el sistema y qué CU tenemos

- **Bolsines**: sistema donde las Comisiones Médicas (CM) se mandan documentación entre sí. La documentación va en **remitos** (cada remito tiene **detalles**, cada detalle apunta a una **documentación**), y los remitos van dentro de un **bolsín** cerrado con un **precinto**.
- Somos **grupo par** → **CU 28 Registrar recepción de bolsín** + **máquina de estados de Documentación**.
- **Actor**: Encargado de Bolsines (EB).
- **Objetivo**: registrar que llegó un bolsín y actualizar el estado del bolsín, sus remitos y su documentación según lo que pasó.

## 2. El caso de uso (flujo básico)

1. EB elige "registrar recepción de bolsín".
2. Sistema muestra la **CM del usuario logueado** (Sesión → Usuario → Empleado → CM).
3. Busca los bolsines en estado **Enviado** cuyo destino es esa CM.
4. Muestra de cada uno **CM origen y nro de precinto** (se puede filtrar por ellos).
5. EB selecciona un bolsín.
6. Muestra cada remito (nro) y de cada documentación su asunto y tipo.
7. Muestra las 4 opciones de recepción:
   1. El contenido es igual al registrado
   2. No se recibe toda la documentación
   3. Hay documentación que no corresponde al destino
   4. Hay que redirigir a otra área
8. EB elige la **opción 1**. 9. Sistema pide confirmación. 10. EB confirma.
11. Sistema actualiza:
    - **Bolsín** → *Recibido en CM destino* (con el empleado responsable)
    - **Remitos** → *Recibido y Aceptado*
    - **Documentación** → *Recibida y Aceptada* (con el empleado responsable)
12. Se incluye el CU 29 (notificar a la CM origen) → nosotros lo simulamos con un `println`. Fin.

### Flujos alternativos (la consigna pide el básico + al menos 2)

| Flujo | Qué es | ¿Implementado? |
| --- | --- | --- |
| A1 / A2 | No hay bolsín con ese precinto / esa CM origen (filtros) | Sí, en el front con los filtros |
| **A3** | Opción 2: no llegó toda la documentación | **Sí** |
| **A4** | Opción 3: documentación que no corresponde al destino | **Sí** |
| A5 | Opción 4: redirigir a otra área | **No** (tira error "opción no disponible") |
| **A6** | El EB no confirma → vuelve a elegir opción | **Sí** |

## 3. Patrón de diseño: **State** (Gamma / GoF)

### Problema que resuelve (para decirlo de memoria)
> El comportamiento de Documentación, Remito y Bolsín **depende del estado en el que están**. Sin el patrón, cada clase tendría `if/switch` preguntando "¿en qué estado estoy?" para saber si puede pasar a otro y a cuál. Eso viola el principio abierto/cerrado: agregar un estado obliga a tocar todos esos `if`. Con State, **cada estado es una clase** que sabe a qué estados puede pasar; el objeto (contexto) **delega** en su estado actual.

### Roles del patrón
| Rol | En nuestro código (Documentación) |
| --- | --- |
| **Contexto** | `Documentacion` (tiene `estadoActual`) |
| **Estado abstracto** | `EstadoDoc` (clase abstracta) |
| **Estados concretos** | `DocEnBolsinEnviado`, `DocRecibidoYAceptado`, `DocNoRecibida`, `DocRecibidaYRechazada`, `DocRegistrada`, `DocEnRemito`, `DocBolsinSaliente`, `DocParaRedirigir`, `DocDeBaja` |

Lo mismo hicimos para **Bolsín** (`EstadoBolsin`, `BolsinEnviado`, `BolsinRecibidoCMDestino`, ...) y **Remito** (`EstadoRemito`, `RemitoEnviado`, `RemitoRecibidoYAceptado`, `RemitoRecibidoYAceptadoParcialmente`, ...).

### Cómo funciona (clave para explicar)
- `EstadoDoc` define los métodos de transición (`aceptarDocumentacion`, `marcarNoRecibida`, `rechazarDocumentacion`) y **por defecto tiran excepción** ("en este estado no se puede hacer eso").
- Solo el estado que tiene la flecha de salida en la máquina de estados **sobrescribe** el método. En nuestro CU ese estado es **`DocEnBolsinEnviado`**, que redefine los tres.
- `Documentacion` no decide nada, **delega**:
  ```java
  public void aceptarDocumentacion(LocalDateTime fecha, Empleado emp) {
      estadoActual.aceptarDocumentacion(this, fecha, emp, this.cambioEstado);
  }
  ```
- Dentro del estado concreto, cada transición hace siempre lo mismo:
  1. `cambiarEstado(...)` → busca el `CambioEstadoDoc` actual (`sosActual()`) y le pone **fechaHoraFin**.
  2. `crearEstadoX()` → crea el **nuevo estado** (`new DocRecibidoYAceptado()`, `new DocNoRecibida()` o `new DocRecibidaYRechazada()`).
  3. `registrarNuevoEstado(...)` → crea el nuevo `CambioEstadoDoc` (fecha inicio, estado, **empleado responsable**), lo agrega al historial de la documentación y hace `documentacion.setEstado(nuevo)`.
- **El estado es el que cambia el estado del contexto**, no el gestor. Esa es la idea central del patrón.
- **Polimorfismo para la defensa**: el mismo estado `DocEnBolsinEnviado` resuelve 3 transiciones distintas; y si la doc estuviera en otro estado, la misma llamada falla sola sin ningún `if`.

### Otro beneficio que se ve en el código
`Bolsin.buscarBolsinEnviado(cm)` antes recorría el historial de `CambioEstadoBolsin` (`sosActual()`, `sosEnviado()`). **Ahora le pregunta directo al estado**: `estadoActual.esEnviado()`.

## 4. Máquina de estados de Documentación (la que nos asignaron)

Transiciones que implementa el CU 28:

| Desde | Hacia | Método | Cuándo |
| --- | --- | --- | --- |
| En Bolsín Enviado | Recibida y Aceptada | `aceptarDocumentacion()` | Opción 1, o docs no marcadas en A3/A4 |
| En Bolsín Enviado | No Recibida | `marcarNoRecibida()` | A3 [doc marcada como faltante] |
| En Bolsín Enviado | Recibida y Rechazada | `rechazarDocumentacion()` | A4 [doc marcada como que no corresponde] |

(Los otros estados — Registrada, En Remito, Bolsín Saliente, Para Redirigir, De Baja — son de otros CU.)

## 5. Arquitectura / clases (análisis → diseño)

Patrón **MVC / Boundary-Control-Entity** (GRASP: Controlador, Experto, Creador):

- **Pantalla (boundary)**: `PantallaAdministracionBolsines` → en el código es un `@RestController` con endpoints REST que llama el front Vue.
- **Gestor (control)**: `GestorRecepcionBolsin` (`@Service`) → coordina el CU, guarda el estado de la conversación (bolsín seleccionado, opción elegida, docs marcadas, empleado logueado, fecha).
- **Entidades**: `Bolsin`, `Remito`, `DetalleRemito`, `Documentacion`, `CambioEstadoBolsin`, `CambioEstadoDoc`, `Empleado`, `ComisionMedica`, `Sesion`, `Usuario`, `TipoDoc`, y los estados.
- **Experto**: cada clase hace lo que sabe → `Bolsin.esTuCmDestino()`, `Bolsin.obtenerInformacionRemitos()`, `Remito.buscarDocumentacion()`, `DetalleRemito.obtenerDocumentacion()`.
- **Creador**: el estado crea al siguiente estado y al `CambioEstado`.

### Cadena de llamadas al confirmar (flujo básico)
```
Pantalla.tomarConfirmacion("S")
 → Gestor.tomarConfirmacion → getFechaHoraActual → registrarRecepcionBolsin
   → Bolsin.registrarRecepcion(fecha, empleado)
       → estadoActual(BolsinEnviado).registrarRecepcion(this, ...)   → Bolsín pasa a Recibido en CM destino
       → por cada Remito: aceptarRemito(...)
            → estado(RemitoEnviado).aceptarRemito(this)              → Remito pasa a Recibido y Aceptado
            → por cada DetalleRemito: aceptarDocumentacion(...)
                 → Documentacion.aceptarDocumentacion(...)
                     → estadoActual(DocEnBolsinEnviado).aceptarDocumentacion(this, ...)  → Recibida y Aceptada
 → bolsinRepository.save(bolsin)  (cascada guarda todo)
 → notificarRec() (CU 29 simulado) → mostrarMensajeExito → finCU
```

### A3 / A4 (qué cambia)
- Gestor guarda `opcionSeleccionada`. Con opción 2 o 3 la pantalla pide **marcar documentación** (`solicitarSeleccionDocumentacion`).
- `tomarSeleccionDocumentacion(nrosDoc)` valida: que se marque al menos una y que **pertenezca al bolsín** (`bolsin.contieneDocumentacion`).
- Al confirmar: `Bolsin.registrarRecepcionConFaltantes(...)` (A3) o `registrarRecepcionConRechazos(...)` (A4).
- En cada remito: si tiene alguna doc marcada → `aceptarParcialmente()` → **Recibido y Aceptado Parcial**; si no → **Recibido y Aceptado**.
- En cada detalle: si está marcada → `marcarNoRecibida()` (A3) / `rechazarDocumentacion()` (A4); si no → `aceptarDocumentacion()`.
- El bolsín pasa igual a **Recibido en CM destino**.

### A6
Si responde "N": el gestor limpia la opción y docs marcadas y llama a `pantalla.solicitarOpcionRecepcion()`. **No se registra nada.**

## 6. Detalles de implementación (los pide la consigna, tenerlos de memoria)

| Pregunta | Respuesta |
| --- | --- |
| Lenguaje | **Java** (17+) |
| Framework | **Spring Boot 3.2.5** (Spring Web + Spring Data JPA) |
| Tecnología | **Web**: backend REST en puerto 8080 + frontend **Vue 3 + Vite** (Tailwind) en 5173; Vite hace proxy de `/api` al backend |
| Base de datos | **MariaDB** (corriendo en Docker) |
| Persistencia | **JPA / Hibernate**, `ddl-auto=update` (crea las tablas solo); repositorios `BolsinRepository`, `SesionRepository` |

### ¿Cómo se persisten los estados? (pregunta probable)
- `EstadoDoc` es una `@Entity` abstracta con `@Inheritance(strategy = SINGLE_TABLE)`: **todos los estados en una sola tabla** `estado_doc`, con una columna discriminadora que indica la subclase. Id = `nombre`.
- `Documentacion` tiene `@ManyToOne estadoActual` → Hibernate, al leer, instancia la **subclase correcta** (por eso el polimorfismo funciona después de traer de la base).
- El historial (`CambioEstadoDoc`, `CambioEstadoBolsin`) es `@OneToMany` con `cascade = ALL`: al hacer `save(bolsin)` se guarda todo en cascada.
- Cada cambio anterior queda con `fecha_hora_fin` cargada y el actual con `NULL` (`sosActual()` = fechaHoraFin es null).
- Todo el registro va en `@Transactional` → si algo falla, no se guarda nada a medias.

### Datos de prueba (seed `DataSeedConfig`, solo con base vacía)
| Bolsín | Para mostrar |
| --- | --- |
| **1042** | Flujo básico (opción 1) |
| **1047** | A3 (opción 2) – marcar docs faltantes |
| **1051** | A4 (opción 3) – marcar docs rechazadas |

## 7. Cómo levantar para la demo (hacerlo ANTES de entrar)

```powershell
docker start bolsines-mariadb
mvn spring-boot:run
cd frontend-bolsines; npm run dev
```
Abrir http://localhost:5173. Si ya usaron los bolsines antes, **recrear la base** para que vuelvan a estar en Enviado:
```powershell
docker exec -it bolsines-mariadb mariadb -u root -p -e "DROP DATABASE IF EXISTS bolsines_db; CREATE DATABASE bolsines_db;"
```
y reiniciar el backend. Para mostrar en la base que cambiaron los estados:
```powershell
docker exec -it bolsines-mariadb mariadb -u root -p bolsines_db -e "SELECT nro_bolsin, estado_actual FROM bolsin; SELECT nro_remito, estado FROM remito; SELECT nro_doc, estado_actual FROM documentacion;"
```

**Orden sugerido de demo**: 1042 opción 1 → mostrar en la base → 1047 opción 2 → 1051 opción 3 → en alguno decir "No, volver" para mostrar A6 → filtros por precinto/CM (A1/A2).

## 8. Preguntas probables del tutor y respuesta corta

- **¿Por qué State y no un `switch`?** Porque el comportamiento depende del estado, elimina condicionales, cada estado encapsula sus transiciones, y agregar un estado nuevo no obliga a modificar `Documentacion` (abierto/cerrado).
- **¿Quién cambia el estado?** El **estado concreto** (`DocEnBolsinEnviado`), que crea el nuevo estado y se lo setea al contexto con `setEstado`.
- **¿Qué pasa si intento aceptar una doc que ya está Recibida y Aceptada?** El método heredado de `EstadoDoc` tira `IllegalStateException`: la transición no existe en la máquina de estados.
- **¿Para qué sirve `CambioEstadoDoc`?** Es el historial: fecha inicio, fecha fin, estado y **empleado responsable** (lo pide el paso 11).
- **¿Por qué el remito queda "Parcial" en A4?** Decisión nuestra: el ERS solo lo dice para A3; lo aplicamos igual en A4 porque parte de la documentación no se aceptó (decirlo como decisión de diseño).
- **¿Diferencia entre análisis y diseño?** En análisis los estados eran objetos con `esEnviado()` y se recorría el historial; en diseño aplicamos State: las transiciones viven en las clases estado y la entidad delega.
- **¿Qué es la Pantalla en una app web?** El boundary: en el back es el `@RestController` (recibe los eventos del EB) y el front Vue es la interfaz visual.
- **¿La opción 4?** A5 no está implementado; la consigna pide básico + 2 alternativos, y tenemos A3, A4 y A6 (+ A1/A2 con filtros).

## 9. Cosas "flojas" que conviene saber por si preguntan

- `CMOrigen` en el gestor es `String` (nombre) y en el diagrama figura como `ComisionMedica`.
- En el gestor, los datos de los bolsines se mandan como lista a la pantalla en vez de mostrarse uno por uno dentro del loop.
- El CU 29 (notificación por mail) está simulado con un `println`.
- `modificaciones.md` en el repo lista todo lo que hay que actualizar en los diagramas para que coincidan con el código; si el diagrama no está actualizado, conviene saberlo.
