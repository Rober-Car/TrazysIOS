---
name: trazys-progreso
description: Use when working on TrazysiOS (Trazys Cliente iOS, KMP/Compose, fases 1A-5) at C:\Users\Roberto\AndroidStudioProjects\TrazysiOS, or when asked to continue, resume, close or update progress of this project. Keeps the session aligned with ESTADO.md, PLAN.md, DECISIONS.md and RULES.md so work is not lost.
---

# TrazysiOS — protocolo de progreso

## Antes de tocar código

1. Lee `ESTADO.md`, `PLAN.md`, `RULES.md` y `DECISIONS.md`.
2. Confirma la fase activa y la tarea exacta. Si la tarea no está en el
   backlog de `ESTADO.md`, pregunta antes de inventarla.
3. No avances de fase sin instrucción explícita (R3).

## Durante el trabajo

- Una tarea a la vez; al terminar, anota el resultado en `ESTADO.md`.
- R1: GestorPro es solo referencia; no modificar sus módulos Android.
- R2: cualquier cambio en `functions/`, reglas o Firebase requiere aprobación,
  tests y regresión Android.
- Convenciones de `AGENTS.md`: sin `!!`, E/S en corrutinas, estado con
  `StateFlow`, UI Material3, textos en español, KDoc en API pública nueva.
- R6: no commitear ni hacer push sin petición explícita.

## Verificación obligatoria

```powershell
.\gradlew.bat :composeApp:jvmTest
.\gradlew.bat :composeApp:compileKotlinIosSimulatorArm64
```

Si el cambio es solo de documentación, basta con `git diff --stat`.

## Al cerrar la sesión

1. Actualiza `ESTADO.md`: Ahora, Hecho recientemente (con fecha), Siguiente
   paso, Bloqueos, Verificación y Decisiones pendientes.
2. Actualiza `PLAN.md` si cambia el estado de una fase.
3. Actualiza `DECISIONS.md` si se cierra una decisión (con fecha y motivo).
4. Resume: hecho, verificado, pendiente, riesgo y siguiente paso.
