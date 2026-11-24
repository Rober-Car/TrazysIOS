package com.roberto.trazys.cliente.util

import kotlin.random.Random

/** Rango mínimo de los identificadores de cliente (coherente con GestorPro). */
const val ID_CLIENTE_MINIMO = 1_000_000_000

/** Número máximo de intentos para encontrar un id de cliente libre. */
const val MAX_INTENTOS_ID_CLIENTE = 5

/** Normaliza el DNI tal y como se almacena en el índice (sin espacios, mayúsculas). */
fun normalizarDni(dni: String): String = dni.trim().uppercase()

/**
 * DocumentId del índice de unicidad negocio + DNI:
 * `indices_clientes/{negocioId}_{dni}`.
 */
fun indiceClienteId(negocioId: String, dni: String): String =
    "${negocioId}_${normalizarDni(dni)}"

/** Genera un id de cliente aleatorio dentro del rango alto compartido. */
fun generarIdCliente(random: Random = Random.Default): Int =
    random.nextInt(ID_CLIENTE_MINIMO, Int.MAX_VALUE)
