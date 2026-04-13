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
import com.knoxprobe.domain.ExternalProbeResult
import com.knoxprobe.domain.NetworkSnapshot

@Composable
fun NetworkScreen(network: NetworkSnapshot?, probes: List<ExternalProbeResult>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Network diagnostics", style = MaterialTheme.typography.headlineSmall)
        Text("Active network: ${network?.activeNetworkPresent}")
        Text("Sees VPN + non-VPN: ${network?.seesVpnAndNonVpn}")
        network?.interpretation?.forEach { Text("• $it") }

        network?.visibleNetworks?.forEach { record ->
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Network ${record.id}", style = MaterialTheme.typography.titleSmall)
                    Text("Active: ${record.isActive}")
                    Text("Transports: ${record.transports.joinToString()}")
                    Text("VPN transport visible: ${record.hasVpnTransport}")
                    Text("Interface: ${record.interfaceName ?: "N/A"}")
                    Text("MTU: ${record.mtu ?: "N/A"}")
                    Text("DNS: ${record.dnsServers.joinToString()}")
                    Text("Routes: ${record.routes.joinToString()}")
                    Text("Proxy: ${record.proxy ?: "N/A"}")
                }
            }
        }

        Text("External probes", style = MaterialTheme.typography.titleMedium)
        probes.forEach {
            Text("${it.endpoint} -> success=${it.success}, code=${it.statusCode}, latency=${it.latencyMs}ms")
            Text("Body: ${it.bodySnippet ?: "(none)"}")
        }
    }
}
