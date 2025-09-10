package de.tebbeubben.remora.lib.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.tebbeubben.remora.lib.R
import de.tebbeubben.remora.lib.ui.configuration.ConfigurationScreen
import de.tebbeubben.remora.lib.ui.configuration.ConfigurationViewModel
import de.tebbeubben.remora.lib.ui.follower_management.FollowerManagementScreen
import de.tebbeubben.remora.lib.ui.follower_management.FollowerManagementViewModel
import de.tebbeubben.remora.lib.ui.pairing.PairingScreen
import de.tebbeubben.remora.lib.ui.pairing.PairingViewModel
import de.tebbeubben.remora.lib.ui.theme.RemoraTheme

class RemoraLibActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemoraTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val snackbarHostState = remember { SnackbarHostState() }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(titleForDestination(currentDestination)) },
                navigationIcon = {
                    val activity = LocalActivity.current
                    IconButton(onClick = {
                        if (!navController.navigateUp()) {
                            activity?.finish()
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_24px),
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "followers",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("configuration") { backStackEntry ->
                val viewModel: ConfigurationViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = ConfigurationViewModel.Factory
                )
                val activity = LocalActivity.current
                ConfigurationScreen(
                    viewModel = viewModel,
                    onClose = {
                        if (!navController.navigateUp()) {
                            activity?.finish()
                        }
                    }
                )
            }
            composable(
                "pairing?peerId={peerId}",
                arguments = listOf(navArgument("peerId") {
                    type = NavType.LongType
                })
            ) { backStackEntry ->
                val peerId = backStackEntry.arguments?.getLong("peerId", -1)
                val viewModel: PairingViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = PairingViewModel.provideFactory(peerId)
                )
                val activity = LocalActivity.current
                PairingScreen(
                    viewModel = viewModel,
                    onClose = {
                        if (!navController.navigateUp()) {
                            activity?.finish()
                        }
                    }
                )
            }
            composable("followers") { backStackEntry ->
                val viewModel: FollowerManagementViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = FollowerManagementViewModel.Factory
                )
                FollowerManagementScreen(
                    viewModel = viewModel,
                    onStartPairing = { id -> navController.navigate("pairing?peerId=${id ?: -1}") },
                    startConfiguration = { navController.navigate("configuration") }
                )
            }
        }
    }
}

@Composable
private fun titleForDestination(dest: NavDestination?): String {
    val r = dest?.route ?: return "App"
    return when {
        r.startsWith("configuration") -> "Firebase Configuration"
        r.startsWith("pairing") -> "Pair New Device"
        r.startsWith("followers") -> "Manage Followers"
        else -> "App"
    }
}