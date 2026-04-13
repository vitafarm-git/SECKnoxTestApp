package com.knoxprobe.diagnostics

import android.content.Context
import android.net.Uri
import com.knoxprobe.domain.StorageMarkerResult
import com.knoxprobe.domain.StorageSnapshot
import java.io.File

class StorageDiagnostics(private val context: Context) {

    fun regenerateMarkers(): StorageSnapshot {
        val markers = mutableListOf<StorageMarkerResult>()

        val internal = File(context.filesDir, "knoxprobe_internal_marker.txt")
        internal.writeText("marker=${System.currentTimeMillis()}")
        markers += internal.toMarker("App-private internal")

        val externalDir = context.getExternalFilesDir(null)
        if (externalDir != null) {
            val external = File(externalDir, "knoxprobe_external_marker.txt")
            external.writeText("marker=${System.currentTimeMillis()}")
            markers += external.toMarker("App-specific external")
        }

        return StorageSnapshot(markers = markers, importedFileInfo = null)
    }

    fun readCurrentMarkers(imported: String? = null): StorageSnapshot {
        val markers = mutableListOf<StorageMarkerResult>()
        markers += File(context.filesDir, "knoxprobe_internal_marker.txt").toMarker("App-private internal")

        context.getExternalFilesDir(null)?.let {
            markers += File(it, "knoxprobe_external_marker.txt").toMarker("App-specific external")
        }
        return StorageSnapshot(markers = markers, importedFileInfo = imported)
    }

    fun importUserFile(uri: Uri): String {
        val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
        return "Imported ${content.length} bytes from $uri"
    }

    private fun File.toMarker(label: String): StorageMarkerResult = StorageMarkerResult(
        locationLabel = label,
        path = absolutePath,
        exists = exists(),
        canRead = canRead(),
        canWrite = canWrite()
    )
}
