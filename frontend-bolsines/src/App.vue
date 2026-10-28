<!--
  PantallaAdministracionBolsines.vue   (Boundary - BCE)

  Equivalencia con la clase Java PantallaAdministracionBolsines:
  ┌──────────────────────────────────┬───────────────────────────────────────────────┐
  │ Método Java                      │ Aquí                                          │
  ├──────────────────────────────────┼───────────────────────────────────────────────┤
  │ opcionRegRecepBolsin()           │ onMounted -> opcionRegRecepBolsin()           │
  │ habilitarVentana()               │ el propio template (ventana habilitada)       │
  │ mostrarCMEmpLogueado()           │ chip de CM en topbar + título de la tabla     │
  │ mostraCmOrigen()/mostrarNumPre.. │ columnas "CM origen" y "Nº precinto"          │
  │ solicitarSeleccionBolsin()       │ tabla de bolsines enviados (click en fila)    │
  │ tomarSeleccionBolsin()           │ tomarSeleccionBolsin(bolsin)                  │
  │ mostrarDatosRemitos()            │ panel de detalle: remitos y documentación     │
  │ solicitarOpcionRecepcion()       │ tarjetas de opciones 1..4                     │
  │ tomarSelecRecepcion(op)          │ tomarSelecRecepcion(op)                       │
  │ solicitarConfirmacion()          │ modal de confirmación                         │
  │ tomarConfirmacion("S"/"N")       │ tomarConfirmacion('S' | 'N')                  │
  │ mostrarMensajeExito()            │ banner/pantalla de éxito + toast              │
  └──────────────────────────────────┴───────────────────────────────────────────────┘

  Esta vista NO contiene lógica de negocio: solo muestra datos y envía las
  acciones del usuario al backend (GestorRecepcionBolsin, expuesto por un
  @RestController de Spring Boot). Los cambios de estado de bolsín / remitos /
  documentación los hace el gestor.

  Contrato REST esperado (prefijo /api/recepcion-bolsin):
    POST /iniciar        {}                       -> { comisionMedica:{nombre}, bolsines:[{id,cmOrigen,nroPrecinto,fechaEnvio,cantRemitos}] }
    POST /seleccion      { bolsinId }             -> { remitos:[{nroRemito, documentos:[{asunto,tipoDocumento}]}] }
    POST /opcion         { opcion }               -> { requiereConfirmacion:true }
    POST /confirmacion   { respuesta:'S'|'N' }    -> { exito:boolean, mensaje:string }

  Dependencias:  npm i lucide-vue-next   (Tailwind ya configurado en el proyecto)
  Para trabajar sin backend dejar USE_MOCK = true; para conectar con Spring Boot, ponerlo en false.
-->

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  LayoutDashboard, FileText, Receipt, PackagePlus, PackageCheck, PackageOpen, MapPin,
  BarChart3, Building2, Tags, Users, Search, Bell, Check, X, ChevronRight, ShieldCheck,
  Truck, Inbox, Lock, Loader2, ArrowLeft
} from 'lucide-vue-next'

/* ───────────────────────── Capa de acceso al backend ───────────────────────── */
const USE_MOCK = false
// const BASE_URL = '/api/recepcion-bolsin'
const BASE_URL = 'http://localhost:8080/api/recepcion-bolsin'
const wait = (ms) => new Promise((r) => setTimeout(r, ms))

async function http(path, body = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // el gestor mantiene la sesión del CU
    body: JSON.stringify(body)
  })
  if (!res.ok) throw new Error(`Error ${res.status}`)
  return res.json()
}

