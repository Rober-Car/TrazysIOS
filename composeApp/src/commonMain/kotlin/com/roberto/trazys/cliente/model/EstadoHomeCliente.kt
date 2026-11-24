package com.roberto.trazys.cliente.model

/**
 * Estados que puede representar el indicador del Home. Los tres últimos
 * pertenecen a la ficha, no a un periodo de pago.
 */
enum class EstadoIndicadorCliente {
    ACTIVO,
    PAGO_VENCIDO,
    BAJA,
    REGISTRADO,
    ARCHIVADO
}

/** Estado preparado por el ViewModel para que el Home solo lo represente. */
data class EstadoHomeCliente(
    val estado: EstadoIndicadorCliente? = null,
    val fechaRelevante: Long? = null,
    val cargando: Boolean = false,
    val error: String? = null
)
