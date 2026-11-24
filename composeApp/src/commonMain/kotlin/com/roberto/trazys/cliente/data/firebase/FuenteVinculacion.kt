package com.roberto.trazys.cliente.data.firebase

import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.model.NegocioPublico
import com.roberto.trazys.cliente.model.PerfilPendiente
import com.roberto.trazys.cliente.util.MAX_INTENTOS_ID_CLIENTE
import com.roberto.trazys.cliente.util.generarIdCliente
import com.roberto.trazys.cliente.util.indiceClienteId
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore

/** `codigos_maestros/{codigo}` → negocioId (reserva global, un código = un centro). */
interface FuenteCodigos {
    suspend fun negocioIdDe(codigo: String): String?
}

/** `negocios_publicos/{negocioId}` → nombre, logo y código maestro. */
interface FuenteNegocios {
    suspend fun leerPublico(negocioId: String): NegocioPublico?
}

/** `indices_clientes/{negocioId}_{dni}` → ficha creada por el ADMIN. */
interface FuenteIndices {
    suspend fun clienteIdDe(negocioId: String, dni: String): Int?
}

/** Resultado de vincular el UID a una ficha existente (VÍA 1). */
sealed interface ResultadoFichaVinculada {
    data object Vinculada : ResultadoFichaVinculada
    data object YaVinculada : ResultadoFichaVinculada
    data object NoExiste : ResultadoFichaVinculada
}

/** Resultado de crear ficha + índice + vínculo (VÍA 2). */
sealed interface ResultadoCreacionFicha {
    data class Creada(val clienteId: Int) : ResultadoCreacionFicha
    data object IndiceOcupado : ResultadoCreacionFicha
    data object IdOcupado : ResultadoCreacionFicha
    data object SinIdDisponible : ResultadoCreacionFicha
}

/**
 * Escrituras atómicas de vinculación. La comprobación y la escritura viven en la
 * misma transacción para que la unicidad la garantice el documentId del índice.
 */
interface FuenteClientesVinculacion {
    suspend fun vincularFicha(
        clienteId: Int,
        uid: String,
        negocioId: String
    ): ResultadoFichaVinculada

    suspend fun crearFicha(
        uid: String,
        negocioId: String,
        dni: String,
        perfil: PerfilPendiente
    ): ResultadoCreacionFicha
}

/** Aviso idempotente en la bandeja del ADMIN tras una vinculación correcta. */
interface FuenteNotificacionesAdmin {
    suspend fun notificarVinculacion(negocioId: String, clienteId: Int)
}

/** Implementación real sobre GitLive Firestore. */
class FuenteCodigosGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteCodigos {

    override suspend fun negocioIdDe(codigo: String): String? {
        val documento = try {
            db.collection(COLECCION).document(codigo.trim()).get()
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
        if (!documento.exists) return null
        val negocioId = documento.campoString("negocioId")
        return negocioId.ifBlank { null }
    }

    private companion object {
        const val COLECCION = "codigos_maestros"
    }
}

/** Implementación real sobre GitLive Firestore. */
class FuenteNegociosGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteNegocios {

    override suspend fun leerPublico(negocioId: String): NegocioPublico? {
        val documento = try {
            db.collection(COLECCION).document(negocioId).get()
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
        if (!documento.exists) return null
        return NegocioPublico(
            nombre = documento.campoString("nombre"),
            codigoMaestro = documento.campoString("codigoMaestro"),
            logo = documento.campoString("logo")
        )
    }

    private companion object {
        const val COLECCION = "negocios_publicos"
    }
}

/** Implementación real sobre GitLive Firestore. */
class FuenteIndicesGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteIndices {

    override suspend fun clienteIdDe(negocioId: String, dni: String): Int? {
        val documento = try {
            db.collection(COLECCION).document(indiceClienteId(negocioId, dni)).get()
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
        if (!documento.exists) return null
        if (!documento.contains("clienteId")) return null
        return documento.get<Long?>("clienteId")?.toInt()
    }

    private companion object {
        const val COLECCION = "indices_clientes"
    }
}

/** Implementación real sobre GitLive Firestore. */
class FuenteClientesVinculacionGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteClientesVinculacion {

