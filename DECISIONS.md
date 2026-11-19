# DECISIONS.md — TrazysiOS

| Fecha | Decisión | Motivo |
|---|---|---|
| 2026-11-18 | Cliente primero, Admin después | 68 archivos, 36 pantallas y sin Room; es la vía más corta a TestFlight |
| 2026-11-18 | Proyecto iOS aislado | No arriesga la beta Android ni obliga a subir Kotlin/AGP de las apps vivas |
| 2026-11-18 | Descartar GestorProKMP como base | Modela 7 entidades/Room v1 y no cubre el Cliente real |
| 2026-11-18 | Compose Multiplatform | Reutiliza la UI y el conocimiento de la app Android |
| 2026-11-18 | Bundle ID `com.roberto.trazys.cliente` | Identidad de marca Trazys, independiente de Android |
| 2026-11-18 | iOS 15 mínimo | Requisito de Firebase 13 y cobertura razonable de dispositivos |
| 2026-11-18 | iPhone portrait | Evita trabajo de iPad y capturas adicionales en App Store |
| 2026-11-18 | GitLive Firebase 3.0.0-beta1 | Compatibilidad `com.google.firebase.*`, SPM y Firebase 13; beta a validar |
| 2026-11-18 | Push después del núcleo | Permite validar auth/vinculación/reservas antes de APNs |
| 2026-11-18 | `functions` se modifica con tests y regresión Android | El push iOS necesita `apns` y no debe romper Android |
| 2026-11-18 | Sin target Android de desarrollo | Decisión del usuario; la validación runtime espera al Mac |
| 2026-11-18 | Sin commits automáticos | Solo se commitea si se pide explícitamente |
