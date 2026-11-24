# PLAN.md — Trazys Cliente iOS

## Estado actual

- **Fase:** 2 en curso (auth, vinculación, sesiones/reservas, Home,
  notificaciones, baja, cuenta y denuncias cerrados con tests).
- **Hecho:** repo, wrapper 8.14.4, catálogo de versiones, targets iOS/JVM,
  DataStore común, `PreferencesRepository`, test JVM, App mínima, shell iOS
  y documentación.
- **Fase 1A (parcial):** Koin, Navigation, Lifecycle, Coil y datetime
  conectados; rutas `inicio`/`login`/`home`; Login guarda DNI y Home lee
  preferencias, carga logo y formatea fecha.
- **Spike GitLive (compilado):** `FirebaseSpike` con sesión anónima, lectura
  tipada de Firestore, transacción de vinculación, callable `reservar` y
  subida a Storage. Requiere validación runtime en Mac.
- **Modelos portados:** `commonMain/.../model/` con los contratos del Cliente
  (`@Serializable` en documentos de Firestore, `kotlinx.datetime.DayOfWeek` en
  horarios) y tests de `HorarioParseo`/estados.
- **Fase 2 (auth):** `AutenticacionRepository` con fuentes inyectables,
  `ErrorAuth`/`ResultadoSesion`, validaciones puras y 11 pruebas nuevas.
- **Fase 2 (vinculación):** `VinculacionRepository` con VÍA 1/VÍA 2,
  `PerfilPendienteRepository`, fuentes de códigos/centros/índices/fichas,
  aviso idempotente al ADMIN y 16 pruebas nuevas.
- **Fase 2 (sesiones/reservas/Home):** `FuenteClientes`, `FuenteSesiones`
  (servicio activo + sesiones por servicio) y `FuenteReservas` (callables
  `reservar`/`cancelarReserva` y lectura de reservas propias);
  `SesionRepository`, `ReservaRepository`, `HomeRepository` y estado derivado
  con reloj inyectable. 33 pruebas nuevas.
- **Fase 2 (notificaciones/baja/cuenta):** `FuenteNotificaciones` (buzón
  propio, orden por `fechaEnvio`, marcar leída y eliminar), `FuenteSolicitudes`
  (solicitud PENDIENTE con ID determinista), `FuenteEliminacionCuenta`
  (callable `eliminarMiCuenta`), `NotificacionRepository`,
  `SolicitudBajaRepository` y `CuentaRepository` (reautenticación, limpieza
  local y cierre de sesión tras el éxito remoto). 23 pruebas nuevas.
- **Fase 2 (denuncias UGC):** `TiposContenidoDenunciable`,
  `MotivosDenuncia`, `FuenteDenuncias` (estado PENDIENTE, fecha y descripción
  máxima de 500) y `DenunciaRepository` con validación y errores de dominio.
  9 pruebas nuevas.
- **Verificado en Windows:**
  - `:composeApp:jvmTest` → BUILD SUCCESSFUL (97/97 pruebas).
  - `:composeApp:compileKotlinIosSimulatorArm64` → BUILD SUCCESSFUL.
  - `:composeApp:compileKotlinJvm` → BUILD SUCCESSFUL.
- **Ajustes surgidos al compilar:**
  - JVM: `File.toOkioPath()` (okio 3.15.0) se invoca con el companion como
    receptor: `with(Path.Companion) { file.toOkioPath() }`.
  - iOS: `timeIntervalSince1970` es una extensión de Foundation y requiere
    `import platform.Foundation.timeIntervalSince1970`.
  - iOS: `URLForDirectory`/`createDirectoryAtPath` requieren
    `@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)`.
  - Tests JVM: un método `= runBlocking { ... }` debe terminar en una expresión
    `Unit`; si no, JUnit lo rechaza por no devolver `void`.
- **Límite de Windows:** compila Kotlin/Native, pero no enlaza Firebase,
  no ejecuta simulador ni firma.

## Decisiones de alcance

- Solo **Trazys Cliente** por ahora; Admin se decide al final.
- Proyecto **aislado**: no se toca la app Android.
- Push después del núcleo; APNs en Fase 4.
- Sin target Android de desarrollo: la validación runtime espera al Mac.
- Denuncias, dispositivos, fotos y términos se revisan al cerrar la Fase 2
  para ubicarlos en 2 o 4.

## Fases

| Fase | Contenido | Estado |
|---|---|---|
| 0A | Repo, bootstrap KMP, DataStore, shell iOS, compilación Windows | Completada |
| 0B | Mac, Xcode, Apple Developer, Firebase iOS, APNs | Bloqueada por infraestructura |
| 1A | Spike GitLive/Firebase, navegación, Coil, Koin, fechas, modelos | Completada (Windows) |
| 1B | Primera build en iPhone | Bloqueada por Mac |
| 2 | Núcleo: auth, repos, vinculación, reservas, Home, avisos, baja/cuenta, denuncias | En curso |
| 3 | UI: tema, recursos, 21 rutas, pantallas | Pendiente |
| 4 | Push/APNs, fotos, privacy manifest, backend | Pendiente |
| 5 | TestFlight y App Store | Pendiente |

## Bloqueos

1. No hay Mac ni Xcode: no se puede enlazar, ejecutar ni firmar.
2. Apple Developer Program pendiente.
3. `GoogleService-Info.plist` pendiente de descargar desde Firebase Console.
4. APNs y despliegue de Functions requieren cuenta/CLI y aprobación.

## Siguiente paso

1. Fase 2: dispositivos/FCM, fotos y términos se ubican en Fase 4/UI; revisar
   si queda algún dato del núcleo antes de pasar a la Fase 3.
2. Validar en Mac el arranque de Firebase (`GoogleService-Info.plist`) y el
   runtime de navegación/Coil/lectura tipada/callables.
