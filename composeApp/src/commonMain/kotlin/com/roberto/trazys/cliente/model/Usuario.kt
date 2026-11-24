package com.roberto.trazys.cliente.model

/**
 * Documento `usuarios/{uid}` del Cliente. [clienteId] y [negocioId] son nulos
 * mientras la cuenta no está vinculada a un centro.
 */
data class Usuario(
    val rol: String,
    val activo: Boolean,
    val clienteId: Int?,
    val negocioId: String?
) {
    val vinculado: Boolean get() = clienteId != null && negocioId != null
}
