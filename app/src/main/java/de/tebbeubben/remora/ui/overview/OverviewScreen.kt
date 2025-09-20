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
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R
import de.tebbeubben.remora.lib.model.status.RemoraStatusData
import de.tebbeubben.remora.ui.overview.graphs.OverviewGraphs
import de.tebbeubben.remora.ui.theme.LocalExtendedColors
import de.tebbeubben.remora.util.formatBG
import de.tebbeubben.remora.util.toMinimalLocalizedString
import de.tebbeubben.remora.util.toRelativeString
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Composable
fun Overview(
    currentTime: Instant,
    statusData: RemoraStatusData,
    onPressBolusButton: () -> Unit
) {
    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
        bottomBar = {
            FlexibleBottomAppBar(
                //horizontalArrangement = Arrangement.SpaceBetween,
                horizontalArrangement = Arrangement.Center,
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

                FilledIconButton(
                    onClick = onPressBolusButton,
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
                text = "Last updated: $updated",
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
                    remainingDuration.toMinimalLocalizedString()
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