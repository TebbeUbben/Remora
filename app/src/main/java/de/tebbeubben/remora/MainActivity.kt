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
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.model.status.StatusView
import de.tebbeubben.remora.lib.ui.pairing.RemoraPairingScreen
import de.tebbeubben.remora.ui.commands.CommandDialog
import de.tebbeubben.remora.ui.commands.CommandType
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
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            RemoraTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = if (RemoraLib.instance.isPairedToMain) "screen_overview" else "screen_pairing"
                ) {
                    composable("screen_pairing") { backStackEntry ->
                        RemoraPairingScreen(
                            viewModelStoreOwner = backStackEntry,
                            onClose = {
                                navController.navigate("screen_overview") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("screen_overview") {
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
                            statusData = fullStatus.data,
                            onPressBolusButton = { navController.navigate("dialog_command") }
                        )
                    }

                    dialog("dialog_command") {
                        CommandDialog(
                            viewModelStoreOwner = it,
                            onDismiss = { navController.popBackStack() },
                            initialCommandType = CommandType.BOLUS
                        )
                    }
                }
            }
        }
    }
}
