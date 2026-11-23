# iosApp — Trazys Cliente

Cáscara Xcode del cliente iOS. El código real vive en `../composeApp` (Kotlin
Multiplatform + Compose Multiplatform).

## Requisitos (macOS)

- macOS 15.6 o superior.
- Xcode 26.2 o superior (Firebase 13 y GitLive 3 lo exigen).
- `xcodegen` (`brew install xcodegen`).
- JDK 17 o superior para Gradle.
- Cuenta Apple Developer para dispositivo/TestFlight.

## Puesta en marcha

1. Copia `GoogleService-Info.plist` de Firebase Console a `iosApp/iosApp/`
   (no se versiona). `FirebaseApp.configure()` ya se llama en `iOSApp.swift`.
2. Ajusta `DEVELOPMENT_TEAM` en `project.yml` o en Signing & Capabilities.
3. Genera el proyecto y ábrelo:

   ```bash
   chmod +x ../gradlew
   xcodegen generate
   open TrazysiOS.xcodeproj
   ```

4. Ejecuta el scheme `TrazysCliente` en un iPhone o simulador.

El script `BuildKotlinFramework` compila e incrusta el framework `ComposeApp`
con Gradle antes de cada build.

## Pendiente para push (Fase 4)

- Subir la clave APNs `.p8` a Firebase Console.
- Activar Push Notifications y Background Modes (Remote notifications) en el
  target.
- Añadir el bloque `apns` al envío FCM de `functions/lib/plan_envio.js`.