// Simula las respuestas del gestor mientras no hay backend (solo datos, sin reglas de negocio)
const MOCK = {
  async '/iniciar'() {
    await wait(500)
    return {
      comisionMedica: { nombre: 'Comisión Médica Córdoba' },
      bolsines: [
        { id: 1042, cmOrigen: 'Comisión Médica Mendoza', nroPrecinto: 508231, fechaEnvio: '2026-10-23', cantRemitos: 2 },
        { id: 1047, cmOrigen: 'Comisión Médica Rosario', nroPrecinto: 508377, fechaEnvio: '2026-10-23', cantRemitos: 1 },
        { id: 1051, cmOrigen: 'Comisión Médica Central (C.A.B.A.)', nroPrecinto: 509012, fechaEnvio: '2026-10-24', cantRemitos: 3 },
        { id: 1055, cmOrigen: 'Comisión Médica Villa María', nroPrecinto: 509144, fechaEnvio: '2026-10-24', cantRemitos: 1 },
        { id: 1060, cmOrigen: 'Comisión Médica Paraná', nroPrecinto: 509560, fechaEnvio: '2026-10-26', cantRemitos: 2 }
      ]
    }
  },
  async '/seleccion'() {
    await wait(400)
    return {
      remitos: [
        { nroRemito: 3318, documentos: [
          { asunto: 'Dictamen médico - Exp. 44.120/26', tipoDocumento: 'Dictamen' },
          { asunto: 'Estudio de columna lumbosacra', tipoDocumento: 'Estudio médico' }
        ] },
        { nroRemito: 3321, documentos: [
          { asunto: 'Notificación al empleador', tipoDocumento: 'Carta documento' }
        ] }
      ]
    }
  },
  async '/opcion'() { await wait(250); return { requiereConfirmacion: true } },
  async '/confirmacion'(b) {
    await wait(700)
    return b.respuesta === 'S'
      ? { exito: true, mensaje: 'Recepción registrada correctamente.' }
      : { exito: false, mensaje: 'Registro cancelado.' }
  }
}
const call = (path, body) => (USE_MOCK ? MOCK[path](body) : http(path, body))

/* ───────────────────────── Estado de la vista ───────────────────────── */
const cmLogueada = ref('')
const bolsines = ref([])
const bolsinElegido = ref(null)
const remitos = ref([])
const opcionElegida = ref(null)

const cargandoLista = ref(true)
const cargandoDetalle = ref(false)
const procesando = ref(false)
const mostrarModal = ref(false)
const recepcionExitosa = ref(false)
const errorMsg = ref('')
const toast = ref('')

const filtroPrecinto = ref('')
const filtroOrigen = ref('')

// Filtrado solo de presentación (CU 28, paso 4)
const bolsinesFiltrados = computed(() =>
  bolsines.value.filter(
    (b) =>
      String(b.nroPrecinto).includes(filtroPrecinto.value.trim()) &&
      b.cmOrigen.toLowerCase().includes(filtroOrigen.value.trim().toLowerCase())
  )
)

const totalDocumentos = computed(() => remitos.value.reduce((n, r) => n + r.documentos.length, 0))

const opciones = [
  { id: 1, titulo: 'El contenido del bolsín es igual al registrado', detalle: 'Remitos y documentación pasan a Recibida y Aceptada.', activa: true },
  { id: 2, titulo: 'No se recibe toda la documentación asociada a los remitos', detalle: 'La documentación faltante queda como No recibida.', activa: false },
  { id: 3, titulo: 'Existe documentación que no corresponde al destino', detalle: 'Se marca como Recibida y Rechazada.', activa: false },
  { id: 4, titulo: 'La documentación se debe redirigir a otra área', detalle: 'Solo para la Comisión Médica Central.', activa: false }
]

/* ───────────────────────── Acciones (equivalentes a la Boundary Java) ───────────────────────── */
async function opcionRegRecepBolsin() {
  cargandoLista.value = true
  errorMsg.value = ''
  try {
    const data = await call('/iniciar')
    cmLogueada.value = data.comisionMedica.nombre // mostrarCMEmpLogueado
    bolsines.value = data.bolsines // solicitarSeleccionBolsin
  } catch (e) {
    errorMsg.value = 'No se pudo conectar con el servidor. Intentá nuevamente.'
  } finally {
    cargandoLista.value = false
  }
}

