package com.roberto.trazys.cliente.ui.registro

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
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
import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ResultadoSesion
import com.roberto.trazys.cliente.data.repository.AutenticacionRepository
import com.roberto.trazys.cliente.data.repository.PreferencesRepository
import com.roberto.trazys.cliente.ui.auth.mensajeErrorAuth
import com.roberto.trazys.cliente.ui.components.BotonPrincipal
import com.roberto.trazys.cliente.ui.components.CampoContrasena
import com.roberto.trazys.cliente.ui.components.MensajeError
import com.roberto.trazys.cliente.ui.components.TituloAuth
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Alta de cuenta CLIENTE con contraseña repetida y aceptación de términos. */
@Composable
fun RegistroScreen(
    onEntrar: () -> Unit,
    onVolver: () -> Unit
) {
    val autenticacion = koinInject<AutenticacionRepository>()
    val preferencias = koinInject<PreferencesRepository>()
    val alcance = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var repetida by rememberSaveable { mutableStateOf("") }
    var visible by rememberSaveable { mutableStateOf(false) }
    var visibleRepetida by rememberSaveable { mutableStateOf(false) }
    var aceptaTerminos by rememberSaveable { mutableStateOf(false) }
    var cargando by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf("") }

    val formularioCompleto = email.isNotBlank() &&
        contrasena.length >= 6 &&
        repetida.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        TituloAuth(
            titulo = "Crear cuenta",
            subtitulo = "Registro de cliente"
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
                    etiqueta = "Contraseña (mínimo 6 caracteres)",
                    visible = visible,
                    onCambiarVisibilidad = { visible = !visible },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                CampoContrasena(
                    valor = repetida,
                    onValorChange = {
                        repetida = it
                        error = ""
                    },
                    etiqueta = "Repetir contraseña",
                    visible = visibleRepetida,
                    onCambiarVisibilidad = { visibleRepetida = !visibleRepetida },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = aceptaTerminos,
                        onCheckedChange = {
                            aceptaTerminos = it
                            error = ""
                        }
                    )
                    Text(
                        text = "Acepto los términos de uso y la política de privacidad.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                MensajeError(texto = error, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))

                BotonPrincipal(
                    texto = "Crear cuenta",
                    onClick = {
                        when {
                            contrasena != repetida -> {
                                error = mensajeErrorAuth(
                                    ErrorAuth.CONTRASENAS_NO_COINCIDEN
                                )
                            }

                            !aceptaTerminos -> {
                                error = "Debes aceptar los términos para crear la cuenta."
                            }

                            else -> alcance.launch {
                                cargando = true
                                error = ""
                                when (val resultado =
                                    autenticacion.registrar(email, contrasena)) {
                                    ResultadoSesion.Exito -> {
                                        runCatching {
                                            autenticacion.uidActual()?.let { uid ->
                                                preferencias.guardarAceptacionTerminos(uid)
                                            }
                                        }
                                        cargando = false
                                        onEntrar()
                                    }

                                    is ResultadoSesion.Fallo -> {
                                        error = mensajeErrorAuth(resultado.error)
                                        cargando = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = formularioCompleto && aceptaTerminos,
                    cargando = cargando,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(onClick = onVolver, enabled = !cargando) {
                    Text(text = "Ya tengo cuenta")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
