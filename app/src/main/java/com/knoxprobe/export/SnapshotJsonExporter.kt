package com.knoxprobe.export

import android.content.Context
import android.net.Uri
import com.knoxprobe.domain.DiagnosticSnapshot
import com.knoxprobe.util.toJsonArray
import org.json.JSONArray
import org.json.JSONObject

class SnapshotJsonExporter(private val context: Context) {
    fun toJson(snapshot: DiagnosticSnapshot): String {
        return JSONObject().apply {
            put("timestampEpochMs", snapshot.timestampEpochMs)
            put("sessionId", snapshot.sessionId)
            put("appInfo", JSONObject().apply {
                put("versionName", snapshot.appInfo.versionName)
                put("versionCode", snapshot.appInfo.versionCode)
            })
            put("permissions", JSONObject(snapshot.permissionSummary))
            put("time", JSONObject().apply {
                put("localTimeIso", snapshot.timeSnapshot.localTimeIso)
                put("timezoneId", snapshot.timeSnapshot.timezoneId)
                put("utcOffsetMinutes", snapshot.timeSnapshot.utcOffsetMinutes)
                put("elapsedRealtimeMs", snapshot.timeSnapshot.elapsedRealtimeMs)
            })
            put("location", snapshot.locationSnapshot?.let {
                JSONObject().apply {
                    put("available", it.available)
                    put("permissionGranted", it.permissionGranted)
                    put("latitude", it.latitude)
                    put("longitude", it.longitude)
                    put("accuracyMeters", it.accuracyMeters)
                    put("provider", it.provider)
                    put("timestampEpochMs", it.timestampEpochMs)
                    put("note", it.note)
                }
            })
            put("network", JSONObject().apply {
                put("activeNetworkPresent", snapshot.networkSnapshot.activeNetworkPresent)
                put("seesVpnAndNonVpn", snapshot.networkSnapshot.seesVpnAndNonVpn)
                put("interpretation", snapshot.networkSnapshot.interpretation.toJsonArray())
                put("visibleNetworks", JSONArray().apply {
                    snapshot.networkSnapshot.visibleNetworks.forEach { record ->
                        put(JSONObject().apply {
                            put("id", record.id)
                            put("isActive", record.isActive)
                            put("transports", record.transports.toJsonArray())
                            put("hasVpnTransport", record.hasVpnTransport)
                            put("interfaceName", record.interfaceName)
                            put("mtu", record.mtu)
                            put("dnsServers", record.dnsServers.toJsonArray())
                            put("routes", record.routes.toJsonArray())
                            put("proxy", record.proxy)
                        })
                    }
                })
            })
            put("externalProbes", JSONArray().apply {
                snapshot.externalProbes.forEach { probe ->
                    put(JSONObject().apply {
                        put("endpoint", probe.endpoint)
                        put("success", probe.success)
                        put("statusCode", probe.statusCode)
                        put("bodySnippet", probe.bodySnippet)
                        put("latencyMs", probe.latencyMs)
                        put("timestampEpochMs", probe.timestampEpochMs)
                        put("note", probe.note)
                    })
                }
            })
            put("packageProbes", JSONArray().apply {
                snapshot.packageProbes.forEach { probe ->
                    put(JSONObject().apply {
                        put("packageName", probe.packageName)
                        put("visible", probe.visible)
                        put("hasLaunchIntent", probe.hasLaunchIntent)
                        put("versionName", probe.versionName)
                        put("versionCode", probe.versionCode)
                        put("installerPackage", probe.installerPackage)
                    })
                }
            })
            put("storage", JSONObject().apply {
                put("markers", JSONArray().apply {
                    snapshot.storageSnapshot.markers.forEach { marker ->
                        put(JSONObject().apply {
                            put("locationLabel", marker.locationLabel)
                            put("path", marker.path)
                            put("exists", marker.exists)
                            put("canRead", marker.canRead)
                            put("canWrite", marker.canWrite)
                        })
                    }
                })
                put("importedFileInfo", snapshot.storageSnapshot.importedFileInfo)
            })
            put("notes", snapshot.notes.toJsonArray())
        }.toString(2)
    }

    fun writeToUri(uri: Uri, payload: String) {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(payload) }
    }
}
