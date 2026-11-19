# Modificaciones a los diagramas de diseño – CU 28 Registrar recepción de bolsín

Este documento lista qué hay que cambiar en el **diagrama de clases de diseño** y en el **diagrama de secuencia de diseño** para que coincidan con el código actual. Los cambios vienen de:

- implementar los flujos alternativos **A3** (no se recibe toda la documentación) y **A4** (documentación que no corresponde al destino) con el patrón State;
- guardar en el gestor la opción de recepción elegida;
- hacer que `buscarBolsinEnviado` le pregunte al estado en lugar de recorrer el historial;
- pendientes que ya estaban anotados en comentarios del código ("cambiar en el diagrama").

Convenciones: `+` público, `-` privado. En el diagrama los tipos se escriben como ya los usan (`DateTime` para `LocalDateTime`, `X[]` para `List<X>`).

---

## 1. Diagrama de clases de diseño

### 1.1 Patrón State – Documentación (máquina de estados asignada)

#### `EstadoDoc` (abstracta) — agregar 2 métodos

| Visib. | Firma | Comportamiento por defecto |
| ------ | ----- | -------------------------- |
| `+` | `aceptarDocumentacion(doc: Documentacion, fechaActual: DateTime, empleadoLogueado: Empleado, historial: CambioEstadoDoc[]): void` | *(ya existía)* lanza excepción |
| `+` | **`marcarNoRecibida(doc: Documentacion, fechaActual: DateTime, empleadoLogueado: Empleado, historial: CambioEstadoDoc[]): void`** | **nuevo** – lanza excepción |
| `+` | **`rechazarDocumentacion(doc: Documentacion, fechaActual: DateTime, empleadoLogueado: Empleado, historial: CambioEstadoDoc[]): void`** | **nuevo** – lanza excepción |

> Si en el diagrama figura `RegistrarRechazo()`, reemplazarlo por `rechazarDocumentacion(...)`.

#### `DocEnBolsinEnviado` (estado concreto) — redefine las 3 transiciones

| Visib. | Firma | Cambio |
| ------ | ----- | ------ |
| `+` | `aceptarDocumentacion(doc: Documentacion, fechaActual: DateTime, empleadoLogueado: Empleado, historial: CambioEstadoDoc[]): void` | ya existía |
| `+` | **`marcarNoRecibida(doc: Documentacion, fechaActual: DateTime, empleadoLogueado: Empleado, historial: CambioEstadoDoc[]): void`** | nuevo |
| `+` | **`rechazarDocumentacion(doc: Documentacion, fechaActual: DateTime, empleadoLogueado: Empleado, historial: CambioEstadoDoc[]): void`** | nuevo |
| `-` | `cambiarEstado(fechaActual: DateTime, historial: CambioEstadoDoc[]): void` | sin cambios |
| `-` | **`registrarNuevoEstado(doc: Documentacion, fechaActual: DateTime, estado: EstadoDoc, empleadoLogueado: Empleado): void`** | nuevo (agrupa crear el cambio de estado, agregarlo a la documentación y setear el estado) |
| `-` | **`crearEstadoRecibidaYAceptada(): EstadoDoc`** | reemplaza a `crearEstado()` |
| `-` | **`crearEstadoNoRecibida(): EstadoDoc`** | nuevo |
| `-` | **`crearEstadoRecibidaYRechazada(): EstadoDoc`** | nuevo |
| `-` | `crearCambioEstado(fechaActual: DateTime, estado: EstadoDoc, empleadoLogueado: Empleado, doc: Documentacion): CambioEstadoDoc` | se renombró (antes `creaCambioEstado`); recibe **empleado y documentación** y **devuelve** el `CambioEstadoDoc` (no `void`) |

Dependencias nuevas (flechas punteadas «create») desde `DocEnBolsinEnviado` hacia **`DocNoRecibida`** y **`DocRecibidaYRechazada`** (ya existía hacia `DocRecibidoYAceptado`).

#### `Documentacion` — agregar 2 métodos