async function tomarSeleccionBolsin(bolsin) {
  if (procesando.value) return
  bolsinElegido.value = bolsin
  opcionElegida.value = null
  remitos.value = []
  cargandoDetalle.value = true
  try {
    const data = await call('/seleccion', { bolsinId: bolsin.id })
    remitos.value = data.remitos // mostrarDatosRemitos
  } catch (e) {
    errorMsg.value = 'No se pudieron obtener los remitos del bolsín.'
  } finally {
    cargandoDetalle.value = false
  }
}

async function tomarSelecRecepcion(opcion) {
  opcionElegida.value = opcion
  try {
    const data = await call('/opcion', { opcion })
    if (data.requiereConfirmacion) mostrarModal.value = true // solicitarConfirmacion
  } catch (e) {
    errorMsg.value = 'No se pudo registrar la opción seleccionada.'
    opcionElegida.value = null
  }
}

async function tomarConfirmacion(respuesta) {
  mostrarModal.value = false
  if (respuesta === 'N') {
    await call('/confirmacion', { respuesta })
    opcionElegida.value = null // A6: no confirma, vuelve a elegir opción
    return
  }
  procesando.value = true
  try {
    const data = await call('/confirmacion', { respuesta })
    if (data.exito) mostrarMensajeExito(data.mensaje)
  } catch (e) {
    errorMsg.value = 'No se pudo completar el registro de la recepción.'
  } finally {
    procesando.value = false
  }
}

function mostrarMensajeExito(mensaje) {
  recepcionExitosa.value = true
  toast.value = mensaje
  setTimeout(() => (toast.value = ''), 4000)
}

async function registrarOtra() {
  recepcionExitosa.value = false
  bolsinElegido.value = null
  remitos.value = []
  opcionElegida.value = null
  await opcionRegRecepBolsin()
}

function volverAlListado() {
  bolsinElegido.value = null
  remitos.value = []
  opcionElegida.value = null
}

const fmtFecha = (iso) => new Date(iso + 'T00:00:00').toLocaleDateString('es-AR')

onMounted(opcionRegRecepBolsin)

/* ───────────────────────── Elementos decorativos (hardcodeados) ───────────────────────── */
const menu = [
  { grupo: 'General', items: [{ t: 'Panel general', i: LayoutDashboard }] },
  { grupo: 'Documentación', items: [{ t: 'Documentación', i: FileText }, { t: 'Remitos', i: Receipt }] },
  {
    grupo: 'Bolsines',
    items: [
      { t: 'Generar bolsín', i: PackagePlus },
      { t: 'Retiro de bolsines', i: Truck },
      { t: 'Recepción de bolsines', i: PackageCheck, activo: true },
      { t: 'Seguimiento en mapa', i: MapPin }
    ]
  },
  { grupo: 'Reportes', items: [{ t: 'Bolsines en tránsito', i: PackageOpen }, { t: 'Estadísticas', i: BarChart3 }] },
  { grupo: 'Administración', items: [{ t: 'Comisiones médicas', i: Building2 }, { t: 'Tipos de documento', i: Tags }, { t: 'Usuarios', i: Users }] }
]
const kpis = [
  { t: 'Para recibir', v: '5', n: 'bolsines enviados a tu CM' },
  { t: 'En tránsito', v: '12', n: 'saliendo de tu CM' },
  { t: 'Remitos creados', v: '27', n: 'sin bolsín asignado: 4' },
  { t: 'Recibidos este mes', v: '63', n: '3 con diferencias' }
]
</script>

