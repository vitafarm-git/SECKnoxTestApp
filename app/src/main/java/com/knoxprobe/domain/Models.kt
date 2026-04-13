package com.knoxprobe.domain

data class AppInfo(
    val versionName: String,
    val versionCode: Long
)

data class TimeSnapshot(
    val localTimeIso: String,
    val timezoneId: String,
    val utcOffsetMinutes: Int,
    val elapsedRealtimeMs: Long
)

data class LocationSnapshot(
    val available: Boolean,
    val permissionGranted: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val provider: String? = null,
    val timestampEpochMs: Long? = null,
    val note: String? = null
)

data class NetworkRecord(
    val id: String,
    val isActive: Boolean,
    val transports: List<String>,
    val hasVpnTransport: Boolean,
    val interfaceName: String?,
    val mtu: Int?,
    val dnsServers: List<String>,
    val routes: List<String>,
    val proxy: String?
)

data class NetworkSnapshot(
    val activeNetworkPresent: Boolean,
    val visibleNetworks: List<NetworkRecord>,
    val seesVpnAndNonVpn: Boolean,
    val interpretation: List<String>
)

data class ExternalProbeResult(
    val endpoint: String,
    val success: Boolean,
    val statusCode: Int?,
    val bodySnippet: String?,
    val latencyMs: Long,
    val timestampEpochMs: Long,
    val note: String?
)

data class PackageProbeResult(
    val packageName: String,
    val visible: Boolean,
    val hasLaunchIntent: Boolean,
    val versionName: String?,
    val versionCode: Long?,
    val installerPackage: String?
)

data class StorageMarkerResult(
    val locationLabel: String,
    val path: String,
    val exists: Boolean,
    val canRead: Boolean,
    val canWrite: Boolean
)

data class StorageSnapshot(
    val markers: List<StorageMarkerResult>,
    val importedFileInfo: String?
)

data class DiagnosticSnapshot(
    val timestampEpochMs: Long,
    val sessionId: String,
    val appInfo: AppInfo,
    val permissionSummary: Map<String, Boolean>,
    val timeSnapshot: TimeSnapshot,
    val locationSnapshot: LocationSnapshot?,
    val networkSnapshot: NetworkSnapshot,
    val externalProbes: List<ExternalProbeResult>,
    val packageProbes: List<PackageProbeResult>,
    val storageSnapshot: StorageSnapshot,
    val notes: List<String>
)
