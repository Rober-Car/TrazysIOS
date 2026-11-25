package com.roberto.trazys.cliente.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roberto.trazys.cliente.data.repository.AutenticacionRepository
import com.roberto.trazys.cliente.data.repository.PreferencesRepository
import com.roberto.trazys.cliente.ui.home.HomeScreen
import com.roberto.trazys.cliente.ui.inicio.InicioScreen
import com.roberto.trazys.cliente.ui.login.LoginScreen
import com.roberto.trazys.cliente.ui.recuperar.RecuperarPasswordScreen
import com.roberto.trazys.cliente.ui.registro.RegistroScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Grafo de acceso del Cliente. Si Firebase conserva la sesión, arranca en Home;
 * si no, en la pantalla de entrada.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val autenticacion = koinInject<AutenticacionRepository>()
    val preferencias = koinInject<PreferencesRepository>()
    val alcance = rememberCoroutineScope()
    val destinoInicial = remember {
        if (autenticacion.haySesionActiva()) Routes.HOME else Routes.INICIO
    }

    NavHost(navController = navController, startDestination = destinoInicial) {
        composable(Routes.INICIO) {
            InicioScreen(
                onContinuar = { navController.navigate(Routes.LOGIN) },
                onRegistro = { navController.navigate(Routes.REGISTRO) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onEntrar = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.INICIO) { inclusive = true }
                    }
                },
                onRegistro = { navController.navigate(Routes.REGISTRO) },
                onRecuperar = {
                    navController.navigate(Routes.RECUPERAR_PASSWORD)
                },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Routes.REGISTRO) {
            RegistroScreen(
                onEntrar = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.INICIO) { inclusive = true }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Routes.RECUPERAR_PASSWORD) {
            RecuperarPasswordScreen(onVolver = { navController.popBackStack() })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onSalir = {
                    alcance.launch {
                        runCatching { autenticacion.cerrarSesion() }
                        runCatching { preferencias.limpiarSesion() }
                        navController.navigate(Routes.INICIO) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
