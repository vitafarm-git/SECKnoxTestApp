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
import com.knoxprobe.domain.StorageSnapshot

@Composable
fun StorageScreen(
    snapshot: StorageSnapshot?,
    onRegenerate: () -> Unit,
    onExportMarkers: () -> Unit,
    onImport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Storage diagnostics", style = MaterialTheme.typography.headlineSmall)
        Text("App-private and app-specific external storage are distinct from user-granted SAF document access.")
        snapshot?.markers?.forEach { marker ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(marker.locationLabel, style = MaterialTheme.typography.titleSmall)
                    Text(marker.path)
                    Text("exists=${marker.exists} read=${marker.canRead} write=${marker.canWrite}")
                }
            }
        }
        snapshot?.importedFileInfo?.let { Text(it) }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onRegenerate) { Text("Regenerate markers") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onExportMarkers) { Text("Export markers (SAF)") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onImport) { Text("Import file (SAF)") }
    }
}
