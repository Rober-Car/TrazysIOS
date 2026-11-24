package com.roberto.trazys.cliente.di

import com.roberto.trazys.cliente.data.datastore.obtenerDataStore
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacionGitLive
import com.roberto.trazys.cliente.data.firebase.FuenteUsuarios
import com.roberto.trazys.cliente.data.firebase.FuenteUsuariosGitLive
import com.roberto.trazys.cliente.data.repository.AutenticacionRepository
import com.roberto.trazys.cliente.data.repository.PreferencesRepository
import org.koin.core.context.startKoin
import org.koin.dsl.module

/** Módulo Koin del Cliente. */
val appModule = module {
    single { PreferencesRepository(obtenerDataStore()) }
    single<FuenteAutenticacion> { FuenteAutenticacionGitLive() }
    single<FuenteUsuarios> { FuenteUsuariosGitLive() }
    single { AutenticacionRepository(get(), get()) }
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
