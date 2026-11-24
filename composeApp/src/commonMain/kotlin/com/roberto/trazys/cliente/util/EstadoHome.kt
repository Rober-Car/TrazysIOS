package com.roberto.trazys.cliente.util

import com.roberto.trazys.cliente.model.Cliente
import com.roberto.trazys.cliente.model.EstadoCliente
import com.roberto.trazys.cliente.model.EstadoHomeCliente
import com.roberto.trazys.cliente.model.EstadoIndicadorCliente

/** La ficha tiene un estado remoto que el Home no puede representar. */
const val MENSAJE_ESTADO_NO_COMPATIBLE = "La ficha contiene un estado remoto no compatible"

/**
 * Deriva el indicador del Home a partir de la ficha. Un ACTIVO con el período
 * vencido se muestra como PAGO_VENCIDO; MOROSO no se almacena ni se falsea.
 */
fun estadoHomeDe(cliente: Cliente, ahora: Long): EstadoHomeCliente = when (cliente.estado) {
    EstadoCliente.ACTIVO -> EstadoHomeCliente(
        estado = if (cliente.fechaFinActual?.let { it < ahora } == true) {
            EstadoIndicadorCliente.PAGO_VENCIDO
        } else {
            EstadoIndicadorCliente.ACTIVO
        },
        fechaRelevante = cliente.fechaFinActual
    )

    EstadoCliente.BAJA -> EstadoHomeCliente(
        estado = EstadoIndicadorCliente.BAJA,
        fechaRelevante = cliente.fechaBaja
    )

    EstadoCliente.REGISTRADO -> EstadoHomeCliente(
        estado = EstadoIndicadorCliente.REGISTRADO
    )

    EstadoCliente.ARCHIVADO -> EstadoHomeCliente(
        estado = EstadoIndicadorCliente.ARCHIVADO
    )

    EstadoCliente.MOROSO -> EstadoHomeCliente(
        error = MENSAJE_ESTADO_NO_COMPATIBLE
    )
}
