# ESTADO.md — Tablero de progreso

Actualizado: 2026-11-25

## Ahora

- **Fase:** 3 en curso (UI). Núcleo de datos de la Fase 2 cerrado: auth,
  vinculación, sesiones/reservas, Home, avisos, baja/cuenta y denuncias.
- **Trabajando en:** flujo de acceso real ya portado; siguiente pantalla del
  grafo pendiente de elegir (vinculación, Home real o cuenta).
- **Último commit:** `b4f77ba` (denuncias UGC) con push a `main`.
- **Remoto:** https://github.com/Rober-Car/TrazysIOS (`main`).

## Siguiente paso (en orden)

1. Fase 3: continuar con elección/vinculación, Home real y pantallas de
   avisos/baja/cuenta; dispositivos/FCM y fotos quedan para la Fase 4.
2. Validar en Mac: `FirebaseApp.configure` + `GoogleService-Info.plist`, Coil
   con Skiko, navegación real y lectura tipada con los modelos portados.

## Hecho recientemente

- 2026-11-25: **Fase 3 (acceso real):** `LoginScreen`, `RegistroScreen` y
  `RecuperarPasswordScreen` contra `AutenticacionRepository`, con validación,
  errores en español (`mensajeErrorAuth`), carga y navegación. El arranque
  respeta la sesión de Firebase y el cierre de sesión limpia la ficha/negocio
  (`PreferencesRepository.limpiarSesion`) conservando ajustes del dispositivo.
  El registro exige aceptar términos y los guarda tras el alta. 3 pruebas
  nuevas (100/100).
- 2026-11-24: **Fase 2 (denuncias UGC):** `TiposContenidoDenunciable`,
  `MotivosDenuncia`, `FuenteDenuncias` (`denuncias`, estado PENDIENTE, fecha y
  descripción limitada a 500) y `DenunciaRepository` con validación de tipo,
  motivo, negocio/referencia y traducción de errores. 9 pruebas nuevas.
- 2026-11-24: **Fase 2 (notificaciones/baja/cuenta):** fuentes
  `FuenteNotificaciones` (`notificaciones_por_destinatario`, filtros
  `clienteId` + `negocioId`, orden `fechaEnvio` desc, marcar leída y eliminar)
  y `FuenteSolicitudes` (`solicitudes`, filtros `negocioId` + `idCliente`, alta
  PENDIENTE con ID determinista). `FuenteEliminacionCuenta` invoca la callable
  `eliminarMiCuenta` (Admin SDK). Repositorios `NotificacionRepository`,
  `SolicitudBajaRepository` y `CuentaRepository` (reautenticación previa,
  limpieza local y cierre de sesión solo tras el éxito remoto).
- 2026-11-24: 23 pruebas nuevas; total 88 al cerrar esa rebanada (97/97 con
  denuncias).
- 2026-11-24: **Fase 2 (sesiones/reservas/Home):** fuentes `FuenteClientes`,
  `FuenteSesiones` (servicio + sesiones por servicio con filtros de Rules) y
  `FuenteReservas` (callables `reservar`/`cancelarReserva` en `europe-west1`
  y lectura de reservas propias). Repositorios `SesionRepository`,
  `ReservaRepository` y `HomeRepository`; `EstadoHomeCliente` derivado de la
  ficha con reloj inyectable. 33 pruebas nuevas (65/65 en total).
- 2026-11-23: **Fase 2 (vinculación):** `ErrorVinculacion`/`ResultadoVinculacion`,
  `PerfilPendienteRepository`, fuentes de códigos, centros públicos, índices,
  fichas y aviso al ADMIN. VÍA 1 vincula la ficha libre; VÍA 2 crea ficha +
  índice + vínculo en la misma transacción. La declaración temporal y el perfil
  solo se borran al completar correctamente. Transferencia de foto pendiente de
  la fase de Storage.
- 2026-11-23: **Fase 2 (auth):** `AutenticacionRepository` con alta y rollback,
  login con rol CLIENTE y cuenta activa, recuperación sin revelar cuentas,
  cambio de contraseña con reautenticación y 11 pruebas.
- 2026-11-23: **spike GitLive** con las operaciones reales: sesión anónima,
  lectura tipada de Firestore, transacción de vinculación, callable `reservar`
  y subida a Storage (`FirebaseSpike`).
- 2026-11-23: modelos del Cliente en `commonMain/.../model/` con
  `@Serializable` en documentos de Firestore y `kotlinx.datetime.DayOfWeek`.
- 2026-11-23: Koin 4.2.2, Navigation 2.10.0, Lifecycle 2.11.0, Coil 3.3.0 y
  kotlinx-datetime 0.8.0 conectados; rutas `inicio`, `login` y `home`; Login
  guarda el DNI en DataStore; Home lee negocio/logo y formatea la fecha.

## Bloqueos

1. Sin Mac/Xcode: no se enlaza Firebase, no se ejecuta simulador ni se firma.
2. Apple Developer Program pendiente.
3. Falta `GoogleService-Info.plist` (no se versiona).
4. APNs y despliegue de `functions/` requieren cuenta, CLI y aprobación.

## Riesgos

- **Skiko/Coil:** Coil 3.3.0 arrastra Skiko 0.9.4.2 frente a 0.144.6 de CMP
  1.11.1 (aviso del checker). Compila y los tests pasan; validar runtime en
  Mac. Si falla, revisar la versión de Coil con `DECISIONS.md` (R4).
- La validación real de Navigation/Koin/Coil en iOS no es posible en Windows.
- **GitLive:** la beta 3.0.0-beta1 declara Kotlin/stdlib 2.4.20 y compilamos con
  2.4.10; compila en JVM e iOS, pero vigilarlo al actualizar Kotlin (R4).
- **Índice compuesto:** el buzón ordenado por `fechaEnvio` necesita el índice
  `notificaciones_por_destinatario(clienteId, negocioId, fechaEnvio desc)`
  desplegado en el backend compartido.
- **Messaging** queda para la Fase 4 (APNs); no se enlazó todavía.

## Verificación

- 2026-11-25: `.\gradlew.bat :composeApp:jvmTest` → OK (100/100).
- 2026-11-25: `.\gradlew.bat :composeApp:compileKotlinIosSimulatorArm64` → OK.
- 2026-11-24: `.\gradlew.bat :composeApp:jvmTest` → OK (97/97).
- 2026-11-23: `.\gradlew.bat :composeApp:jvmTest` → OK (65/65).
- Nota: en Koin 4 `GlobalContext` no está en common; se usa un flag de arranque.

## Decisiones pendientes

- Validar en Mac el warning Skiko/Coil y la lectura tipada de Firestore.

## Regla de sesión

Al terminar cada sesión: actualizar este archivo y, si aplica, `PLAN.md` y
`DECISIONS.md`. No commitear sin petición explícita (R6).
