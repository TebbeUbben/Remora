package de.tebbeubben.remora.ui.graphs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.model.RemoraStatusData
import de.tebbeubben.remora.ui.theme.LocalExtendedColors
import de.tebbeubben.remora.ui.time_axis.rememberTimeAxisState
import de.tebbeubben.remora.ui.time_axis.timeAxis
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Composable
fun ColumnScope.OverviewGraphs(currentTime: Instant, statusData: RemoraStatusData) {
    val timeAxisState = rememberTimeAxisState(
        initialStart = currentTime - 24.hours,
        initialEnd = currentTime + 3.hours,
        initialWindowStart = currentTime - 3.hours,
        initialWindowWidth = 6.hours,
        initialMinWindowWidth = 1.hours,
        initialMaxWindowWidth = Duration.INFINITE
    )
    var previousTime by remember { mutableStateOf(currentTime) }
    LaunchedEffect(currentTime) {
        timeAxisState.end = currentTime + 3.hours
        timeAxisState.start = currentTime - 24.hours
        val diff = currentTime - previousTime
        previousTime = currentTime
        timeAxisState.mutate {
            windowStart += diff
        }
    }
    val overscrollEffect = rememberOverscrollEffect()

    Surface(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .timeAxis(timeAxisState, overscrollEffect),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large
    ) {

        val timezone = TimeZone.currentSystemDefault()
        val fullHours = remember(timeAxisState.windowStart, timeAxisState.windowWidth) {
            val startDateTime = (timeAxisState.windowStart - timeAxisState.windowWidth).toLocalDateTime(timezone)
            val withoutFractionalHours = LocalDateTime(
                year = startDateTime.year,
                month = startDateTime.month,
                day = startDateTime.day,
                hour = startDateTime.hour,
                minute = 0,
                second = 0,
                nanosecond = 0
            )
            val startInstant = withoutFractionalHours.toInstant(timezone)
            // One extra hour on both sides, so that labels don't disappear prematurely on the edges
            generateSequence(startInstant - 1.hours) { it + 1.hours }
                .takeWhile { it <= timeAxisState.windowEnd + 1.hours }
                .map { it to it.toLocalDateTime(timezone).hour.toString() }
                .toList()
        }

        val bgData = statusData.bucketedData.mapNotNull { it.bgData?.let { bgData -> it.timestamp to bgData } }
        val bgMaxValue = maxOf(
            statusData.short.lowBgThreshold,
            statusData.short.highBgThreshold,
            bgData.maxOf { it.second.value },
            statusData.predictions.maxOf { it.value }
        )
        val cobColor = LocalExtendedColors.current.carbs.color
        val iobColor = LocalExtendedColors.current.bolus.color

        val iobValues = statusData.bucketedData.map { it.timestamp to (it.insulinData?.iob ?: 0f) }
        val cobValues = statusData.bucketedData.map { Triple(it.timestamp, it.autosensData?.cob ?: 0f, it.autosensData?.carbsFromBolus ?: 0f) }

        val maxIobValue = iobValues.maxOfOrNull { it.second } ?: 1.0f
        val minIobValue = iobValues.minOfOrNull { it.second } ?: -1.0f
        val maxIobRange = maxOf(2, ceil(maxOf(abs(maxIobValue), abs(minIobValue))).roundToInt())

        val maxCobValue = cobValues.maxOfOrNull { it.second } ?: 10.0f
        val maxCobRange = maxOf(20, ceil(maxCobValue / 10f).roundToInt() * 10)

        val bolusColor = LocalExtendedColors.current.bolus.color
        val carbsColor = LocalExtendedColors.current.carbs.color

        val deviations = statusData.bucketedData.map { Triple(it.timestamp, it.autosensData?.deviation ?: 0f, it.autosensData?.type ?: RemoraStatusData.AutosensType.NEUTRAL) }
        val maxDevValue = deviations.maxOfOrNull { it.second } ?: 10.0f
        val minDevValue = deviations.minOfOrNull { it.second } ?: -10.0f
        val maxDevRange = max(20, ceil(maxOf(abs(maxDevValue), abs(minDevValue)) / 10.0f).roundToInt() * 10)

        // All elements inside this Box are subject to the overscroll effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .overscroll(overscrollEffect)
        ) {

            TimeGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 32.dp),
                state = timeAxisState,
                fullHours = fullHours.map { it.first }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BgCanvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f),
                    maxValue = bgMaxValue,
                    highBgThreshold = statusData.short.highBgThreshold,
                    lowBgThreshold = statusData.short.lowBgThreshold,
                    state = timeAxisState,
                    bgData = bgData,
                    bgColor = MaterialTheme.colorScheme.onSurface,
                    predictions = statusData.predictions,
                    iobColor = iobColor,
                    cobColor = cobColor,
                    aCobColor = Color(red = cobColor.red - 0.1f, green = cobColor.green - 0.1f, blue = cobColor.blue - 0.1f),
                    uamColor = Color(red = cobColor.red + 0.1f, green = cobColor.green + 0.1f, blue = cobColor.blue + 0.1f),
                    ztColor = Color(red = iobColor.red + 0.1f, green = iobColor.green + 0.1f, blue = iobColor.blue + 0.1f)
                )

                IobCobCanvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = timeAxisState,
                    iobValues = iobValues,
                    cobValues = cobValues,
                    maxIobRange = maxIobRange,
                    maxCobRange = maxCobRange,
                    baselineColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iobStrokeColor = bolusColor,
                    iobFillColor = bolusColor.copy(alpha = 0.3f),
                    cobStrokeColor = carbsColor,
                    cobFillColor = carbsColor.copy(alpha = 0.3f)
                )

                DeviationCanvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = timeAxisState,
                    baselineColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    deviations = deviations,
                    maxDevRange = maxDevRange,
                    posColor = Color(0x8000FF00),
                    negColor = Color(0x80FF0000),
                    uamColor = LocalExtendedColors.current.carbs.color,
                    neutralColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val lineColor = MaterialTheme.colorScheme.onSurfaceVariant

            Canvas(Modifier.fillMaxSize()) {
                val durationPerPx = timeAxisState.windowWidth / size.width.toDouble()
                val posX = ((currentTime - timeAxisState.windowStart) / durationPerPx).toFloat()
                drawLine(
                    color = lineColor,
                    strokeWidth = 1.dp.toPx(),
                    start = Offset(posX + 1.dp.toPx() / 2, 16.dp.toPx()),
                    end = Offset(posX + 1.dp.toPx() / 2, size.height - 32.dp.toPx()),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
                )
            }

            TimeLabels(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                state = timeAxisState,
                fullHours = fullHours
            )
        }

        // Labels are static and don't scroll -> no overscroll effect
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier
                    .fillMaxWidth()
                    .weight(2f)) {
                BgLabels(
                    usesMgdl = statusData.short.usesMgdl,
                    maxValue = bgMaxValue
                )
            }

            Box(Modifier
                    .fillMaxWidth()
                    .weight(1f)) {
                IobCobLabels(
                    maxIobRange = maxIobRange,
                    bolusColor = bolusColor,
                    maxCobRange = maxCobRange,
                    carbsColor = carbsColor
                )
            }

            Box(Modifier
                    .fillMaxWidth()
                    .weight(1f)) {
                DeviationLabels(
                    maxDevRange = maxDevRange
                )
            }
        }
    }
}