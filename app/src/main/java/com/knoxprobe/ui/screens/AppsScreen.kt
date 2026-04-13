package com.knoxprobe.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knoxprobe.domain.PackageProbeResult

@Composable
fun AppsScreen(packages: List<PackageProbeResult>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Package visibility probes", style = MaterialTheme.typography.headlineSmall)
        Text("Only a small predefined list is checked. Android package visibility rules may hide apps.")
        packages.forEach { probe ->
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(probe.packageName, style = MaterialTheme.typography.titleSmall)
                    Text("Visible: ${probe.visible}")
                    Text("Launch intent: ${probe.hasLaunchIntent}")
                    Text("Version: ${probe.versionName ?: "N/A"} (${probe.versionCode ?: 0})")
                    Text("Installer: ${probe.installerPackage ?: "N/A"}")
                }
            }
        }
    }
}
