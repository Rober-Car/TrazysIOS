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
| 2026-11-23 | Fase 1A: navegación/DI/UI antes del spike GitLive | Todo lo verificable en Windows queda cerrado primero; el runtime de Firebase exige Mac |
| 2026-11-23 | Plugin `kotlinx-serialization` con runtime 1.9.0 | GitLive lo exige para `@Serializable`, Firestore tipado y callables; 1.9.0 es la versión que declara la beta |
| 2026-11-23 | Messaging se enlaza en Fase 4 | No hace falta para validar auth/Firestore/Storage y depende de APNs |
| 2026-11-23 | Modelos portados tal cual; `@Serializable` solo en documentos de Firestore | Mantiene el contrato remoto y permite `data<T>()`; el horario se reconstruye desde mapas crudos y no necesita serialización |
| 2026-11-23 | `kotlinx.datetime.DayOfWeek` en el horario | Elimina `java.time` para que el modelo compile en Kotlin/Native; `valueOf` conserva las claves `MONDAY`… del contrato |
| 2026-11-23 | Repositorios sobre interfaces `Fuente*`, no sobre Firebase directo | Permite tests JVM con dobles y aísla las excepciones de GitLive; el texto de error lo resuelve la UI desde `ErrorAuth` |
| 2026-11-23 | VÍA 2 sin transferir foto local (por ahora) | La subida depende de Storage, términos y ficheros iOS; se pospone a la fase de fotos sin bloquear ficha/índice. El perfil se borra igualmente |
| 2026-11-23 | Transacciones de vinculación dentro de la fuente, no del repositorio | Firestore exige lecturas antes de escrituras y reintentos; el repo decide el flujo y la fuente garantiza atomicidad y unicidad |
| 2026-11-24 | Reservar/cancelar solo por las callables `reservar`/`cancelarReserva` | Las reglas de plazas, estado, contratación, apertura y combinación viven en el backend; el Cliente no replica la transacción |
| 2026-11-24 | Fechas de Firestore: primero `Timestamp`, luego número | El contrato mezcla `Timestamp` (fechas de ficha) y epoch numérico; el helper tolera ambos sin romper la lectura |
| 2026-11-24 | Estado del Home derivado en `util/EstadoHome.kt` con reloj inyectable | Mantiene la UI como representación pura y permite probar `PAGO_VENCIDO` sin depender del reloj del equipo |
