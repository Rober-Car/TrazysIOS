package com.roberto.trazys.cliente

import androidx.compose.ui.window.ComposeUIViewController
import com.roberto.trazys.cliente.di.iniciarKoin
import platform.UIKit.UIViewController

/**
 * Punto de entrada iOS: arranca Koin y envuelve el composable compartido App()
 * en un UIViewController que SwiftUI consume desde ContentView.
 */
fun MainViewController(): UIViewController {
    iniciarKoin()
    return ComposeUIViewController { App() }
}