| Visib. | Firma |
| ------ | ----- |
| `+` | `aceptarDocumentacion(fechaActual: DateTime, empleadoLogueado: Empleado): void` *(ya existía)* |
| `+` | **`marcarNoRecibida(fechaActual: DateTime, empleadoLogueado: Empleado): void`** |
| `+` | **`rechazarDocumentacion(fechaActual: DateTime, empleadoLogueado: Empleado): void`** |
| `+` | `agregarCambioEstado(cambio: CambioEstadoDoc): void` *(ya existía)* |
| `+` | `setEstado(estado: EstadoDoc): void` *(ya existía)* |

Los dos métodos nuevos delegan en `estadoActual` pasándose a sí misma y su historial.

### 1.2 Patrón State – Remito

#### `EstadoRemito` (abstracta) — agregar 1 método

| Visib. | Firma |
| ------ | ----- |
| `+` | `aceptarRemito(remito: Remito): void` *(ya existía)* |
| `+` | **`aceptarParcialmente(remito: Remito): void`** – por defecto lanza excepción |

#### `RemitoEnviado`

| Visib. | Firma | Cambio |
| ------ | ----- | ------ |
| `+` | `aceptarRemito(remito: Remito): void` | ya existía |
| `+` | **`aceptarParcialmente(remito: Remito): void`** | nuevo |
| `-` | `crearEstado(): EstadoRemito` | ya existía |
| `-` | **`crearEstadoParcial(): EstadoRemito`** | nuevo |

Dependencia nueva «create» desde `RemitoEnviado` hacia **`RemitoRecibidoYAceptadoParcialmente`**.

### 1.3 Patrón State – Bolsín

`BolsinEnviado` no cambia de comportamiento. Solo verificar que la firma en el diagrama sea:

- `+registrarRecepcion(bolsin: Bolsin, fechaActual: DateTime, empleadoLogueado: Empleado, historial: CambioEstadoBolsin[]): void`
- `-crearCambioEstado(fechaActual: DateTime, estado: EstadoBolsin, empleadoLogueado: Empleado, bolsin: Bolsin): CambioEstadoBolsin` (recibe **empleado y bolsín**; pendiente que ya estaba en un comentario del código)

### 1.4 Entidades

#### `Bolsin`

| Visib. | Firma | Cambio |
| ------ | ----- | ------ |
| `+` | `buscarBolsinEnviado(cm: ComisionMedica): boolean` | **ahora usa `estadoActual.esEnviado()`** (ya no recorre `CambioEstadoBolsin`) |
| `+` | `esTuCmDestino(cm: ComisionMedica): boolean` | compara por `codigo` |
| `+` | `obtenerInformacionRemitos(): String[]` | cada elemento con el formato `nroRemito\|nroDoc\|asunto\|tipoDoc` |
| `+` | `registrarRecepcion(fechaActual: DateTime, empleadoLogueado: Empleado): void` | ya existía (opción 1) |
| `+` | **`registrarRecepcionConFaltantes(fechaActual: DateTime, empleadoLogueado: Empleado, nrosDocFaltantes: Integer[]): void`** | nuevo (A3) |
| `+` | **`registrarRecepcionConRechazos(fechaActual: DateTime, empleadoLogueado: Empleado, nrosDocRechazados: Integer[]): void`** | nuevo (A4) |
| `+` | **`contieneDocumentacion(nroDoc: int): boolean`** | nuevo (validación del gestor) |

#### `Remito`

| Visib. | Firma | Cambio |
| ------ | ----- | ------ |
| `+` | `buscarDocumentacion(): String[]` | ya existía |
| `+` | `aceptarRemito(fechaActual: DateTime, empleadoLogueado: Empleado): void` | ya existía |
| `+` | **`registrarFaltantes(fechaActual: DateTime, empleadoLogueado: Empleado, nrosDocFaltantes: Integer[]): void`** | nuevo (A3) |
| `+` | **`registrarRechazos(fechaActual: DateTime, empleadoLogueado: Empleado, nrosDocRechazados: Integer[]): void`** | nuevo (A4) |
| `-` | **`actualizarEstadoSegun(nrosDocNoAceptados: Integer[]): void`** | nuevo: si el remito tiene alguna documentación no aceptada llama a `estado.aceptarParcialmente(this)`; si no, a `estado.aceptarRemito(this)` |
| `-` | **`tieneDocumentacion(nrosDoc: Integer[]): boolean`** | nuevo |

#### `DetalleRemito`

