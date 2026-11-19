# RULES.md — Reglas de trabajo

1. **R1 — GestorPro es referencia.** No modificar los módulos Android
   (`:app`, `:appCliente`) ni borrar/mover archivos del proyecto terminado.
2. **R2 — El backend es compartido.** Cualquier cambio en `functions/`,
   reglas o configuración de Firebase requiere aprobación, tests y regresión
   Android.
3. **R3 — Fases con parada.** No avanzar de fase sin revisar el entregable y
   sin instrucción explícita.
4. **R4 — Versiones verificadas.** No subir/bajar Kotlin, CMP, Gradle o
   Firebase sin comprobar compatibilidad y documentarlo en `DECISIONS.md`.
5. **R5 — Sin secretos.** No versionar `GoogleService-Info.plist`, keystores,
   `.p8` ni credenciales.
6. **R6 — Sin commits no pedidos.** Trabajar en el árbol de trabajo y commitear
   solo cuando se solicite.
7. **R7 — Windows no ejecuta iOS.** Compilar fuentes sí; enlazar, firmar y
   ejecutar requiere macOS + Xcode.
