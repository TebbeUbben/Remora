package de.tebbeubben.remora

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import dagger.hilt.android.AndroidEntryPoint
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.ui.pairing.RemoraPairingScreen
import de.tebbeubben.remora.ui.commands.CommandDialog
import de.tebbeubben.remora.ui.commands.CommandType
import de.tebbeubben.remora.ui.overview.Overview
import de.tebbeubben.remora.ui.theme.RemoraTheme
import de.tebbeubben.remora.ui.welcome.WelcomeScreen
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var remoraLib: RemoraLib

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            RemoraTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = if (remoraLib.isPairedToMain) "screen_overview" else "screen_welcome"
                ) {
                    composable("screen_welcome") { backStackEntry ->
                        WelcomeScreen(
                            onContinue = {
                                navController.navigate("screen_pairing") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

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

                    composable("screen_overview") { backStackEntry ->
                        Overview(
                            onOpenCommandDialog = { commandType ->
                                if (commandType == null) {
                                    navController.navigate("dialog_command")
                                } else {
                                    navController.navigate("dialog_command?type=${commandType.name}")
                                }
                            }
                        )
                    }

                    dialog(
                        route = "dialog_command?type={type}",
                        arguments = listOf(
                            navArgument("type") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        ),
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern = "remora://dialog_command"
                            }
                        )
                    ) { backStackEntry ->
                        val type = backStackEntry.arguments?.getString("type")?.let { CommandType.valueOf(it) }
                        CommandDialog(
                            onDismiss = { navController.popBackStack() },
                            initialCommandType = type
                        )
                    }
                }
            }
        }
    }
}