| Visib. | Firma | Cambio |
| ------ | ----- | ------ |
| `+` | `obtenerDocumentacion(): String` | ya existía (en el diagrama figuraba `getDocumentacion`, **renombrar**). Devuelve `nroDoc\|asunto\|tipoDoc` |
| `+` | **`contieneDocumentacion(nrosDoc: Integer[]): boolean`** | nuevo |
| `+` | `aceptarDocumentacion(fechaActual: DateTime, empleadoLogueado: Empleado): void` | ya existía |
| `+` | **`marcarNoRecibida(fechaActual: DateTime, empleadoLogueado: Empleado): void`** | nuevo |
| `+` | **`rechazarDocumentacion(fechaActual: DateTime, empleadoLogueado: Empleado): void`** | nuevo |

#### `Empleado`

- `+getCm(): ComisionMedica` – devuelve el **objeto** CM, no el nombre. Si en el diagrama figura `getNombre()` en ese paso, corregirlo (pendiente que ya estaba en un comentario del código).

### 1.5 Gestor y Pantalla

#### `GestorRecepcionBolsin`

Atributos nuevos:

| Visib. | Atributo |
| ------ | -------- |
| `-` | **`opcionSeleccionada: Integer`** |
| `-` | **`nrosDocSeleccionados: Integer[]`** |

> El atributo `CMOrigen` está implementado como `String`. En el diagrama figura como `ComisionMedica`: unificar en uno de los dos.

Métodos:

| Visib. | Firma | Cambio |
| ------ | ----- | ------ |
| `+` | `tomarSelecRecepcion(opcion: int): void` | ahora **guarda la opción**. Con 1 pide confirmación; con 2 o 3 llama a `pantalla.solicitarSeleccionDocumentacion()` |
| `+` | **`tomarSeleccionDocumentacion(nrosDoc: Integer[]): void`** | nuevo: valida que se haya elegido algo y que pertenezca al bolsín, lo guarda y pide confirmación |
| `+` | `tomarConfirmacion(respuesta: String): void` | con `"S"` registra; con otra respuesta (A6) limpia la opción y llama a `pantalla.solicitarOpcionRecepcion()` |
| `-` | `registrarRecepcionBolsin(): void` | ahora decide según `opcionSeleccionada` a cuál de los tres métodos de `Bolsin` llamar |
| `-` | `finCU(): void` | también limpia `opcionSeleccionada` y `nrosDocSeleccionados` |

#### `PantallaAdministracionBolsines`

| Visib. | Firma | Cambio |
| ------ | ----- | ------ |
| `+` | **`solicitarSeleccionDocumentacion(): void`** | nuevo (A3/A4) |
| `+` | **`tomarSeleccionDocumentacion(nrosDoc: Integer[]): void`** | nuevo (A3/A4) |
| `+` | `solicitarOpcionRecepcion(): void` | ahora también se usa en A6 |
| `+` | `mostrarDatosRemitos(datosRemitos: String[]): void` | ahora muestra también el número de documentación |
| `+` | **`mostrarMensajeError(error: Exception)`** | nuevo: devuelve al front los errores de validación del gestor. Es un detalle técnico de Spring y es opcional ponerlo en el diagrama |

> En el código, los métodos `tomarX` de la pantalla reciben el JSON del front (`Map`) porque son endpoints REST. En el diagrama conviene dejar el parámetro de dominio (`nrosDoc: Integer[]`, `opcion: int`, etc.).

---

## 2. Diagrama de secuencia de diseño (flujo básico)

La consigna pide modelar "la parte del escenario que se rediseña debido a la aplicación del patrón". El flujo básico cambia en estos puntos:

### 2.1 Búsqueda de bolsines enviados (pasos 3–4)

**Antes:**

```
:Gestor -> :Bolsin : buscarBolsinEnviado(cm)
  :Bolsin -> :Bolsin : esTuCmDestino(cm)
  loop mientras haya cambios de estado
    :Bolsin -> :CambioEstadoBolsin : sosActual()
    :Bolsin -> :CambioEstadoBolsin : sosEnviado()
      :CambioEstadoBolsin -> :Estado : esEnviado()
```

**Ahora:**

