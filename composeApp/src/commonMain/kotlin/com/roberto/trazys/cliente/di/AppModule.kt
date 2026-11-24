package com.roberto.trazys.cliente.di

import com.roberto.trazys.cliente.data.datastore.obtenerDataStore
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacionGitLive
import com.roberto.trazys.cliente.data.firebase.FuenteClientes
import com.roberto.trazys.cliente.data.firebase.FuenteClientesGitLive
import com.roberto.trazys.cliente.data.firebase.FuenteClientesVinculacion
import com.roberto.trazys.cliente.data.firebase.FuenteClientesVinculacionGitLive
import com.roberto.trazys.cliente.data.firebase.FuenteCodigos
import com.roberto.trazys.cliente.data.firebase.FuenteCodigosGitLive
import com.roberto.trazys.cliente.data.firebase.FuenteIndices
import com.roberto.trazys.cliente.data.firebase.FuenteIndicesGitLive
import com.roberto.trazys.cliente.data.firebase.FuenteNegocios
import com.roberto.trazys.cliente.data.firebase.FuenteNegociosGitLive
import com.roberto.trazys.cliente.data.firebase.FuenteNotificacionesAdmin
import com.roberto.trazys.cliente.data.firebase.FuenteNotificacionesAdminGitLive
import com.roberto.trazys.cliente.data.firebase.FuentePerfiles
import com.roberto.trazys.cliente.data.firebase.FuentePerfilesGitLive
import com.roberto.trazys.cliente.data.firebase.FuenteReservas
import com.roberto.trazys.cliente.data.firebase.FuenteReservasGitLive
import com.roberto.trazys.cliente.data.firebase.FuenteSesiones
import com.roberto.trazys.cliente.data.firebase.FuenteSesionesGitLive
import com.roberto.trazys.cliente.data.firebase.FuenteUsuarios
import com.roberto.trazys.cliente.data.firebase.FuenteUsuariosGitLive
import com.roberto.trazys.cliente.data.repository.AutenticacionRepository
import com.roberto.trazys.cliente.data.repository.HomeRepository
import com.roberto.trazys.cliente.data.repository.PerfilPendienteRepository
import com.roberto.trazys.cliente.data.repository.PreferencesRepository
import com.roberto.trazys.cliente.data.repository.ReservaRepository
import com.roberto.trazys.cliente.data.repository.SesionRepository
import com.roberto.trazys.cliente.data.repository.VinculacionRepository
import com.roberto.trazys.cliente.time.horaActualMilis
import org.koin.core.context.startKoin
import org.koin.dsl.module

/** Módulo Koin del Cliente. */
val appModule = module {
    single { PreferencesRepository(obtenerDataStore()) }
    single<FuenteAutenticacion> { FuenteAutenticacionGitLive() }
    single<FuenteUsuarios> { FuenteUsuariosGitLive() }
    single<FuentePerfiles> { FuentePerfilesGitLive() }
    single<FuenteCodigos> { FuenteCodigosGitLive() }
    single<FuenteNegocios> { FuenteNegociosGitLive() }
    single<FuenteIndices> { FuenteIndicesGitLive() }
    single<FuenteClientesVinculacion> { FuenteClientesVinculacionGitLive() }
    single<FuenteNotificacionesAdmin> { FuenteNotificacionesAdminGitLive() }
    single<FuenteClientes> { FuenteClientesGitLive() }
    single<FuenteSesiones> { FuenteSesionesGitLive() }
    single<FuenteReservas> { FuenteReservasGitLive() }
    single { AutenticacionRepository(get(), get()) }
    single { PerfilPendienteRepository(get(), get()) }
    single { VinculacionRepository(get(), get(), get(), get(), get(), get(), get()) }
    single { SesionRepository(get()) }
    single { ReservaRepository(get(), get()) }
    single { HomeRepository(get(), get(), ::horaActualMilis) }
}

private var koinIniciado = false

/**
 * Arranca Koin una sola vez. La llamada es idempotente para que SwiftUI pueda
 * recrear el controlador sin relanzar el grafo.
 */
fun iniciarKoin() {
    if (!koinIniciado) {
        startKoin { modules(appModule) }
        koinIniciado = true
    }
}
