package com.roberto.trazys.cliente.model

import kotlinx.serialization.Serializable

/**
 * Datos públicos del centro (`negocios_publicos/{negocioId}`) que puede leer
 * cualquier usuario autenticado: nombre, logo y código maestro.
 */
@Serializable
data class NegocioPublico(
    val nombre: String = "",
    val codigoMaestro: String = "",
    val logo: String = ""
)
