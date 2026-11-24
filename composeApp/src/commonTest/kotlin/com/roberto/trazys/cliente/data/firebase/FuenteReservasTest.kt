package com.roberto.trazys.cliente.data.firebase

import com.google.firebase.functions.FirebaseFunctionsException
import com.roberto.trazys.cliente.data.error.ErrorReserva
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FuenteReservasTest {

    @Test
    fun mensajesDeNegocioSeTraducen() {
        assertEquals(
            ErrorReserva.NO_VINCULADO,
            errorReservaDeMensaje("Todavía no estás vinculado a ningún gimnasio")
        )
        assertEquals(
            ErrorReserva.CUENTA_NO_ACTIVA,
            errorReservaDeMensaje("Tu cuenta no permite reservar")
        )
        assertEquals(
            ErrorReserva.SIN_PLAZAS,
            errorReservaDeMensaje("La sesión no tiene plazas disponibles")
        )
        assertEquals(
            ErrorReserva.FUERA_DE_APERTURA,
            errorReservaDeMensaje("Las reservas se abren a las 09:00")
        )
        assertEquals(
            ErrorReserva.NO_COMBINABLE,
            errorReservaDeMensaje("Ese día no permite combinar actividades")
        )
        assertEquals(
            ErrorReserva.NO_CONTRATADO,
            errorReservaDeMensaje("No tienes contratado ese servicio")
        )
        assertEquals(
            ErrorReserva.SESION_CAMBIO,
            errorReservaDeMensaje("La sesión ha cambiado. Inténtalo de nuevo")
        )
        assertNull(errorReservaDeMensaje("Mensaje sin motivo conocido"))
        assertNull(errorReservaDeMensaje(null))
    }

    @Test
    fun codigosDeLaCallableSeTraducen() {
        assertEquals(
            ErrorReserva.SIN_SESION,
            errorReservaDe(null, FirebaseFunctionsException.Code.UNAUTHENTICATED)
        )
        assertEquals(
            ErrorReserva.SIN_PERMISOS,
            errorReservaDe(null, FirebaseFunctionsException.Code.PERMISSION_DENIED)
        )
        assertEquals(
            ErrorReserva.SIN_CONEXION,
            errorReservaDe(null, FirebaseFunctionsException.Code.UNAVAILABLE)
        )
        assertEquals(
            ErrorReserva.SESION_INVALIDA,
            errorReservaDe(null, FirebaseFunctionsException.Code.INVALID_ARGUMENT)
        )
        assertEquals(
            ErrorReserva.DESCONOCIDO,
            errorReservaDe(null, FirebaseFunctionsException.Code.INTERNAL)
        )
    }

    @Test
    fun excepcionDeRedSeTraduceASinConexion() {
        assertEquals(
            ErrorReserva.SIN_CONEXION,
            errorReservaDeExcepcion(Exception("network error"))
        )
        assertEquals(
            ErrorReserva.DESCONOCIDO,
            errorReservaDeExcepcion(Exception("algo inesperado"))
        )
    }
}
