package com.roberto.trazys.cliente.time

/**
 * Reloj común de la app.
 *
 * commonMain no debe llamar a APIs de plataforma: cada target aporta su
 * implementación (JVM usa System.currentTimeMillis, iOS usa NSDate).
 */
expect fun horaActualMilis(): Long