```
:Gestor -> :Bolsin : buscarBolsinEnviado(cm)
  :Bolsin -> :Bolsin : esTuCmDestino(cm)
  :Bolsin -> estadoActual:EstadoBolsin : esEnviado()
```

Se borran el loop sobre `CambioEstadoBolsin` y los mensajes `sosActual()` / `sosEnviado()` de este tramo.

### 2.2 Paso donde se obtiene la CM del empleado

- `:Gestor -> :Empleado : getCm()`. Devuelve el objeto `ComisionMedica`, no `getNombre()`.

### 2.3 Paso 6 (información de los remitos)

- `:Remito -> :DetalleRemito : obtenerDocumentacion()`. Renombrar desde `getDocumentacion()`.

### 2.4 Selección de opción (pasos 7–10)

```
:EB -> :Pantalla : tomarSelecRecepcion(1)
:Pantalla -> :Gestor : tomarSelecRecepcion(1)
  :Gestor : guarda opcionSeleccionada = 1        <- nuevo (puede ir como nota)
:Gestor -> :Pantalla : solicitarConfirmacion()
```

### 2.5 Registro de la recepción (paso 11)

Los mensajes ya estaban, pero se corrigen parámetros y nombres:

```
:Gestor -> :Bolsin : registrarRecepcion(fechaActual, empleadoLogueado)
  :Bolsin -> estadoActual:BolsinEnviado : registrarRecepcion(this, fechaActual, empleadoLogueado, cambioEstadoBolsin)
    :BolsinEnviado -> :BolsinEnviado : cambiarEstado(fechaActual, cambioEstadoBolsin)
    :BolsinEnviado -> :BolsinEnviado : crearEstado()
      create :BolsinRecibidoCMDestino
    :BolsinEnviado -> :BolsinEnviado : crearCambioEstado(fechaActual, nuevoEstado, empleadoLogueado, bolsin)   <- se agregan empleado y bolsín
      create :CambioEstadoBolsin
    :BolsinEnviado -> :Bolsin : agregarCambioEstado(nuevoCambio)
    :BolsinEnviado -> :Bolsin : setEstado(nuevoEstado)
  loop mientras haya remitos
    :Bolsin -> :Remito : aceptarRemito(fechaActual, empleadoLogueado)
      :Remito -> estado:RemitoEnviado : aceptarRemito(this)
        create :RemitoRecibidoYAceptado
        :RemitoEnviado -> :Remito : setEstado(nuevoEstado)
      loop mientras haya DETALLES          <- antes decía "mientras haya documentación"
        :Remito -> :DetalleRemito : aceptarDocumentacion(fechaActual, empleadoLogueado)
          :DetalleRemito -> :Documentacion : aceptarDocumentacion(fechaActual, empleadoLogueado)
            :Documentacion -> estadoActual:DocEnBolsinEnviado : aceptarDocumentacion(this, fechaActual, empleadoLogueado, cambioEstado)
              :DocEnBolsinEnviado -> :DocEnBolsinEnviado : cambiarEstado(fechaActual, cambioEstado)
              :DocEnBolsinEnviado -> :DocEnBolsinEnviado : crearEstadoRecibidaYAceptada()     <- antes crearEstado()
                create :DocRecibidoYAceptado
              :DocEnBolsinEnviado -> :DocEnBolsinEnviado : registrarNuevoEstado(this doc, fechaActual, estado, empleadoLogueado)   <- nuevo
                :DocEnBolsinEnviado -> :DocEnBolsinEnviado : crearCambioEstado(fechaActual, estado, empleadoLogueado, doc)
                  create :CambioEstadoDoc
                :DocEnBolsinEnviado -> :Documentacion : agregarCambioEstado(nuevoCambio)
                :DocEnBolsinEnviado -> :Documentacion : setEstado(estado)
```

Si no quieren mostrar `registrarNuevoEstado`, pueden dejar los tres mensajes de adentro (`crearCambioEstado`, `agregarCambioEstado`, `setEstado`) colgando directamente de `aceptarDocumentacion`. Lo importante es que el diagrama coincida con el código, así que en ese caso conviene borrar `registrarNuevoEstado` del código.

---

## 3. Secuencias de los flujos alternativos (opcional)

No es obligatorio diagramarlos, pero sirven para la defensa porque muestran el polimorfismo del patrón: **el mismo estado `DocEnBolsinEnviado` resuelve tres transiciones distintas**.

