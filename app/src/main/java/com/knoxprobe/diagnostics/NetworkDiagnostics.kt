package com.knoxprobe.diagnostics

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import com.knoxprobe.domain.ExternalProbeResult
import com.knoxprobe.domain.NetworkRecord
import com.knoxprobe.domain.NetworkSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class NetworkDiagnostics(context: Context) {
    private val cm = context.getSystemService(ConnectivityManager::class.java)

    fun captureNetworkSnapshot(): NetworkSnapshot {
        val active = cm.activeNetwork
        val allNetworks = cm.allNetworks.toList()
        val records = allNetworks.map { toRecord(it, it == active) }

        val hasVpn = records.any { it.hasVpnTransport }
        val hasNonVpn = records.any { !it.hasVpnTransport }

        val notes = mutableListOf<String>()
        if (hasVpn) notes += "Strong signal: VPN transport visible to app."
        if (records.any { (it.interfaceName ?: "").startsWith("tun") }) {
            notes += "Circumstantial signal: tun-like interface exposed by LinkProperties."
        }
        if (records.any { r -> r.routes.any { it.contains("tun") || it.contains("vpn") } }) {
            notes += "Circumstantial signal: route contains vpn-like interface naming."
        }
        if (hasVpn && hasNonVpn) notes += "App can see both VPN and non-VPN networks."
        if (notes.isEmpty()) notes += "No direct VPN artifact visible through official app-level APIs."

        return NetworkSnapshot(
            activeNetworkPresent = active != null,
            visibleNetworks = records,
            seesVpnAndNonVpn = hasVpn && hasNonVpn,
            interpretation = notes
        )
    }

    private fun toRecord(network: Network, isActive: Boolean): NetworkRecord {
        val caps = cm.getNetworkCapabilities(network)
        val link = cm.getLinkProperties(network)
        return NetworkRecord(
            id = network.toString(),
            isActive = isActive,
            transports = caps.transportStrings(),
            hasVpnTransport = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true,
            interfaceName = link?.interfaceName,
            mtu = link?.mtu,
            dnsServers = link?.dnsServers?.map { it.hostAddress ?: it.toString() } ?: emptyList(),
            routes = link.routeStrings(),
            proxy = link?.httpProxy?.toString()
        )
    }

    suspend fun runExternalProbes(endpoints: List<String>): List<ExternalProbeResult> = withContext(Dispatchers.IO) {
        endpoints.map { endpoint ->
            val start = System.currentTimeMillis()
            try {
                val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    requestMethod = "GET"
                }
                val code = conn.responseCode
                val body = conn.inputStream.bufferedReader().use { it.readText() }.take(256)
                ExternalProbeResult(
                    endpoint = endpoint,
                    success = true,
                    statusCode = code,
                    bodySnippet = body,
                    latencyMs = System.currentTimeMillis() - start,
                    timestampEpochMs = System.currentTimeMillis(),
                    note = "Default network probe; endpoint list is configurable in code."
                )
            } catch (e: Exception) {
                ExternalProbeResult(
                    endpoint = endpoint,
                    success = false,
                    statusCode = null,
                    bodySnippet = null,
                    latencyMs = System.currentTimeMillis() - start,
                    timestampEpochMs = System.currentTimeMillis(),
                    note = "Probe failed: ${e.message}"
                )
            }
        }
    }

    fun advancedProbeNote(): String =
        "TODO: Optional Network#bindSocket/ConnectivityManager.requestNetwork probe can be added later after Samsung-device reliability validation."

    private fun NetworkCapabilities?.transportStrings(): List<String> {
        if (this == null) return emptyList()
        val transports = mutableListOf<String>()
        if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) transports += "WIFI"
        if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) transports += "CELLULAR"
        if (hasTransport(NetworkCapabilities.TRANSPORT_VPN)) transports += "VPN"
        if (hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) transports += "ETHERNET"
        return transports
    }

    private fun LinkProperties?.routeStrings(): List<String> = this?.routes?.map { it.toString() } ?: emptyList()
}
