package com.roberto.trazys.cliente.data.firebase

import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Timestamp

/**
 * Lecturas tolerantes de campos de Firestore comunes a las fuentes del
 * Cliente. Evitan `getString`/`getLong` del wrapper y no fallan si el campo
 * opcional está ausente.
 */
internal fun DocumentSnapshot.texto(campo: String): String =
    if (contains(campo)) get<String>(campo) else ""

internal fun DocumentSnapshot.textoOpcional(campo: String): String? =
    if (contains(campo)) get<String?>(campo) else null

internal fun DocumentSnapshot.entero(campo: String): Int? =
    if (contains(campo)) get<Long?>(campo)?.toInt() else null

internal fun DocumentSnapshot.booleano(campo: String): Boolean? =
    if (contains(campo)) get<Boolean>(campo) else null

internal fun DocumentSnapshot.fechaMilis(campo: String): Long? {
    if (!contains(campo)) return null
    val fecha = try {
        get<Timestamp?>(campo)
    } catch (_: Exception) {
        null
    }
    if (fecha != null) {
        return fecha.seconds * 1000L + fecha.nanoseconds / 1_000_000
    }
    return try {
        get<Long?>(campo)
    } catch (_: Exception) {
        null
    }
}

/** El mapa remoto `asistentes` como `{ clienteId: nombre }`; vacío si no encaja. */
internal fun asistentesDe(valor: Any?): Map<String, String> {
    val mapa = valor as? Map<*, *> ?: return emptyMap()
    return mapa.mapNotNull { (clave, nombre) ->
        val id = clave?.toString()
        val texto = nombre?.toString()
        if (id != null && texto != null) id to texto else null
    }.toMap()
}
