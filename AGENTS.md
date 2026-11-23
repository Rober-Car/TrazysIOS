# AGENTS.md — TrazysiOS

Lee este archivo antes de modificar el proyecto.

## Proyecto

- **Nombre:** TrazysiOS (cáscara iOS de Trazys Cliente).
- **Bundle ID iOS:** `com.roberto.trazys.cliente`.
- **Package Kotlin:** `com.roberto.trazys.cliente`.
- **Stack:** Kotlin Multiplatform 2.4.10 · Compose Multiplatform 1.11.1 ·
  Gradle 8.14.4 · DataStore 1.2.1 + Okio 3.15.0 · Koin 4.2.2 ·
  Navigation KMP 2.10.0 · Lifecycle 2.11.0 · Coil 3.3.0 ·
  GitLive Firebase 3.0.0-beta1 (Firebase iOS 13.1.1 vía SPM).
- **iOS mínimo:** 15.0. iPhone, portrait. Xcode 26.2+.
- **Targets:** `iosArm64`, `iosSimulatorArm64` y `jvm` (solo pruebas).
  Sin Android y sin `iosX64`.

## Relación con GestorPro

- `C:\Users\Roberto\AndroidStudioProjects\GestorPro` es la app Android
  terminada (Admin y Cliente) y **solo se usa como referencia**: no se
  modifican sus módulos Android.
- El backend (Firebase, Functions, reglas) es compartido. Cualquier cambio en
  `functions/` requiere aprobación explícita, tests y regresión Android.
- `GestorProKMP` es un prototipo obsoleto: solo se reutilizan fragmentos ya
  validados (DataStore, shell iOS), nunca como base del producto.

## Estructura

```text
composeApp/src/
├── commonMain/kotlin/com/roberto/trazys/cliente/
│   ├── data/datastore/   # claves + creador DataStore (expect)
│   ├── data/repository/  # PreferencesRepository
│   ├── time/             # horaActualMilis (expect)
│   ├── util/             # términos
│   └── App.kt
├── iosMain/              # actuals iOS + MainViewController
├── jvmMain/              # actuals JVM (solo pruebas)
└── jvmTest/              # pruebas de persistencia
iosApp/                   # shell Xcode (xcodegen)
```

## Comandos

```powershell
# Pruebas JVM (desde Windows)
.\gradlew.bat :composeApp:jvmTest

# Compilar fuentes Kotlin/Native para iOS desde Windows
.\gradlew.bat :composeApp:compileKotlinIosSimulatorArm64

# En macOS: generar y abrir el proyecto Xcode
cd iosApp
xcodegen generate
open TrazysiOS.xcodeproj
```

## Restricciones

- Windows compila fuentes Kotlin/Native, pero **no enlaza el framework con
  Firebase, no ejecuta simulador y no firma**. Eso requiere macOS + Xcode.
- No añadir `kotlin.native.ignoreDisabledTargets=true`.
- No usar `fallbackToDestructiveMigration` (el Cliente no usa Room).
- No commitear `GoogleService-Info.plist`, keystores ni credenciales.
- No hacer commit sin petición explícita.

## Notas técnicas validadas (Windows)

- JVM + DataStore: usar `with(okio.Path.Companion) { file.toOkioPath() }`.
  `okio.Path(file)` no existe en 3.15.0 y `String.toPath()` no reconoce `C:/...`
  como absoluto.
- iOS: importar `platform.Foundation.timeIntervalSince1970` (es extensión).
- iOS: `NSFileManager.URLForDirectory`/`createDirectoryAtPath` requieren opt-in
  `kotlinx.cinterop.ExperimentalForeignApi`.
- Koin 4: `GlobalContext` no existe en `commonMain`; arrancar con
  `startKoin` + flag de idempotencia.
- GitLive 3.0.0-beta1 sí publica variante JVM: `commonMain` compila en
  `jvm` e iOS. Messaging queda para Fase 4.
- Firebase iOS: `FirebaseApp.configure()` va en `iOSApp.swift` y requiere
  `GoogleService-Info.plist` en el bundle (no versionado).
- `jvmTest` y `compileKotlinIosSimulatorArm64` verificados en Windows.

## Convenciones

- Documentación y mensajes en español; nombres existentes no se traducen.
- KDoc útil en clases y funciones públicas nuevas.
- Estado con `StateFlow`; UI Compose Multiplatform (Material3).
- Sin operador `!!`; E/S fuera del hilo principal (corrutinas).
- Errores de dominio en ViewModels/repositorios; la UI resuelve los textos.

## Documentación viva

- `ESTADO.md`: tablero de sesión (ahora, siguiente paso, bloqueos, verificación).
- `PLAN.md`: fases, estado y bloqueos.
- `DECISIONS.md`: decisiones cerradas y su motivo.
- `RULES.md`: reglas de trabajo.

## Flujo con opencode

- Ejecutar opencode **desde esta carpeta** (`TrazysiOS`) para que cargue
  `opencode.json`, la skill `.opencode/skills/trazys-progreso` y los comandos.
- Comandos: `/estado` (informe), `/siguiente` (siguiente tarea) y `/cierre`
  (verifica y actualiza el tablero).
- Regla de sesión: actualizar `ESTADO.md` al terminar; no commitear sin
  petición explícita.
