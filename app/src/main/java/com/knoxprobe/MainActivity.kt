package com.knoxprobe

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.knoxprobe.ui.KnoxProbeViewModel
import com.knoxprobe.ui.screens.AppsScreen
import com.knoxprobe.ui.screens.NetworkScreen
import com.knoxprobe.ui.screens.OverviewScreen
import com.knoxprobe.ui.screens.StorageScreen
import com.knoxprobe.ui.theme.KnoxProbeTheme

class MainActivity : ComponentActivity() {
    private val viewModel: KnoxProbeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KnoxProbeTheme {
                AppRoot(viewModel)
            }
        }
    }
}

@Composable
private fun AppRoot(viewModel: KnoxProbeViewModel) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { viewModel.runFullSnapshot() }
    )
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> if (uri != null) viewModel.writeSnapshotToUri(uri) }
    )
    val markerExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            uri?.let {
                val content = state.storageSnapshot?.markers?.joinToString("\n") { m -> "${m.locationLabel}: ${m.path}" } ?: ""
                viewModel.writeTextToUri(it, content)
            }
        }
    )
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) viewModel.importFromUri(uri) }
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val route = backStack?.destination?.route
                navItems().forEach { item ->
                    NavigationBarItem(
                        selected = route == item.route,
                        onClick = { navController.navigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "overview", modifier = Modifier.padding(padding)) {
            composable("overview") {
                OverviewScreen(
                    state = state,
                    onRunSnapshot = viewModel::runFullSnapshot,
                    onRequestLocation = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    },
                    onExportSnapshot = { exportLauncher.launch("knoxprobe_snapshot.json") }
                )
            }
            composable("network") { NetworkScreen(state.networkSnapshot, state.externalProbeResults) }
            composable("apps") { AppsScreen(state.packageProbeResults) }
            composable("storage") {
                StorageScreen(
                    snapshot = state.storageSnapshot,
                    onRegenerate = viewModel::regenerateMarkers,
                    onExportMarkers = { markerExportLauncher.launch("knoxprobe_markers.txt") },
                    onImport = { importLauncher.launch(arrayOf("*/*")) }
                )
            }
        }
    }
}

data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private fun navItems() = listOf(
    NavItem("overview", "Overview", Icons.Default.Home),
    NavItem("network", "Network", Icons.Default.NetworkCheck),
    NavItem("apps", "Apps", Icons.AutoMirrored.Filled.List),
    NavItem("storage", "Storage", Icons.Default.Folder)
)
