package de.tebbeubben.remora.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import de.tebbeubben.remora.R
import de.tebbeubben.remora.lib.model.commands.RemoraCommand
import de.tebbeubben.remora.ui.commands.CommandType
import de.tebbeubben.remora.ui.overview.graphs.OverviewGraphs
import de.tebbeubben.remora.ui.theme.LocalExtendedColors
import de.tebbeubben.remora.util.formatBG
import de.tebbeubben.remora.util.toMinimalLocalizedString
import de.tebbeubben.remora.util.toRelativeString
import kotlinx.coroutines.awaitCancellation
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Composable
fun Overview(
    onOpenCommandDialog: (commandType: CommandType?) -> Unit,
) {

    val viewModel = hiltViewModel<OverviewViewModel>()
    val state by viewModel.statusState.collectAsStateWithLifecycle()
    val (currentTime, statusView) = state ?: return

    if (statusView.full == null) {
        MissingDataScreen()
        return
    }

    val statusData = statusView.full!!.data
    val commandState by viewModel.commandState.collectAsStateWithLifecycle()

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.setActive(true)
            try {
                awaitCancellation()
            } finally {
                viewModel.setActive(false)
            }
        }
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
        bottomBar = {
            FlexibleBottomAppBar(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                /*IconButton(
                    onClick = {},
                    colors = IconButtonDefaults.iconButtonColors().copy(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.settings_24px),
                        contentDescription = "Settings"
                    )
                }*/

                /*FilledIconButton(
                    onClick = {},
                    colors = IconButtonDefaults.filledIconButtonColors().copy(
                        containerColor = LocalExtendedColors.current.carbs.colorContainer,
                        contentColor = LocalExtendedColors.current.carbs.onColorContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.grain_24px),
                        contentDescription = "Enter Carbs"
                    )
                }*/

                Spacer(Modifier.width(40.dp))

                if (commandState != null) {
                    FilledTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenCommandDialog(null) },
                    ) {
                        @Suppress("SENSELESS_NULL_IN_WHEN")
                        Text(
                            text = when (commandState) {
                                is RemoraCommand.Final       -> when ((commandState as RemoraCommand.Final).result) {
                                    is RemoraCommand.Result.Error -> "Command failed"
                                    is RemoraCommand.Result.Success -> "Command was successful"
                                }

                                is RemoraCommand.Initial     -> "Validating command…"
                                is RemoraCommand.Prepared    -> "Waiting for user confirmation"
                                is RemoraCommand.Progressing -> when (val progress = (commandState as RemoraCommand.Progressing).progress) {
                                    is RemoraCommand.Progress.Connecting -> "Connecting to pump…"
                                    RemoraCommand.Progress.Enqueued      -> "Command is waiting in queue…"
                                    is RemoraCommand.Progress.Percentage -> "Progress: ${progress.percent}%"
                                }

                                is RemoraCommand.Rejected    -> "Command failed"
                                null                         -> error("Unreachable")
                            }
                        )
                    }
                } else {
                    FilledIconButton(
                        onClick = { onOpenCommandDialog(CommandType.BOLUS) },
                        colors = IconButtonDefaults.filledIconButtonColors().copy(
                            containerColor = LocalExtendedColors.current.bolus.colorContainer,
                            contentColor = LocalExtendedColors.current.bolus.onColorContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.syringe_24px),
                            contentDescription = "Deliver Bolus"
                        )
                    }
                }

                Spacer(Modifier.width(40.dp))

                /*FilledIconButton(
                    onClick = {},
                    /* TODO
                    colors = IconButtonDefaults.filledIconButtonColors().copy(
                        containerColor = LocalExtendedColors.current.carbs.colorContainer,
                        contentColor = LocalExtendedColors.current.carbs.onColorContainer
                    )*/
                ) {
                    Icon(
                        painter = painterResource(R.drawable.calculate_24px),
                        contentDescription = "Bolus Calculator"
                    )
                }

                IconButton(
                    onClick = {},
                    colors = IconButtonDefaults.iconButtonColors().copy(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert_24px),
                        contentDescription = "More"
                    )
                }*/
            }
        }
    ) { paddingValues ->

        val scrollState = rememberScrollState()

        OverviewLayout(
            modifier = Modifier
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxSize(),
            scrollState = scrollState
        ) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            val updated = (statusData.short.timestamp - currentTime).toRelativeString()
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.last_updated, updated),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            RibbonBar(
                modifier = Modifier.fillMaxWidth(),
                statusData = statusData,
                currentTime = currentTime
            )

            Spacer(Modifier.height(16.dp))

            statusData.short.displayBg?.let { displayBg ->
                val bg = (displayBg.smoothedValue ?: displayBg.value)

                val deltas = displayBg.deltas?.let { deltas ->
                    Triple(
                        first = (if (deltas.delta >= 0) "+" else "") + deltas.delta.formatBG(statusData.short.usesMgdl),
                        second = (if (deltas.shortAverageDelta >= 0) "+" else "") + deltas.shortAverageDelta.formatBG(statusData.short.usesMgdl),
                        third = (if (deltas.longAverageDelta >= 0) "+" else "") + deltas.longAverageDelta.formatBG(statusData.short.usesMgdl)
                    )
                }

                val runningModeRemainingDuration = statusData.short.runningModeDuration?.let { duration ->
                    val end = statusData.short.runningModeStart + duration
                    val remainingDuration = end - currentTime
                    if (remainingDuration >= 1.days) null
                    else remainingDuration.toMinimalLocalizedString()
                }

                GlucoseStatus(
                    modifier = Modifier.fillMaxWidth(),
                    value = bg.formatBG(statusData.short.usesMgdl),
                    isStale = (currentTime - displayBg.timestamp) >= 9.minutes,
                    bgClassification = when {
                        bg > statusData.short.highBgThreshold -> BgClassification.ABOVE_RANGE
                        bg < statusData.short.lowBgThreshold  -> BgClassification.BELOW_RANGE
                        else                                  -> BgClassification.IN_RANGE
                    },
                    trendArrow = displayBg.trendArrow,
                    glucoseAge = (displayBg.timestamp - currentTime).toRelativeString(),
                    delta = deltas?.first,
                    shortAverageDelta = deltas?.second,
                    longAverageDelta = deltas?.third,
                    runningMode = statusData.short.runningMode,
                    remainingDuration = runningModeRemainingDuration
                )

                Spacer(Modifier.height(16.dp))
            }

            StatusIndicators(
                modifier = Modifier.fillMaxWidth(),
                statusData = statusData,
                currentTime = currentTime
            )

            Spacer(Modifier.height(16.dp))

            OverviewGraphs(
                modifier = Modifier
                    .overviewLayoutData(true, 300.dp)
                    .fillMaxWidth(),
                currentTime = currentTime,
                statusData = statusData
            )

            Spacer(Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .windowInsetsTopHeight(WindowInsets.systemBars)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background.copy(alpha = 0f))))
        )
    }
}