<template>
  <div class="flex h-screen bg-[#F3F5F8] text-slate-800 antialiased">
    <!-- ═════════ Sidebar (decorativa) ═════════ -->
    <aside class="hidden lg:flex w-64 shrink-0 flex-col bg-[#10233A] text-slate-300">
      <div class="flex items-center gap-3 px-5 h-16 border-b border-white/10">
        <div class="grid place-items-center w-9 h-9 rounded-md bg-[#F2B705] text-[#10233A]">
          <PackageCheck class="w-5 h-5" />
        </div>
        <div class="leading-tight">
          <p class="font-semibold text-white">Bolsines</p>
          <p class="text-xs text-slate-400">Organismo Controlador</p>
        </div>
      </div>

      <nav class="flex-1 overflow-y-auto px-3 py-4 space-y-5">
        <div v-for="g in menu" :key="g.grupo">
          <p class="px-2 mb-1 text-xs font-medium text-slate-500">{{ g.grupo }}</p>
          <a
            v-for="it in g.items"
            :key="it.t"
            href="#"
            @click.prevent
            :class="[
              'flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors',
              it.activo ? 'bg-white/10 text-white border-l-2 border-[#F2B705]' : 'hover:bg-white/5'
            ]"
          >
            <component :is="it.i" class="w-4 h-4" /> {{ it.t }}
          </a>
        </div>
      </nav>

      <div class="p-4 border-t border-white/10 flex items-center gap-3">
        <div class="grid place-items-center w-9 h-9 rounded-full bg-[#1F5FA8] text-white text-sm font-semibold">LF</div>
        <div class="leading-tight min-w-0">
          <p class="text-sm text-white truncate">Lucía Fernández</p>
          <p class="text-xs text-slate-400 truncate">Encargada de Bolsines</p>
        </div>
      </div>
    </aside>

    <!-- ═════════ Contenido ═════════ -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Topbar -->
      <header class="h-16 shrink-0 bg-white border-b border-slate-200 flex items-center gap-4 px-6">
        <div class="relative w-full max-w-md">
          <Search class="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
          <input disabled placeholder="Buscar remito, documentación o bolsín…"
            class="w-full h-9 pl-9 pr-3 rounded-md bg-slate-100 text-sm placeholder:text-slate-400 cursor-not-allowed" />
        </div>
        <div class="ml-auto flex items-center gap-4">
          <span v-if="cmLogueada" class="inline-flex items-center gap-2 rounded-full bg-[#E8F0FA] text-[#1F5FA8] text-sm px-3 py-1">
            <Building2 class="w-4 h-4" /> {{ cmLogueada }}
          </span>
          <button class="relative text-slate-500" @click.prevent>
            <Bell class="w-5 h-5" />
            <span class="absolute -top-0.5 -right-0.5 w-2 h-2 rounded-full bg-[#F2B705]"></span>
          </button>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto p-6 space-y-6">
        <!-- Título -->
        <div class="flex flex-wrap items-end justify-between gap-3">
          <div>
            <p class="text-sm text-slate-500">Bolsines <ChevronRight class="inline w-3.5 h-3.5" /> Recepción</p>
            <h1 class="text-2xl font-semibold text-[#10233A]">Registrar recepción de bolsín</h1>
          </div>
          <div class="flex gap-2">
            <button disabled class="h-9 px-4 rounded-md border border-slate-300 bg-white text-sm text-slate-400 cursor-not-allowed">Exportar</button>
            <button disabled class="h-9 px-4 rounded-md border border-slate-300 bg-white text-sm text-slate-400 cursor-not-allowed">Ver historial</button>
          </div>
        </div>

        <!-- KPIs decorativos -->
        <section class="grid grid-cols-2 xl:grid-cols-4 gap-4">
          <div v-for="k in kpis" :key="k.t" class="bg-white rounded-lg border border-slate-200 p-4">
            <p class="text-sm text-slate-500">{{ k.t }}</p>
            <p class="mt-1 text-3xl font-semibold text-[#10233A] tabular-nums">{{ k.v }}</p>
            <p class="text-xs text-slate-400 mt-1">{{ k.n }}</p>
          </div>
        </section>

        <!-- Error -->
        <div v-if="errorMsg" class="flex items-start justify-between gap-3 rounded-md border border-red-200 bg-red-50 text-red-800 px-4 py-3 text-sm" role="alert">
          <span>{{ errorMsg }}</span>
          <button @click="errorMsg = ''" aria-label="Cerrar aviso"><X class="w-4 h-4" /></button>
        </div>

        <!-- ═════════ ÉXITO (mostrarMensajeExito) ═════════ -->
        <section v-if="recepcionExitosa" class="bg-white rounded-lg border border-emerald-200 p-10 text-center">
          <div class="mx-auto grid place-items-center w-16 h-16 rounded-full bg-emerald-100 text-emerald-600">
            <Check class="w-8 h-8" />
          </div>
          <h2 class="mt-4 text-xl font-semibold text-[#10233A]">Bolsín recibido</h2>
          <p class="mt-1 text-slate-600 max-w-md mx-auto">
            El bolsín Nº {{ bolsinElegido?.id }} (precinto {{ bolsinElegido?.nroPrecinto }}) quedó en estado
            <strong>Recibido en CM destino</strong>. Los remitos y la documentación figuran como recibidos y aceptados.
            Se notificó por correo a la comisión médica de origen.
          </p>
          <button @click="registrarOtra"
            class="mt-6 h-10 px-5 rounded-md bg-[#1F5FA8] text-white text-sm font-medium hover:bg-[#194f8c] focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-[#1F5FA8]">
            Registrar otra recepción
          </button>
        </section>

        <template v-else>
          <!-- ═════════ PASO 1: Listado de bolsines enviados ═════════ -->
          <section v-if="!bolsinElegido" class="bg-white rounded-lg border border-slate-200">
            <div class="p-4 border-b border-slate-200 flex flex-wrap items-center gap-3">
              <div class="mr-auto">
                <h2 class="font-semibold text-[#10233A]">Bolsines enviados a {{ cmLogueada || 'tu comisión médica' }}</h2>
                <p class="text-sm text-slate-500">Elegí el bolsín que tenés en mano para controlar su contenido.</p>
              </div>
              <input v-model="filtroPrecinto" inputmode="numeric" placeholder="Nº de precinto"
                class="h-9 w-40 px-3 rounded-md border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-[#1F5FA8]" />
              <input v-model="filtroOrigen" placeholder="CM de origen"
                class="h-9 w-52 px-3 rounded-md border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-[#1F5FA8]" />
            </div>

            <!-- tabs decorativos -->
            <div class="px-4 pt-3 flex gap-5 text-sm border-b border-slate-200">
              <span class="pb-2 border-b-2 border-[#1F5FA8] text-[#1F5FA8] font-medium">Pendientes de recepción</span>
              <span class="pb-2 text-slate-400 cursor-not-allowed">Recibidos hoy</span>
              <span class="pb-2 text-slate-400 cursor-not-allowed">Todos</span>
            </div>

            <div v-if="cargandoLista" class="flex items-center justify-center gap-2 py-16 text-slate-500">
              <Loader2 class="w-5 h-5 animate-spin" /> Buscando bolsines enviados a esta comisión médica…
            </div>

            <div v-else-if="!bolsinesFiltrados.length" class="py-16 text-center text-slate-500">
              <Inbox class="w-8 h-8 mx-auto mb-2 text-slate-300" />
              No hay bolsines en estado Enviado que coincidan con los filtros.
            </div>

            <div v-else class="overflow-x-auto">
              <table class="w-full text-sm">
                <thead class="text-left text-slate-500 bg-slate-50">
                  <tr>
                    <th class="px-4 py-2.5 font-medium">Bolsín</th>
                    <th class="px-4 py-2.5 font-medium">CM de origen</th>
                    <th class="px-4 py-2.5 font-medium">Nº de precinto</th>
                    <th class="px-4 py-2.5 font-medium">Enviado</th>
                    <th class="px-4 py-2.5 font-medium">Remitos</th>
                    <th class="px-4 py-2.5 font-medium">Estado</th>
                    <th class="px-4 py-2.5"></th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr v-for="b in bolsinesFiltrados" :key="b.id"
                    class="hover:bg-[#F6F9FD] cursor-pointer" tabindex="0"
                    @click="tomarSeleccionBolsin(b)" @keydown.enter="tomarSeleccionBolsin(b)">
                    <td class="px-4 py-3 font-medium tabular-nums">#{{ b.id }}</td>
                    <td class="px-4 py-3">{{ b.cmOrigen }}</td>
                    <td class="px-4 py-3 tabular-nums">{{ b.nroPrecinto }}</td>
                    <td class="px-4 py-3 tabular-nums">{{ fmtFecha(b.fechaEnvio) }}</td>
                    <td class="px-4 py-3 tabular-nums">{{ b.cantRemitos }}</td>
                    <td class="px-4 py-3">
                      <span class="inline-flex rounded-full bg-amber-100 text-amber-800 px-2.5 py-0.5 text-xs font-medium">Enviado</span>
                    </td>
                    <td class="px-4 py-3 text-right">
                      <span class="inline-flex items-center gap-1 text-[#1F5FA8] font-medium">Controlar <ChevronRight class="w-4 h-4" /></span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <!-- ═════════ PASO 2: Detalle + opciones de recepción ═════════ -->
          <section v-else class="grid xl:grid-cols-5 gap-6">
            <!-- Remitos y documentación -->
            <div class="xl:col-span-3 bg-white rounded-lg border border-slate-200">
              <div class="p-4 border-b border-slate-200 flex items-start gap-3">
                <button @click="volverAlListado" class="mt-0.5 text-slate-500 hover:text-slate-800" aria-label="Volver al listado">
                  <ArrowLeft class="w-5 h-5" />
                </button>
                <div>
                  <h2 class="font-semibold text-[#10233A]">Bolsín #{{ bolsinElegido.id }}</h2>
                  <p class="text-sm text-slate-500">
                    Origen: {{ bolsinElegido.cmOrigen }} · Precinto {{ bolsinElegido.nroPrecinto }}
                  </p>
                </div>
                <span class="ml-auto inline-flex items-center gap-1 rounded-full bg-amber-100 text-amber-800 px-2.5 py-0.5 text-xs font-medium">
                  <Lock class="w-3 h-3" /> Enviado
                </span>
              </div>

              <div v-if="cargandoDetalle" class="flex items-center justify-center gap-2 py-16 text-slate-500">
                <Loader2 class="w-5 h-5 animate-spin" /> Cargando remitos…
              </div>

              <div v-else class="p-4 space-y-4">
                <p class="text-sm text-slate-600">
                  Contenido registrado: {{ remitos.length }} remito(s) y {{ totalDocumentos }} documento(s).
                  Compará con lo que hay físicamente en el bolsín.
                </p>
                <div v-for="r in remitos" :key="r.nroRemito" class="rounded-md border border-slate-200 overflow-hidden">
                  <div class="flex items-center gap-2 bg-slate-50 px-3 py-2 text-sm font-medium text-[#10233A]">
                    <Receipt class="w-4 h-4 text-[#1F5FA8]" /> Remito Nº {{ r.nroRemito }}
                  </div>
                  <table class="w-full text-sm">
                    <thead class="text-left text-slate-500">
                      <tr><th class="px-3 py-2 font-medium">Asunto</th><th class="px-3 py-2 font-medium">Tipo de documento</th></tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100">
                      <tr v-for="(d, i) in r.documentos" :key="i">
                        <td class="px-3 py-2">{{ d.asunto }}</td>
                        <td class="px-3 py-2 text-slate-600">{{ d.tipoDocumento }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            <!-- Opciones de recepción -->
            <div class="xl:col-span-2 bg-white rounded-lg border border-slate-200 self-start">
              <div class="p-4 border-b border-slate-200">
                <h2 class="font-semibold text-[#10233A]">Resultado del control</h2>
                <p class="text-sm text-slate-500">Elegí la situación que corresponde.</p>
              </div>
              <div class="p-4 space-y-3">
                <button
                  v-for="o in opciones" :key="o.id"
                  :disabled="!o.activa || cargandoDetalle || procesando"
                  @click="tomarSelecRecepcion(o.id)"
                  :class="[
                    'w-full text-left rounded-md border p-3 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-[#1F5FA8]',
                    o.activa
                      ? (opcionElegida === o.id ? 'border-[#1F5FA8] bg-[#E8F0FA]' : 'border-slate-300 hover:border-[#1F5FA8] hover:bg-[#F6F9FD]')
                      : 'border-slate-200 bg-slate-50 text-slate-400 cursor-not-allowed'
                  ]"
                >
                  <div class="flex items-start gap-3">
                    <span :class="['grid place-items-center w-6 h-6 rounded-full text-xs font-semibold shrink-0',
                      o.activa ? 'bg-[#1F5FA8] text-white' : 'bg-slate-200 text-slate-400']">{{ o.id }}</span>
                    <span>
                      <span class="block text-sm font-medium">{{ o.titulo }}</span>
                      <span class="block text-xs mt-0.5">{{ o.detalle }}</span>
                    </span>
                  </div>
                </button>
                <p v-if="procesando" class="flex items-center gap-2 text-sm text-slate-500">
                  <Loader2 class="w-4 h-4 animate-spin" /> Registrando recepción…
                </p>
              </div>
            </div>
          </section>
        </template>
      </main>
    </div>

    <!-- ═════════ Modal de confirmación (solicitarConfirmacion) ═════════ -->
    <div v-if="mostrarModal" class="fixed inset-0 z-50 grid place-items-center bg-[#10233A]/60 p-4"
      role="dialog" aria-modal="true" aria-labelledby="titulo-confirmacion" @keydown.esc="tomarConfirmacion('N')">
      <div class="w-full max-w-md rounded-lg bg-white shadow-xl">
        <div class="p-6">
          <div class="grid place-items-center w-11 h-11 rounded-full bg-[#E8F0FA] text-[#1F5FA8]">
            <ShieldCheck class="w-6 h-6" />
          </div>
          <h3 id="titulo-confirmacion" class="mt-4 text-lg font-semibold text-[#10233A]">¿Confirmás la recepción del bolsín?</h3>
          <p class="mt-2 text-sm text-slate-600">
            Vas a registrar que el contenido del bolsín #{{ bolsinElegido?.id }} es igual al registrado.
            Los remitos y la documentación pasarán a <strong>Recibida y Aceptada</strong> a tu nombre.
          </p>
        </div>
        <div class="flex justify-end gap-2 px-6 py-4 bg-slate-50 rounded-b-lg">
          <button @click="tomarConfirmacion('N')"
            class="h-9 px-4 rounded-md border border-slate-300 bg-white text-sm hover:bg-slate-100 focus:outline-none focus-visible:ring-2 focus-visible:ring-slate-400">
            No, volver
          </button>
          <button @click="tomarConfirmacion('S')" autofocus
            class="h-9 px-4 rounded-md bg-[#1F5FA8] text-white text-sm font-medium hover:bg-[#194f8c] focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-[#1F5FA8]">
            Sí, confirmar recepción
          </button>
        </div>
      </div>
    </div>

    <!-- Toast de éxito -->
    <div v-if="toast" class="fixed bottom-6 right-6 z-50 flex items-center gap-2 rounded-md bg-emerald-600 text-white text-sm px-4 py-3 shadow-lg" role="status">
      <Check class="w-4 h-4" /> {{ toast }}
    </div>
  </div>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@400;500;600&display=swap');
body { font-family: 'IBM Plex Sans', ui-sans-serif, system-ui, sans-serif; }
</style>
