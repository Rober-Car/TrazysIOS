package com.roberto.trazys.cliente.model

/** Tipos de contenido UGC denunciable (mismos valores que las Rules). */
object TiposContenidoDenunciable {
    const val FOTO_CLIENTE = "FOTO_CLIENTE"
    const val NOTIFICACION = "NOTIFICACION"
    const val LOGO_NEGOCIO = "LOGO_NEGOCIO"

    fun validos(): List<String> =
        listOf(FOTO_CLIENTE, NOTIFICACION, LOGO_NEGOCIO)
}

/** Motivos cortos de denuncia (mismos valores que las Rules). */
object MotivosDenuncia {
    const val INAPROPIADO = "INAPROPIADO"
    const val ACOSO = "ACOSO"
    const val OFENSIVO = "OFENSIVO"
    const val SUPLANTACION = "SUPLANTACION"
    const val OTRO = "OTRO"

    fun validos(): List<String> =
        listOf(INAPROPIADO, ACOSO, OFENSIVO, SUPLANTACION, OTRO)
}

/** Longitud máxima de la descripción libre de una denuncia. */
const val MAX_DESCRIPCION_DENUNCIA = 500
