# Bolsines – CU 28: Registrar recepción de bolsín

Backend (Spring Boot + JPA + MariaDB) y frontend (Vue + Vite) del caso de uso 28, implementado con el patrón State.

## Requisitos

| Herramienta | Versión                              |
| ----------- | ------------------------------------ |
| Java (JDK)  | 17 o superior (probado con 25)       |
| Maven       | 3.9 o superior                       |
| Node.js     | 20.19 o superior (probado con 20.20) |
| Docker      | Docker Desktop corriendo             |

## Estructura

```
bolsines/
├── src/                  Backend (Spring Boot, puerto 8080)
├── frontend-bolsines/    Frontend (Vue + Vite, puerto 5173)
├── referencias/          Consignas, ERS y diagramas
├── .env                  Contraseña de la base (no se versiona)
└── pom.xml
```

## Ejecución desde cero

Todos los comandos son para PowerShell, parado en la raíz del proyecto.

### 1. Crear el archivo `.env` duplicando .env.example

En la raíz, con la contraseña que le vas a poner a la base (la misma del paso 2):

```
DB_PASSWORD=tu_contraseña
```

### 2. Crear y levantar la base de datos (MariaDB en Docker)

El puerto 3306 tiene que estar libre. Si tenés MySQL, Laragon u otro servicio usándolo, detenelo antes.

Solo la primera vez, crear el contenedor:

```powershell
docker run -d --name bolsines-mariadb -p 3306:3306 -e MARIADB_ROOT_PASSWORD=tu_contraseña -e MARIADB_DATABASE=bolsines_db -v bolsines-data:/var/lib/mysql mariadb:latest
```

Las veces siguientes alcanza con:

```powershell
docker start bolsines-mariadb
```

Verificá que esté corriendo:

```powershell
docker ps --filter name=bolsines-mariadb
```

### 3. Levantar el backend

```powershell
mvn spring-boot:run
```

Esperá a ver `Started Main`. En el primer arranque Hibernate crea las tablas (`ddl-auto=update`) y se cargan los datos de prueba (bolsín 1042, remitos 3318 y 3321, documentaciones 1, 2 y 3). El seed solo corre si la base está vacía.

### 4. Levantar el frontend

En otra terminal:

```powershell
cd frontend-bolsines
npm install
npm run dev
```

`npm install` solo es necesario la primera vez. Abrí la dirección que muestre Vite (normalmente http://localhost:5173). Vite redirige `/api` al backend en `http://localhost:8080`.

## Probar el caso de uso

1. Iniciar el registro de recepción: se muestra la CM del empleado logueado y los bolsines enviados.
2. Seleccionar un bolsín: se muestran sus remitos y documentación.
3. Elegir la opción 1: "El contenido del bolsín es igual al registrado".
4. Confirmar.

Resultado esperado: el bolsín pasa a "Recibido en CM destino", los remitos a "Recibido y Aceptado" y la documentación a "Recibida y Aceptada". El bolsín deja de aparecer entre los enviados.

### Verificar en la base

```powershell
docker exec -it bolsines-mariadb mariadb -u root -p bolsines_db -e "SELECT nro_bolsin, estado_actual FROM bolsin; SELECT nro_remito, estado FROM remito; SELECT nro_doc, estado_actual FROM documentacion; SELECT id, nro_bolsin, estado, fecha_hora_fin FROM cambio_estado_bolsin; SELECT id, nro_doc, estado, fecha_hora_fin FROM cambio_estado_doc;"
```

Cada cambio de estado anterior queda con `fecha_hora_fin` cargada y el nuevo con `NULL`.

## Volver a empezar con datos limpios

El seed no vuelve a correr si ya hay datos, así que para repetir la prueba hay que recrear la base:

```powershell
docker exec -it bolsines-mariadb mariadb -u root -p -e "DROP DATABASE IF EXISTS bolsines_db; CREATE DATABASE bolsines_db;"
```

Después reiniciá el backend (paso 3).

## Endpoints del backend

Prefijo `/api/recepcion-bolsin`, todos `POST`:

| Endpoint        | Cuerpo                | Qué hace                                               |
| --------------- | --------------------- | ------------------------------------------------------ |
| `/iniciar`      | –                     | Busca la CM del empleado y lista los bolsines enviados |
| `/seleccion`    | `{"nroBolsin": 1042}` | Devuelve remitos y documentación del bolsín            |
| `/opcion`       | `{"opcion": 1}`       | Registra la opción de recepción y pide confirmación    |
| `/confirmacion` | `{"respuesta": "S"}`  | Registra la recepción                                  |

## Problemas frecuentes

- **`Ports are not available ... 3306`**: otro proceso usa el puerto. Identificalo con `Get-NetTCPConnection -LocalPort 3306 -State Listen` y detenelo.
- **`Access denied` al arrancar el backend**: `DB_PASSWORD` del `.env` no coincide con la contraseña del contenedor.
- **`Unknown column 'estadoActual'`** en una consulta SQL: la columna se llama `estado_actual`.
- **Al recibir el bolsín da error de estado nulo**: la base se creó con una versión anterior del modelo. Recrearla como se indica arriba.
