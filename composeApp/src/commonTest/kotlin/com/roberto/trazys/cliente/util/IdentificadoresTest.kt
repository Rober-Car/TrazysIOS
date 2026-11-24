package com.roberto.trazys.cliente.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdentificadoresTest {

    @Test
    fun elIndiceNormalizaDniYUsaElFormatoDelContrato() {
        assertEquals("negocio-1_12345678Z", indiceClienteId("negocio-1", " 12345678z "))
        assertEquals("12345678Z", normalizarDni(" 12345678z"))
    }

    @Test
    fun elIdClienteQuedaDentroDelRangoAlto() {
        repeat(100) {
            val id = generarIdCliente(Random(it))
            assertTrue(id >= ID_CLIENTE_MINIMO)
        }
    }
}
