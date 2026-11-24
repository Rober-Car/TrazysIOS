package com.roberto.trazys.cliente.model

/**
 * Estados de la ficha del cliente con los mismos nombres que el contrato
 * remoto. [MOROSO] se calcula y nunca se almacena.
 */
enum class EstadoCliente {
    ACTIVO,
    MOROSO,
    BAJA,
    ARCHIVADO,
    REGISTRADO
}
