package de.tebbeubben.remora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.model.RemoraStatusData
import de.tebbeubben.remora.ui.BasalRateClassification
import de.tebbeubben.remora.ui.BgClassification
import de.tebbeubben.remora.ui.GlucoseStatus
import de.tebbeubben.remora.ui.RibbonItem
import de.tebbeubben.remora.ui.StatusLight
import de.tebbeubben.remora.ui.TherapyIndicators
import de.tebbeubben.remora.ui.graphs.OverviewGraphs
import de.tebbeubben.remora.ui.theme.LocalExtendedColors
import de.tebbeubben.remora.util.formatBG
import de.tebbeubben.remora.util.formatCarbs
import de.tebbeubben.remora.util.formatDaysAndHours
import de.tebbeubben.remora.util.formatInsulin
import de.tebbeubben.remora.util.toMinimalLocalizedString
import de.tebbeubben.remora.util.toRelativeString
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Composable
fun Overview(
    currentTime: Instant,
    statusData: RemoraStatusData,
) {
    rememberScrollState()
    Scaffold(
        bottomBar = {
            FlexibleBottomAppBar(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {

                IconButton(
                    onClick = {},
                    colors = IconButtonDefaults.iconButtonColors().copy(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.settings_24px),
                        contentDescription = "Settings"
                    )
                }

                FilledIconButton(
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
                }

                FilledIconButton(
                    onClick = {},
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

                FilledIconButton(
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
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            val updated = (statusData.short.timestamp - currentTime).toRelativeString()
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Last updated: $updated",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val profilePercentage = if (statusData.short.activeProfilePercentage == 100) null else "${statusData.short.activeProfilePercentage}%"
                val profileShift = when {
                    statusData.short.activeProfileShift == 0 -> null
                    statusData.short.activeProfileShift > 0  -> "+${statusData.short.activeProfileShift}h"
                    else                                     -> "${statusData.short.activeProfileShift}h"
                }
                val profileDetails = when {
                    profilePercentage != null && profileShift != null -> "$profilePercentage $profileShift"
                    profilePercentage != null                         -> profilePercentage
                    profileShift != null                              -> profileShift
                    else                                              -> null
                }

                val profileRemainingDuration = statusData.short.activeProfileDuration?.let { duration ->
                    val end = statusData.short.activeProfileStart + duration
                    val remainingDuration = end - currentTime
                    remainingDuration.toMinimalLocalizedString()
                }

                RibbonItem(
                    modifier = Modifier.weight(1f),
                    icon = painterResource(R.drawable.kid_star_24px),
                    description = "Active Profile",
                    text = statusData.short.activeProfile,
                    activeText = when {
                        profileDetails != null && profileRemainingDuration != null -> "$profileDetails, $profileRemainingDuration"
                        profileRemainingDuration != null                           -> profileRemainingDuration
                        profileDetails != null                                     -> profileDetails
                        else                                                       -> null
                    }
                )

                val targetStart = statusData.short.tempTargetStart
                val targetDuration = statusData.short.tempTargetDuration

                val targetRemainingDuration = if (targetStart != null && targetDuration != null) {
                    val end = targetStart + targetDuration
                    val remainingDuration = end - currentTime
                    remainingDuration.toMinimalLocalizedString()
                } else null

                RibbonItem(
                    modifier = Modifier.weight(1f),
                    icon = painterResource(R.drawable.recenter_24px),
                    description = "Current Target",
                    text = statusData.short.target.formatBG(statusData.short.usesMgdl),
                    activeText = targetRemainingDuration
                )
            }

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
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
            ) {
                var carbsText = statusData.short.displayCob?.formatCarbs()?.plus(" g") ?: "n/a"
                if (statusData.short.futureCarbs.roundToInt() > 0) {
                    carbsText += " (${statusData.short.futureCarbs.roundToInt()})"
                }

                val tempBasal = statusData.short.tempBasalAbsolute
                val baseBasal = statusData.short.baseBasal

                TherapyIndicators(
                    modifier = Modifier.fillMaxWidth(),
                    iob = (statusData.short.bolusIob + statusData.short.basalIob).formatInsulin() + " U",
                    cob = carbsText,
                    basalRate = (tempBasal ?: baseBasal).formatInsulin() + " U/h",
                    basalRateClassification = when {
                        tempBasal == null     -> BasalRateClassification.NEUTRAL
                        tempBasal > baseBasal -> BasalRateClassification.HIGH
                        else                  -> BasalRateClassification.LOW
                    },
                    autosensRatio = (statusData.short.autosensRatio * 100).roundToInt().toString() + "%"
                )

                Spacer(modifier = Modifier.height(1.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large.copy(topStart = CornerSize(0), topEnd = CornerSize(0))),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    StatusLight(
                        icon = painterResource(if (statusData.short.isCharging) R.drawable.mobile_charge_24px else R.drawable.mobile_24px),
                        description = "Phone Battery",
                        texts = listOf(statusData.short.deviceBattery.toString() + "%").map { it to Color.Unspecified }
                    )

                    statusData.short.cannulaChangedAt?.let { cannulaChangedAt ->
                        StatusLight(
                            icon = painterResource(R.drawable.cannula),
                            description = "Cannula",
                            texts = listOf((currentTime - cannulaChangedAt).formatDaysAndHours()).map { it to Color.Unspecified }
                        )
                    }

                    val reservoirText = statusData.short.reservoirLevel?.let { reservoirLevel ->
                        var text = reservoirLevel.roundToInt().toString()
                        if (statusData.short.isReservoirLevelMax) text += "+"
                        text += "U"
                        text
                    }

                    val podChangedAt = statusData.short.podChangedAt
                    if (podChangedAt != null) {
                        val podValues = mutableListOf<String>()
                        if (reservoirText != null) podValues += reservoirText
                        podValues +=
                            (currentTime - podChangedAt).formatDaysAndHours()

                        if (podValues.isNotEmpty()) {
                            StatusLight(
                                icon = painterResource(R.drawable.pod),
                                description = "Reservoir",
                                texts = podValues.map { it to Color.Unspecified }
                            )
                        }
                    } else {

                        val reservoirValues = mutableListOf<String>()
                        if (reservoirText != null) reservoirValues += reservoirText

                        statusData.short.insulinChangedAt?.let { insulinChangedAt ->
                            reservoirValues += (currentTime - insulinChangedAt).formatDaysAndHours()
                        }

                        if (reservoirValues.isNotEmpty()) {
                            StatusLight(
                                icon = painterResource(R.drawable.reservoir),
                                description = "Reservoir",
                                texts = reservoirValues.map { it to Color.Unspecified }
                            )
                        }
                    }

                    val pumpBatteryValues = mutableListOf<String>()

                    statusData.short.batteryLevel?.let { batteryLevel ->
                        pumpBatteryValues += "$batteryLevel%"
                    }

                    statusData.short.batteryChangedAt?.let { batteryChangedAt ->
                        pumpBatteryValues += (currentTime - batteryChangedAt).formatDaysAndHours()
                    }

                    if (pumpBatteryValues.isNotEmpty()) {
                        StatusLight(
                            icon = painterResource(R.drawable.battery_android_full_24px),
                            description = "Pump Battery",
                            texts = pumpBatteryValues.map { it to Color.Unspecified }
                        )
                    }

                    statusData.short.sensorChangedAt?.let { sensorChangedAt ->
                        StatusLight(
                            icon = painterResource(R.drawable.sensors_24px),
                            description = "Sensor",
                            texts = listOf((currentTime - sensorChangedAt).formatDaysAndHours()).map { it to Color.Unspecified }
                        )
                    }
                }
            }

            OverviewGraphs(currentTime, statusData)
        }
    }
}
