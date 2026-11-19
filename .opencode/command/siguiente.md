---
description: Ejecuta la siguiente tarea pendiente de TrazysiOS respetando el protocolo de fases
agent: build
---

Sigue la skill `trazys-progreso`.

1. Lee `ESTADO.md`, `PLAN.md`, `DECISIONS.md` y `RULES.md`.
2. Elige la primera tarea pendiente del backlog. Si hay ambigüedad o más de
   una opción razonable, pregunta antes de empezar.
3. Anuncia en 3 líneas qué vas a hacer y cómo lo vas a verificar.
4. Implementa solo esa tarea. No toques GestorPro (R1) ni `functions/` sin
   aprobación (R2).
5. Verifica con los comandos de `ESTADO.md`/`AGENTS.md`.
6. Actualiza `ESTADO.md` (y `PLAN.md`/`DECISIONS.md` si aplica).
7. No commitees ni hagas push salvo petición explícita (R6).

Instrucción adicional del usuario: $ARGUMENTS
