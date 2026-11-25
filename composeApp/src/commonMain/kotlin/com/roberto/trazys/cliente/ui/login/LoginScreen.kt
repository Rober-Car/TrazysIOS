package com.roberto.trazys.cliente.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roberto.trazys.cliente.data.error.ResultadoSesion
import com.roberto.trazys.cliente.data.repository.AutenticacionRepository
import com.roberto.trazys.cliente.ui.auth.mensajeErrorAuth
import com.roberto.trazys.cliente.ui.components.BotonPrincipal
import com.roberto.trazys.cliente.ui.components.CampoContrasena
import com.roberto.trazys.cliente.ui.components.MensajeError
import com.roberto.trazys.cliente.ui.components.TituloAuth
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Inicio de sesión real contra Firebase Authentication. */
@Composable
fun LoginScreen(
    onEntrar: () -> Unit,
    onRegistro: () -> Unit,
    onRecuperar: () -> Unit,
    onVolver: () -> Unit
) {
    val autenticacion = koinInject<AutenticacionRepository>()
    val alcance = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var visible by rememberSaveable { mutableStateOf(false) }
    var cargando by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        TituloAuth(
            titulo = "Trazys",
            subtitulo = "Acceso de clientes"
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        error = ""
                    },
                    label = { Text(text = "Correo electrónico") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                CampoContrasena(
                    valor = contrasena,
                    onValorChange = {
                        contrasena = it
                        error = ""
                    },
                    etiqueta = "Contraseña",
                    visible = visible,
                    onCambiarVisibilidad = { visible = !visible },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                MensajeError(texto = error, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))
                BotonPrincipal(
                    texto = "Entrar",
                    onClick = {
                        alcance.launch {
                            cargando = true
                            error = ""
                            when (val resultado =
                                autenticacion.iniciarSesion(email, contrasena)) {
                                ResultadoSesion.Exito -> {
                                    cargando = false
                                    onEntrar()
                                }

                                is ResultadoSesion.Fallo -> {
                                    error = mensajeErrorAuth(resultado.error)
                                    cargando = false
                                }
                            }
                        }
                    },
                    enabled = email.isNotBlank() && contrasena.isNotBlank(),
                    cargando = cargando,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(onClick = onRecuperar, enabled = !cargando) {
                    Text(text = "¿Has olvidado tu contraseña?")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRegistro, enabled = !cargando) {
            Text(text = "¿No tienes cuenta? Crear una")
        }
        TextButton(onClick = onVolver, enabled = !cargando) {
            Text(text = "Volver")
        }
    }
}