    override suspend fun vincularFicha(
        clienteId: Int,
        uid: String,
        negocioId: String
    ): ResultadoFichaVinculada {
        val fichaRef = db.collection(COLECCION_CLIENTES).document(clienteId.toString())
        val usuarioRef = db.collection(COLECCION_USUARIOS).document(uid)
        return try {
            db.runTransaction {
                val ficha = get(fichaRef)
                if (!ficha.exists) return@runTransaction ResultadoFichaVinculada.NoExiste
                val uidActual = if (ficha.contains("firebaseUid")) {
                    ficha.get<String?>("firebaseUid")
                } else {
                    null
                }
                if (uidActual != null) return@runTransaction ResultadoFichaVinculada.YaVinculada
                update(fichaRef, mapOf("firebaseUid" to uid))
                update(
                    usuarioRef,
                    mapOf("clienteId" to clienteId, "negocioId" to negocioId)
                )
                ResultadoFichaVinculada.Vinculada
            }
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    override suspend fun crearFicha(
        uid: String,
        negocioId: String,
        dni: String,
        perfil: PerfilPendiente
    ): ResultadoCreacionFicha {
        val usuarioRef = db.collection(COLECCION_USUARIOS).document(uid)
        val indiceRef = db.collection(COLECCION_INDICES).document(indiceClienteId(negocioId, dni))

        repeat(MAX_INTENTOS_ID_CLIENTE) { intento ->
            val idCliente = generarIdCliente()
            val fichaRef = db.collection(COLECCION_CLIENTES).document(idCliente.toString())
            val resultado = try {
                db.runTransaction {
                    if (get(indiceRef).exists) {
                        return@runTransaction ResultadoCreacionFicha.IndiceOcupado
                    }
                    if (get(fichaRef).exists) {
                        return@runTransaction ResultadoCreacionFicha.IdOcupado
                    }
                    set(
                        fichaRef,
                        mapOf(
                            "idCliente" to idCliente,
                            "negocioId" to negocioId,
                            "firebaseUid" to uid,
                            "nombre" to perfil.nombre,
                            "apellidos" to perfil.apellidos,
                            "dni" to dni,
                            "telefono" to perfil.telefono,
                            "email" to perfil.email,
                            "foto" to "",
                            "fechaNacimiento" to perfil.fechaNacimiento,
                            "fechaRegistro" to Timestamp.now(),
                            "fechaAlta" to null,
                            "fechaBaja" to null,
                            "estado" to ESTADO_REGISTRADO,
                            "serviciosContratados" to emptyList<Int>(),
                            "fechaInicioActual" to null,
                            "fechaFinActual" to null
                        )
                    )
                    set(
                        indiceRef,
                        mapOf(
                            "negocioId" to negocioId,
                            "dni" to dni,
                            "clienteId" to idCliente
                        )
                    )
                    update(
                        usuarioRef,
                        mapOf("clienteId" to idCliente, "negocioId" to negocioId)
                    )
                    ResultadoCreacionFicha.Creada(idCliente)
                }
            } catch (e: Exception) {
                throw ErrorRemoto(errorFirestoreDe(e), e)
            }

            when (resultado) {
                is ResultadoCreacionFicha.Creada -> return resultado
                ResultadoCreacionFicha.IndiceOcupado -> return resultado
                ResultadoCreacionFicha.IdOcupado -> {
                    if (intento == MAX_INTENTOS_ID_CLIENTE - 1) {
                        return ResultadoCreacionFicha.SinIdDisponible
                    }
                }

                ResultadoCreacionFicha.SinIdDisponible -> return resultado
            }
        }
        return ResultadoCreacionFicha.SinIdDisponible
    }

    private companion object {
        const val COLECCION_CLIENTES = "clientes"
        const val COLECCION_USUARIOS = "usuarios"
        const val COLECCION_INDICES = "indices_clientes"
        const val ESTADO_REGISTRADO = "REGISTRADO"
    }
}

/**
 * Implementación best-effort: cualquier fallo (red, permisos, Rules) se ignora
 * y nunca bloquea una vinculación ya completada.
 */
class FuenteNotificacionesAdminGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteNotificacionesAdmin {

    override suspend fun notificarVinculacion(negocioId: String, clienteId: Int) {
        try {
            val ficha = db.collection(COLECCION_CLIENTES).document(clienteId.toString()).get()
            val nombre = listOf(
                ficha.campoString("nombre"),
                ficha.campoString("apellidos")
            ).filter { it.isNotBlank() }.joinToString(" ").ifBlank { NOMBRE_POR_DEFECTO }

            val referencia = db.collection(COLECCION_NOTIFICACIONES)
                .document("vinculacion_${negocioId}_$clienteId")
            if (referencia.get().exists) return

            referencia.set(
                mapOf(
                    "negocioId" to negocioId,
                    "titulo" to "Cliente vinculado",
                    "mensaje" to "$nombre se ha vinculado al negocio.",
                    "tipo" to "VINCULACION",
                    "origen" to "AUTOMATICA",
                    "modoDestino" to "INDIVIDUAL",
                    "clienteId" to clienteId,
                    "fechaCreacion" to Timestamp.now(),
                    "estado" to "PENDIENTE"
                )
            )
        } catch (_: Exception) {
            // Best-effort: no debe impedir que la vinculación se complete.
        }
    }

    private companion object {
        const val COLECCION_CLIENTES = "clientes"
        const val COLECCION_NOTIFICACIONES = "notificaciones"
        const val NOMBRE_POR_DEFECTO = "Un cliente"
    }
}
