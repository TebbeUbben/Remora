package de.tebbeubben.remora.ui

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
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R
import de.tebbeubben.remora.lib.model.RemoraStatusData
import de.tebbeubben.remora.util.formatCarbs
import de.tebbeubben.remora.util.formatDaysAndHours
import de.tebbeubben.remora.util.formatInsulin
import kotlin.math.roundToInt
import kotlin.time.Instant

@Composable
fun StatusIndicators(
    modifier: Modifier,
    statusData: RemoraStatusData,
    currentTime: Instant
) {
    Column(
        modifier = modifier
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
                tempBasal == null -> BasalRateClassification.NEUTRAL
                tempBasal > baseBasal -> BasalRateClassification.HIGH
                else -> BasalRateClassification.LOW
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
}
