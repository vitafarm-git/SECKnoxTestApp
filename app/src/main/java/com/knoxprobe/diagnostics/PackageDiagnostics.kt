package com.knoxprobe.diagnostics

import android.content.Context
import com.knoxprobe.domain.PackageProbeResult

class PackageDiagnostics(private val context: Context) {
    private val pm = context.packageManager

    val presetPackages = listOf(
        "com.samsung.knox.securefolder",
        "com.samsung.android.knox.containercore",
        "com.android.vending",
        "com.google.android.gms"
    )

    fun runProbe(packages: List<String> = presetPackages): List<PackageProbeResult> {
        return packages.map { pkg ->
            val packageInfo = runCatching { pm.getPackageInfo(pkg, 0) }.getOrNull()
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            PackageProbeResult(
                packageName = pkg,
                visible = packageInfo != null,
                hasLaunchIntent = launchIntent != null,
                versionName = packageInfo?.versionName,
                versionCode = packageInfo?.longVersionCode,
                installerPackage = runCatching { pm.getInstallSourceInfo(pkg).installingPackageName }.getOrNull()
            )
        }
    }
}
