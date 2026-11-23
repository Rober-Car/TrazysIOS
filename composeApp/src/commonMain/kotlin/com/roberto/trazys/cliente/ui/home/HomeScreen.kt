package com.roberto.trazys.cliente.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.roberto.trazys.cliente.data.repository.PreferencesRepository
import com.roberto.trazys.cliente.time.horaActualMilis
import com.roberto.trazys.cliente.util.formatearFechaCorta
import org.koin.compose.koinInject

/**
 * Home provisional (Fase 1A): lee preferencias con Koin, carga el logo con
 * Coil y formatea la fecha actual.
 */
@Composable
fun HomeScreen(onSalir: () -> Unit) {
    val repositorio = koinInject<PreferencesRepository>()
    val nombreNegocio by repositorio.nombreNegocio.collectAsState(initial = "")
    val logoNegocio by repositorio.logoNegocio.collectAsState(initial = "")
    val dniPendiente by repositorio.dniPendiente.collectAsState(initial = null)
    val fecha = remember { formatearFechaCorta(horaActualMilis()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (logoNegocio.isNotBlank()) {
            AsyncImage(
                model = logoNegocio,
                contentDescription = "Logo del negocio",
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            text = nombreNegocio.ifBlank { "Negocio sin vincular" },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "DNI: ${dniPendiente ?: "no guardado"}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Hoy: $fecha",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onSalir) {
            Text(text = "Salir")
        }
    }
}