### 3.1 A3 – No se recibe toda la documentación (opción 2)

```
:EB -> :Pantalla : tomarSelecRecepcion(2)
:Pantalla -> :Gestor : tomarSelecRecepcion(2)
:Gestor -> :Pantalla : solicitarSeleccionDocumentacion()
:EB -> :Pantalla : tomarSeleccionDocumentacion(nrosDoc)
:Pantalla -> :Gestor : tomarSeleccionDocumentacion(nrosDoc)
  loop por cada nroDoc
    :Gestor -> :Bolsin : contieneDocumentacion(nroDoc)
:Gestor -> :Pantalla : solicitarConfirmacion()
:EB -> :Pantalla : tomarConfirmacion("S")
:Pantalla -> :Gestor : tomarConfirmacion("S")
:Gestor -> :Bolsin : registrarRecepcionConFaltantes(fechaActual, empleadoLogueado, nrosDoc)
  :Bolsin -> estadoActual:BolsinEnviado : registrarRecepcion(...)        (igual que el flujo básico)
  loop mientras haya remitos
    :Bolsin -> :Remito : registrarFaltantes(fechaActual, empleadoLogueado, nrosDoc)
      :Remito -> :Remito : actualizarEstadoSegun(nrosDoc)
        alt [el remito tiene documentación faltante]
          :Remito -> estado:RemitoEnviado : aceptarParcialmente(this)
            create :RemitoRecibidoYAceptadoParcialmente
        else
          :Remito -> estado:RemitoEnviado : aceptarRemito(this)
      loop mientras haya detalles
        :Remito -> :DetalleRemito : contieneDocumentacion(nrosDoc)
        alt [es faltante]
          :DetalleRemito -> :Documentacion : marcarNoRecibida(fechaActual, empleadoLogueado)
            :Documentacion -> estadoActual:DocEnBolsinEnviado : marcarNoRecibida(this, fechaActual, empleadoLogueado, cambioEstado)
              cambiarEstado(...) / crearEstadoNoRecibida() -> create :DocNoRecibida / registrarNuevoEstado(...)
        else
          :DetalleRemito -> :Documentacion : aceptarDocumentacion(...)   (igual que el flujo básico)
```

### 3.2 A4 – Documentación que no corresponde al destino (opción 3)

Es igual a A3, con estos cambios:

- `tomarSelecRecepcion(3)`
- `:Gestor -> :Bolsin : registrarRecepcionConRechazos(fechaActual, empleadoLogueado, nrosDoc)`
- `:Bolsin -> :Remito : registrarRechazos(...)`
- En el `alt`: `rechazarDocumentacion(...)` → `estadoActual:DocEnBolsinEnviado.rechazarDocumentacion(...)` → `crearEstadoRecibidaYRechazada()` → create `:DocRecibidaYRechazada`

### 3.3 A6 – El EB no confirma

```
:EB -> :Pantalla : tomarConfirmacion("N")
:Pantalla -> :Gestor : tomarConfirmacion("N")
  :Gestor : limpia opcionSeleccionada y nrosDocSeleccionados
:Gestor -> :Pantalla : solicitarOpcionRecepcion()
```

---

## 4. Máquina de estados de Documentación

Si la máquina de estados de la entrega 1 no las tiene, agregar o verificar estas transiciones, que ahora están implementadas:

| Desde | Hacia | Método | Caso de uso / condición |
| ----- | ----- | ------ | ----------------------- |
| En Bolsín Enviado | Recibida y Aceptada | `aceptarDocumentacion()` | CU 28 – opción 1, o documentación no marcada en A3/A4 |
| En Bolsín Enviado | No Recibida | `marcarNoRecibida()` | CU 28 – A3 [documentación marcada como faltante] |
| En Bolsín Enviado | Recibida y Rechazada | `rechazarDocumentacion()` | CU 28 – A4 [documentación marcada como que no corresponde] |

---

## 5. Decisión a confirmar con el tutor

En A4 (documentación rechazada), el remito que la contiene queda en **Recibido y Aceptado Parcial**, igual que en A3. El ERS solo menciona el estado parcial explícitamente para el caso de documentación no recibida.
