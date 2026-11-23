package com.roberto.trazys.cliente.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.roberto.trazys.cliente.data.repository.PreferencesRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Login provisional (Fase 1A): guarda el DNI pendiente con
 * [PreferencesRepository] para validar Koin + corrutinas en UI.
 */
@Composable
fun LoginScreen(
    onEntrar: () -> Unit,
    onVolver: () -> Unit
) {
    val repositorio = koinInject<PreferencesRepository>()
    val alcance = rememberCoroutineScope()
    var dni by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Acceso", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Provisional: solo guarda el DNI en DataStore.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = dni,
            onValueChange = { dni = it },
            label = { Text(text = "DNI") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            enabled = dni.isNotBlank(),
            onClick = {
                alcance.launch {
                    repositorio.setDniPendiente(dni)
                    onEntrar()
                }
            }
        ) {
            Text(text = "Entrar")
        }
        TextButton(onClick = onVolver) {
            Text(text = "Volver")
        }
    }
}
