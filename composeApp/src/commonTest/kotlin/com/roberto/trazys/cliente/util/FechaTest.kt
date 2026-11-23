package com.roberto.trazys.cliente.util

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class FechaTest {

    @Test
    fun formateaEpochEnUtc() {
        assertEquals("01/01/1970", formatearFechaCorta(0L, TimeZone.UTC))
    }
}
