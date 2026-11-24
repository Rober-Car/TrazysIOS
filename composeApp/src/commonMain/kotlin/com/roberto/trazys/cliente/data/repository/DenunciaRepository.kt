package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorDenuncia
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ResultadoDenuncia
import com.roberto.trazys.cliente.data.error.comoDenuncia
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteDenuncias
import com.roberto.trazys.cliente.model.MAX_DESCRIPCION_DENUNCIA
import com.roberto.trazys.cliente.model.MotivosDenuncia
import com.roberto.trazys.cliente.model.TiposContenidoDenunciable
import com.roberto.trazys.cliente.time.horaActualMilis
import kotlin.random.Random

/**
 * Denuncias de contenido UGC (foto, notificación o logo). El denunciante es
 * siempre el usuario autenticado y el documento nace PENDIENTE para el ADMIN.
 */
class DenunciaRepository(
    private val auth: FuenteAutenticacion,
    private val denuncias: FuenteDenuncias,
    private val ahora: () -> Long = ::horaActualMilis,
    private val sufijo: () -> Int = { Random.nextInt(1000, 10000) }
) {

    suspend fun crear(
        negocioId: String,
        tipo: String,
        referencia: String,
        usuarioDenunciadoUid: String?,
        motivo: String,
        descripcion: String?
    ): ResultadoDenuncia {
        val uid = auth.uid ?: return ResultadoDenuncia.fallo(ErrorDenuncia.SIN_SESION)
        if (tipo !in TiposContenidoDenunciable.validos()) {
            return ResultadoDenuncia.fallo(ErrorDenuncia.TIPO_INVALIDO)
        }
        if (motivo !in MotivosDenuncia.validos()) {
            return ResultadoDenuncia.fallo(ErrorDenuncia.MOTIVO_INVALIDO)
        }
        if (negocioId.isBlank() || referencia.isBlank()) {
            return ResultadoDenuncia.fallo(ErrorDenuncia.REFERENCIA_INVALIDA)
        }

        val fecha = ahora()
        val descripcionLimpia = descripcion
            ?.trim()
            ?.take(MAX_DESCRIPCION_DENUNCIA)
            ?.takeIf { it.isNotEmpty() }

        return try {
            denuncias.crear(
                idDenuncia = "denuncia_${fecha}_${sufijo()}",
                negocioId = negocioId,
                denuncianteUid = uid,
                tipo = tipo,
                referencia = referencia,
                usuarioDenunciadoUid = usuarioDenunciadoUid?.trim()?.takeIf { it.isNotEmpty() },
                motivo = motivo,
                descripcion = descripcionLimpia,
                fecha = fecha
            )
            ResultadoDenuncia.correcto()
        } catch (e: ErrorRemoto) {
            ResultadoDenuncia.fallo(e.error.comoDenuncia())
        } catch (_: Exception) {
            ResultadoDenuncia.fallo(ErrorDenuncia.DESCONOCIDO)
        }
    }
}
