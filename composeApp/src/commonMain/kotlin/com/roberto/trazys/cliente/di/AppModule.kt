package com.roberto.trazys.cliente.di

import com.roberto.trazys.cliente.data.datastore.obtenerDataStore
import com.roberto.trazys.cliente.data.repository.PreferencesRepository
import org.koin.core.context.startKoin
import org.koin.dsl.module

/** Módulo Koin del Cliente. */
val appModule = module {
    single { PreferencesRepository(obtenerDataStore()) }
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
