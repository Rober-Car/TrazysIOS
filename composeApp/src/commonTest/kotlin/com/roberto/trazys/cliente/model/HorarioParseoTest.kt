package com.roberto.trazys.cliente.model

import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HorarioParseoTest {

    @Test
    fun centroAdmiteFormatoNuevoYAntiguo() {
        val centro = HorarioParseo.mapaACentro(
            mapOf(
                "MONDAY" to listOf(
                    mapOf("apertura" to "10:00", "cierre" to "13:00"),
                    mapOf("apertura" to "17:00", "cierre" to "22:00")
                ),
                "TUESDAY" to mapOf("apertura" to "09:00", "cierre" to "20:00"),
                "WEDNESDAY" to emptyList<Any>(),
                "NOEXISTE" to listOf(mapOf("apertura" to "00:00", "cierre" to "00:00"))
            )
        )

        assertEquals(2, centro[DayOfWeek.MONDAY]?.size)
        assertEquals("17:00", centro[DayOfWeek.MONDAY]?.get(1)?.apertura)
        assertEquals("09:00", centro[DayOfWeek.TUESDAY]?.single()?.apertura)
        assertEquals(emptyList(), centro[DayOfWeek.WEDNESDAY])
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY), centro.keys)
    }

    @Test
    fun diaCerradoAntiguoDevuelveListaVacia() {
        val centro = HorarioParseo.mapaACentro(
            mapOf("FRIDAY" to mapOf("cerrado" to true))
        )
        assertEquals(emptyList(), centro[DayOfWeek.FRIDAY])
    }

    @Test
    fun actividadesIgnoranEntradasInvalidas() {
        val actividades = HorarioParseo.mapaAActividades(
            mapOf(
                "SATURDAY" to listOf(
                    mapOf("idServicio" to 7, "hora" to "18:30"),
                    mapOf("hora" to "19:00"),
                    mapOf("idServicio" to 9),
                    "basura"
                )
            )
        )

        val sabado = actividades[DayOfWeek.SATURDAY].orEmpty()
        assertEquals(1, sabado.size)
        assertEquals(ActividadHorario(idServicio = 7, hora = "18:30"), sabado.single())
    }

    @Test
    fun excepcionesSeOrdenanYAdmitenFormatoAntiguo() {
        val excepciones = HorarioParseo.listaAExcepciones(
            listOf(
                mapOf("fecha" to 2_000L, "tramos" to emptyList<Any>()),
                mapOf("fecha" to 1_000L, "apertura" to "10:00", "cierre" to "14:00"),
                mapOf("cerrado" to true),
                "basura"
            )
        )

        assertEquals(listOf(1_000L, 2_000L), excepciones.map { it.fecha })
        assertEquals("10:00", excepciones.first().tramos.single().apertura)
        assertTrue(excepciones.last().cerrado)
        assertFalse(excepciones.first().cerrado)
    }

    @Test
    fun horarioVacioSoloSiNoHayCentroNiActividades() {
        assertTrue(HorarioNegocio().vacio)
        assertTrue(HorarioNegocio(excepciones = listOf(ExcepcionHorario(1L))).vacio)
        assertFalse(
            HorarioNegocio(
                centro = mapOf(DayOfWeek.MONDAY to listOf(TramoHorario("10:00", "11:00")))
            ).vacio
        )
    }
}
