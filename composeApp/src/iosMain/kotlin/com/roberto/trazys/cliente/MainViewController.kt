package com.roberto.trazys.cliente

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Punto de entrada iOS: envuelve el composable compartido App() en un
 * UIViewController que SwiftUI consume desde ContentView.
 */
fun MainViewController(): UIViewController =
    ComposeUIViewController { App() }
