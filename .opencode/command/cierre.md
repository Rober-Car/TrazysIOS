---
description: Cierra la sesión de TrazysiOS actualizando el tablero y verificando
agent: build
---

Cierra la sesión siguiendo la skill `trazys-progreso`:

1. Revisa `git status -sb` y `git diff --stat`.
2. Si hubo cambios de código, ejecuta:
   `.\gradlew.bat :composeApp:jvmTest`
   `.\gradlew.bat :composeApp:compileKotlinIosSimulatorArm64`
   Si algo falla, no cierres: informa del error y propón la corrección.
3. Actualiza `ESTADO.md`: Ahora, Hecho recientemente (con fecha), Siguiente
   paso, Bloqueos, Verificación (comando y resultado) y Decisiones pendientes.
4. Actualiza `PLAN.md` si cambió el estado de una fase y `DECISIONS.md` si se
   cerró una decisión.
5. Resume en 5 líneas: hecho, verificado, pendiente, riesgo y siguiente paso.
6. Pregunta si se desea commit y push. No lo hagas sin confirmación (R6).

Contexto adicional de la sesión: $ARGUMENTS
