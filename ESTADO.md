# ESTADO.md — Tablero de progreso

Actualizado: 2026-11-23

## Ahora

- **Fase:** 1A completa en Windows. Navegación/DI/UI, spike GitLive y modelos
  del Cliente portados y probados; queda la validación runtime en Mac.
- **Trabajando en:** nada (pendiente de revisión del entregable).
- **Último commit:** el de esta sesión (si se aprueba) · anterior `7b493ca`.
- **Remoto:** https://github.com/Rober-Car/TrazysIOS (`main`).

## Siguiente paso (en orden)

1. Validar en Mac: `FirebaseApp.configure` + `GoogleService-Info.plist`, Coil
   con Skiko, navegación real y lectura tipada con los modelos portados.
2. Arrancar Fase 2 con repositorios (vinculación sin DNI, errores de dominio)
   sobre `data/firebase/FirebaseSpike.kt`.

## Hecho recientemente

- 2026-11-23: dependencias conectadas — Koin 4.2.2 (core/compose/viewmodel),
  Navigation 2.10.0, Lifecycle 2.11.0, Coil 3.3.0 y kotlinx-datetime 0.8.0.
- 2026-11-23: grafo con rutas `inicio`, `login` y `home`; `appModule` de Koin
  y arranque idempotente en `MainViewController`.
- 2026-11-23: Login guarda el DNI en DataStore; Home lee negocio/logo con
  StateFlow, carga el logo con Coil y muestra la fecha formateada.
- 2026-11-23: `formatearFechaCorta` + `FechaTest` (2 pruebas JVM en total).
- 2026-11-23: **spike GitLive** con las operaciones reales: sesión anónima,
  lectura tipada de Firestore, transacción de vinculación, callable
  `reservar` y subida a Storage (`FirebaseSpike`).
- 2026-11-23: plugin `kotlinx-serialization` 1.9.0 y `FirebaseApp.configure()`
  en el punto de entrada Swift.
- 2026-11-23: **modelos del Cliente** portados a `commonMain/.../model/`
  (Cliente, Sesion, Servicio, Reserva, Horario, Notificacion, SolicitudBaja,
  estados del Home) con `@Serializable` en los documentos de Firestore y
  `kotlinx.datetime.DayOfWeek`; `HorarioParseo` conserva compatibilidad con el
  formato antiguo.

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
- **Messaging** queda para la Fase 4 (APNs); no se enlazó todavía.

## Verificación

- 2026-11-23: `.\gradlew.bat :composeApp:jvmTest` → OK (9/9: 2 modelos, 5
  horario, 1 fechas, 1 DataStore).
- 2026-11-23: `.\gradlew.bat :composeApp:compileKotlinJvm` → OK.
- 2026-11-23: `.\gradlew.bat :composeApp:compileKotlinIosSimulatorArm64` → OK.
- Nota: en Koin 4 `GlobalContext` no está en common; se usa un flag de arranque.

## Decisiones pendientes

- Validar en Mac el warning Skiko/Coil y la lectura tipada de Firestore.

## Regla de sesión

Al terminar cada sesión: actualizar este archivo y, si aplica, `PLAN.md` y
`DECISIONS.md`. No commitear sin petición explícita (R6).
