package com.roberto.trazys.cliente.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roberto.trazys.cliente.ui.home.HomeScreen
import com.roberto.trazys.cliente.ui.inicio.InicioScreen
import com.roberto.trazys.cliente.ui.login.LoginScreen

/**
 * Grafo provisional de la Fase 1A: tres rutas para validar Navigation KMP.
 * Las pantallas reales sustituirán a estas en la Fase 3.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.INICIO) {
        composable(Routes.INICIO) {
            InicioScreen(
                onContinuar = { navController.navigate(Routes.LOGIN) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onEntrar = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.INICIO) { inclusive = true }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onSalir = {
                    navController.navigate(Routes.INICIO) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
