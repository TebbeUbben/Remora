package de.tebbeubben.remora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.model.StatusView
import de.tebbeubben.remora.lib.ui.pairing.RemoraPairingScreen
import de.tebbeubben.remora.ui.overview.Overview
import de.tebbeubben.remora.ui.theme.RemoraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            RemoraTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = if (RemoraLib.instance.isPairedToMain) "overview" else "pairing"
                ) {
                    composable("pairing") { backStackEntry ->
                        RemoraPairingScreen(
                            viewModelStoreOwner = backStackEntry,
                            onClose = {
                                navController.navigate("overview") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("overview") {
                        val flow = remember {
                            RemoraLib.instance.statusFlow
                                .flatMapLatest {
                                    flow {
                                        do {
                                            emit(Clock.System.now() to it)
                                            delay(15.seconds)
                                        } while (true)
                                    }
                                }
                        }
                        val status by flow.collectAsState(Instant.fromEpochSeconds(0) to StatusView(null, null, 0))
                        val fullStatus = status.second.full ?: return@composable
                        Overview(
                            currentTime = status.first,
                            statusData = fullStatus.data
                        )
                    }
                }
            }
        }
    }
}
