package com.roberto.trazys.cliente.ui.recuperar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.roberto.trazys.cliente.ui.components.MensajeError
import com.roberto.trazys.cliente.ui.components.TituloAuth
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Recuperación de contraseña por correo; no revela si la cuenta existe. */
@Composable
fun RecuperarPasswordScreen(onVolver: () -> Unit) {
    val autenticacion = koinInject<AutenticacionRepository>()
    val alcance = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var cargando by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf("") }
    var enviado by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        TituloAuth(
            titulo = "Recuperar contraseña",
            subtitulo = "Te enviaremos un enlace al correo indicado."
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                error = ""
                enviado = false
            },
            label = { Text(text = "Correo electrónico") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (enviado) {
            Text(
                text = "Si el correo está registrado, recibirás instrucciones en breve.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            MensajeError(texto = error)
        }

        Spacer(modifier = Modifier.height(16.dp))
        BotonPrincipal(
            texto = "Enviar correo",
            onClick = {
                alcance.launch {
                    cargando = true
                    error = ""
                    when (val resultado = autenticacion.recuperarContrasena(email)) {
                        ResultadoSesion.Exito -> enviado = true
                        is ResultadoSesion.Fallo -> {
                            error = mensajeErrorAuth(resultado.error)
                        }
                    }
                    cargando = false
                }
            },
            enabled = email.isNotBlank(),
            cargando = cargando,
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(onClick = onVolver, enabled = !cargando) {
            Text(text = "Volver")
        }
    }
}
