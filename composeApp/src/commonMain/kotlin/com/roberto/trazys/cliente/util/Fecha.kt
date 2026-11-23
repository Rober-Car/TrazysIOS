package com.roberto.trazys.cliente.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Formatea milisegundos epoch como `dd/MM/yyyy` en la zona indicada.
 * Se usa en la UI provisional; la capa de dominio seguirá trabajando con epoch.
 */
fun formatearFechaCorta(
    epochMillis: Long,
    zona: TimeZone = TimeZone.currentSystemDefault()
): String {
    val fecha = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zona).date
    val dia = fecha.day.toString().padStart(2, '0')
    val mes = (fecha.month.ordinal + 1).toString().padStart(2, '0')
    return "$dia/$mes/${fecha.year}"
}
