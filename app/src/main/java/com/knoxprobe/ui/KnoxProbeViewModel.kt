package com.knoxprobe.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.knoxprobe.diagnostics.NetworkDiagnostics
import com.knoxprobe.diagnostics.PackageDiagnostics
import com.knoxprobe.diagnostics.StorageDiagnostics
import com.knoxprobe.diagnostics.TimeLocationDiagnostics
import com.knoxprobe.domain.AppInfo
import com.knoxprobe.domain.DiagnosticSnapshot
import com.knoxprobe.domain.ExternalProbeResult
import com.knoxprobe.domain.LocationSnapshot
import com.knoxprobe.domain.NetworkSnapshot
import com.knoxprobe.domain.PackageProbeResult
import com.knoxprobe.domain.StorageSnapshot
import com.knoxprobe.domain.TimeSnapshot
import com.knoxprobe.export.SnapshotJsonExporter
import com.knoxprobe.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KnoxProbeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application.applicationContext
    private val networkDiagnostics = NetworkDiagnostics(app)
    private val timeLocationDiagnostics = TimeLocationDiagnostics(app)
    private val packageDiagnostics = PackageDiagnostics(app)
    private val storageDiagnostics = StorageDiagnostics(app)
    private val sessionManager = SessionManager(app)
    private val exporter = SnapshotJsonExporter(app)

    private val _uiState = MutableStateFlow(KnoxProbeUiState())
    val uiState: StateFlow<KnoxProbeUiState> = _uiState.asStateFlow()

    private var lastSnapshot: DiagnosticSnapshot? = null

    init {
        viewModelScope.launch { refreshAll() }
    }

    suspend fun refreshAll() {
        val appInfo = getAppInfo()
        val time = timeLocationDiagnostics.captureTime()
        val location = timeLocationDiagnostics.captureLocationSnapshot()
        val network = networkDiagnostics.captureNetworkSnapshot()
        val packages = packageDiagnostics.runProbe()
        val storage = storageDiagnostics.readCurrentMarkers()
        _uiState.value = _uiState.value.copy(
            appInfo = appInfo,
            sessionId = sessionManager.sessionId,
            permissionGranted = location.permissionGranted,
            timeSnapshot = time,
            locationSnapshot = location,
            networkSnapshot = network,
            packageProbeResults = packages,
            storageSnapshot = storage,
            infoMessage = "Ready"
        )
    }

    fun runFullSnapshot() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, infoMessage = "Running snapshot...")
            val network = networkDiagnostics.captureNetworkSnapshot()
            val probes = networkDiagnostics.runExternalProbes(DEFAULT_ENDPOINTS)
            val time = timeLocationDiagnostics.captureTime()
            val location = timeLocationDiagnostics.captureLocationSnapshot()
            val packages = packageDiagnostics.runProbe()
            val storage = storageDiagnostics.readCurrentMarkers()

            val snapshot = DiagnosticSnapshot(
                timestampEpochMs = System.currentTimeMillis(),
                sessionId = sessionManager.sessionId,
                appInfo = getAppInfo(),
                permissionSummary = permissionSummary(),
                timeSnapshot = time,
                locationSnapshot = location,
                networkSnapshot = network,
                externalProbes = probes,
                packageProbes = packages,
                storageSnapshot = storage,
                notes = listOf(
                    networkDiagnostics.advancedProbeNote(),
                    timeLocationDiagnostics.timezoneConsistencyNote(location) ?: "",
                    "This tool is diagnostic only and does not bypass security controls."
                ).filter { it.isNotBlank() }
            )
            lastSnapshot = snapshot
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                timeSnapshot = time,
                locationSnapshot = location,
                networkSnapshot = network,
                externalProbeResults = probes,
                packageProbeResults = packages,
                storageSnapshot = storage,
                infoMessage = "Snapshot completed"
            )
        }
    }

    fun regenerateMarkers() {
        _uiState.value = _uiState.value.copy(storageSnapshot = storageDiagnostics.regenerateMarkers())
    }

    fun onFileImported(message: String) {
        _uiState.value = _uiState.value.copy(
            storageSnapshot = storageDiagnostics.readCurrentMarkers(imported = message),
            infoMessage = message
        )
    }

    fun importFromUri(uri: android.net.Uri) {
        onFileImported(storageDiagnostics.importUserFile(uri))
    }

    fun buildSnapshotJson(): String? = lastSnapshot?.let(exporter::toJson)

    fun writeSnapshotToUri(uri: android.net.Uri): Boolean {
        val json = buildSnapshotJson() ?: return false
        exporter.writeToUri(uri, json)
        return true
    }

    fun writeTextToUri(uri: android.net.Uri, text: String) {
        exporter.writeToUri(uri, text)
    }

    private fun getAppInfo(): AppInfo {
        val info = app.packageManager.getPackageInfo(app.packageName, 0)
        return AppInfo(versionName = info.versionName ?: "unknown", versionCode = info.longVersionCode)
    }

    private fun permissionSummary(): Map<String, Boolean> = mapOf(
        Manifest.permission.ACCESS_COARSE_LOCATION to isGranted(Manifest.permission.ACCESS_COARSE_LOCATION),
        Manifest.permission.ACCESS_FINE_LOCATION to isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    )

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(app, permission) == PackageManager.PERMISSION_GRANTED

    companion object {
        // Replace endpoints for internal environments as needed.
        private val DEFAULT_ENDPOINTS = listOf(
            "https://api.ipify.org?format=json"
        )
    }
}

data class KnoxProbeUiState(
    val appInfo: AppInfo? = null,
    val sessionId: String = "",
    val permissionGranted: Boolean = false,
    val timeSnapshot: TimeSnapshot? = null,
    val locationSnapshot: LocationSnapshot? = null,
    val networkSnapshot: NetworkSnapshot? = null,
    val externalProbeResults: List<ExternalProbeResult> = emptyList(),
    val packageProbeResults: List<PackageProbeResult> = emptyList(),
    val storageSnapshot: StorageSnapshot? = null,
    val isLoading: Boolean = false,
    val infoMessage: String = ""
)
