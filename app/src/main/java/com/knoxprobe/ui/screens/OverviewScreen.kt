package com.knoxprobe.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knoxprobe.ui.KnoxProbeUiState

@Composable
fun OverviewScreen(
    state: KnoxProbeUiState,
    onRunSnapshot: () -> Unit,
    onRequestLocation: () -> Unit,
    onExportSnapshot: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("KnoxProbe", style = MaterialTheme.typography.headlineSmall)
        Text("Version: ${state.appInfo?.versionName ?: "-"}")
        Text("Session ID: ${state.sessionId}")
        Text("Location permission granted: ${state.permissionGranted}")

        SummaryCard("Active network", state.networkSnapshot?.activeNetworkPresent?.toString() ?: "Unknown")
        SummaryCard("VPN signals", state.networkSnapshot?.interpretation?.joinToString(" | ") ?: "No data")
        SummaryCard("External IP probe", state.externalProbeResults.firstOrNull()?.bodySnippet ?: "Not run")
        SummaryCard("Time/Timezone", "${state.timeSnapshot?.localTimeIso}\n${state.timeSnapshot?.timezoneId}")
        SummaryCard("Package probe", "${state.packageProbeResults.count { it.visible }} visible / ${state.packageProbeResults.size}")
        SummaryCard("Storage probe", "${state.storageSnapshot?.markers?.size ?: 0} marker checks")

        Button(modifier = Modifier.fillMaxWidth(), onClick = onRunSnapshot) { Text("Run full snapshot") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onExportSnapshot) { Text("Export snapshot") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onRequestLocation) { Text("Request location permission") }

        Text(state.infoMessage)
    }
}

@Composable
private fun SummaryCard(title: String, body: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body)
        }
    }
}
