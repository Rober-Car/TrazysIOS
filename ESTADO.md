# ESTADO.md — Tablero de progreso

Actualizado: 2026-11-19

## Ahora

- **Fase:** 0A completada · 1A pendiente.
- **Trabajando en:** nada (sesión cerrada).
- **Último commit:** `59886c6` Fase 0A: bootstrap KMP, DataStore comun y shell iOS.
- **Remoto:** https://github.com/Rober-Car/TrazysIOS (`main` sincronizado).

## Siguiente paso (en orden)

1. Decidir el arranque de 1A: **spike GitLive** o **navegación/DI/modelos**.
2. Conectar dependencias ya declaradas en `libs.versions.toml` a
   `composeApp/build.gradle.kts` (Koin, Navigation, Lifecycle, Coil,
   kotlinx-datetime y GitLive).
3. Spike de navegación/DI/UI con 3 rutas placeholder.
4. Spike Firebase (init, auth, Firestore, Storage, callable `reservar`).
5. Portar modelos del Cliente desde GestorPro y añadir tests JVM.

## Hecho recientemente

- 2026-11-19: primer commit y push a GitHub del trabajo de Fase 0A.
- 2026-11-19: alta de `ESTADO.md`, skill `trazys-progreso`, comandos
  `/estado`, `/siguiente`, `/cierre` y `opencode.json`.

## Bloqueos

1. Sin Mac/Xcode: no se enlaza Firebase, no se ejecuta simulador ni se firma.
2. Apple Developer Program pendiente.
3. Falta `GoogleService-Info.plist` (no se versiona).
4. APNs y despliegue de `functions/` requieren cuenta, CLI y aprobación.

## Verificación

- Última ejecución: 2026-11-19 (Fase 0A).
- `.\gradlew.bat :composeApp:jvmTest` → OK (1/1).
- `.\gradlew.bat :composeApp:compileKotlinIosSimulatorArm64` → OK.

## Decisiones pendientes

- Orden de la Fase 1A: spike GitLive primero (valida el riesgo beta) o
  navegación/DI/modelos primero (avance seguro y visible).

## Regla de sesión

Al terminar cada sesión: actualizar este archivo y, si aplica, `PLAN.md` y
`DECISIONS.md`. No commitear sin petición explícita (R6).
