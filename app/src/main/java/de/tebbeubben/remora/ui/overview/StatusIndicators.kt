package de.tebbeubben.remora.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R
import de.tebbeubben.remora.lib.model.status.RemoraStatusData
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.DeviceBattery
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.StatusLightElement
import de.tebbeubben.remora.ui.theme.LocalExtendedColors
import de.tebbeubben.remora.util.formatCarbs
import de.tebbeubben.remora.util.formatDaysAndHours
import de.tebbeubben.remora.util.formatInsulin
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Instant

@Composable
fun StatusIndicators(
    modifier: Modifier,
    currentTime: Instant,
    cob: RemoraStatusData.Cob,
    iob: RemoraStatusData.Iob,
    basalStatus: RemoraStatusData.BasalStatus,
    autosensRatio: Float,
    reservoirLevel: StatusLightElement<Int, Int>?,
    reservoirChangedAt: StatusLightElement<Instant, Duration>?,
    sensorBatteryLevel: StatusLightElement<Int, Int>?,
    sensorChangedAt: StatusLightElement<Instant, Duration>?,
    pumpBatteryLevel: StatusLightElement<Int, Int>?,
    pumpBatteryChangedAt: StatusLightElement<Instant, Duration>?,
    cannulaChangedAt: StatusLightElement<Instant, Duration>?,
    usesPatchPump: Boolean,
    deviceBattery: DeviceBattery,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
    ) {
        var carbsText = cob.display?.formatCarbs()?.plus(" g") ?: "n/a"
        if (cob.futureCarbs.roundToInt() > 0) {
            carbsText += " (${cob.futureCarbs.roundToInt()})"
        }

        val tempBasal = basalStatus.tempBasalAbsolute
        val baseBasal = basalStatus.baseBasal

        TherapyIndicators(
            modifier = Modifier.fillMaxWidth(),
            iob = (iob.bolus + iob.basal).formatInsulin() + " U",
            cob = carbsText,
            basalRate = (tempBasal ?: baseBasal).formatInsulin() + " U/h",
            basalRateClassification = when {
                tempBasal == null -> BasalRateClassification.NEUTRAL
                tempBasal > baseBasal -> BasalRateClassification.HIGH
                else -> BasalRateClassification.LOW
            },
            autosensRatio = (autosensRatio * 100).roundToInt().toString() + "%"
        )

        Spacer(modifier = Modifier.height(1.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large.copy(topStart = CornerSize(0), topEnd = CornerSize(0))),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            val deviceBatteryColor = when {
                deviceBattery.isCharging -> Color.Unspecified
                deviceBattery.level > 20 -> Color.Unspecified
                deviceBattery.level > 5 -> LocalExtendedColors.current.yellow.color
                else -> LocalExtendedColors.current.red.color
            }
            StatusLight(
                icon = painterResource(if (deviceBattery.isCharging) R.drawable.mobile_charge_24px else R.drawable.mobile_24px),
                description = stringResource(R.string.phone_battery),
                texts = listOf((deviceBattery.level.toString() + "%") to deviceBatteryColor)
            )

            val reservoirLevelIndicator = reservoirLevel?.let {
                var text = it.value.toString()
                if (it.isMax == true) text += "+"
                text += "U"
                text to it.getColor()
            }

            val reservoirAgeIndicator = reservoirChangedAt?.let { (currentTime - it.value).formatDaysAndHours() to it.getColor(currentTime) }

            val cannulaAgeIndicator = cannulaChangedAt?.let { (currentTime - it.value).formatDaysAndHours() to it.getColor(currentTime) }

            if (usesPatchPump) {
                val podIndicators = listOfNotNull(cannulaAgeIndicator, reservoirLevelIndicator)
                if (podIndicators.isNotEmpty()) {
                    StatusLight(
                        icon = painterResource(R.drawable.pod),
                        description = stringResource(R.string.pod),
                        texts = podIndicators
                    )
                }
            } else {
                if (cannulaAgeIndicator != null) {
                    StatusLight(
                        icon = painterResource(R.drawable.cannula),
                        description = stringResource(R.string.cannula),
                        texts = listOf(cannulaAgeIndicator)
                    )
                }
                val reservoirIndicators = listOfNotNull(reservoirAgeIndicator, reservoirLevelIndicator)
                if (reservoirIndicators.isNotEmpty()) {
                    StatusLight(
                        icon = painterResource(R.drawable.reservoir),
                        description = stringResource(R.string.reservoir),
                        texts = reservoirIndicators
                    )
                }
            }

            val pumpBatteryLevelIndicator = pumpBatteryLevel?.let { "${it.value}%" to it.getColor() }
            val pumpBatteryAgeIndicator = pumpBatteryChangedAt?.let { (currentTime - it.value).formatDaysAndHours() to it.getColor(currentTime) }

            val pumpBatteryIndicators = listOfNotNull(pumpBatteryAgeIndicator, pumpBatteryLevelIndicator)

            if (pumpBatteryIndicators.isNotEmpty()) {
                StatusLight(
                    icon = painterResource(R.drawable.battery_android_full_24px),
                    description = stringResource(R.string.pump_battery),
                    texts = pumpBatteryIndicators
                )
            }

            val sensorBatteryLevelIndicator = sensorBatteryLevel?.let { "${it.value}%" to it.getColor() }
            val sensorBatteryAgeIndicator = sensorChangedAt?.let { (currentTime - it.value).formatDaysAndHours() to it.getColor(currentTime) }

            val sensorBatteryIndicators = listOfNotNull(sensorBatteryAgeIndicator, sensorBatteryLevelIndicator)

            if (sensorBatteryIndicators.isNotEmpty()) {
                StatusLight(
                    icon = painterResource(R.drawable.sensors_24px),
                    description = stringResource(R.string.sensor),
                    texts = sensorBatteryIndicators
                )
            }
        }
    }
}

@Composable
private fun StatusLightElement<Int, Int>.getColor(): Color = when {
    value <= criticalThreshold -> LocalExtendedColors.current.red.color
    value <= warnThreshold -> LocalExtendedColors.current.yellow.color
    else -> Color.Unspecified
}

@Composable
private fun StatusLightElement<Instant, Duration>.getColor(currentTime: Instant): Color {
    val age = currentTime - value
    return when {
        age >= criticalThreshold -> LocalExtendedColors.current.red.color
        age >= warnThreshold -> LocalExtendedColors.current.yellow.color
        else -> Color.Unspecified
    }
}