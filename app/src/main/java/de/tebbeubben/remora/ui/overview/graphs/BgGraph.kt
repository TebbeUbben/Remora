package de.tebbeubben.remora.ui.overview.graphs

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.model.status.RemoraStatusData
import de.tebbeubben.remora.ui.overview.time_axis.TimeAxisState
import de.tebbeubben.remora.util.toMmoll
import kotlin.math.roundToInt
import kotlin.time.Instant

@Composable
fun BgCanvas(
    modifier: Modifier = Modifier,
    maxValue: Float,
    highBgThreshold: Float,
    lowBgThreshold: Float,
    state: TimeAxisState,
    bgData: List<Pair<Instant, RemoraStatusData.BgData>>,
    bgColor: Color,
    predictions: List<RemoraStatusData.Prediction>,
    iobColor: Color,
    cobColor: Color,
    aCobColor: Color,
    uamColor: Color,
    ztColor: Color,
) {
    Canvas(modifier) {
        drawRect(
            color = Color(0x4000FF00),
            topLeft = Offset(0f, size.height - size.height / maxValue * highBgThreshold),
            size = Size(size.width, size.height / maxValue * (highBgThreshold - lowBgThreshold))
        )

        val durationPerPx = state.windowWidth / size.width.toDouble()
        val renderBgData = bgData.filter { state.windowStart <= it.first && it.first <= state.windowEnd }
        for ((timestamp, bgData) in renderBgData) {
            val posX = (timestamp - state.windowStart) / durationPerPx
            val posY = size.height - size.height / maxValue * bgData.value

            drawCircle(
                color = bgColor,
                radius = 2.dp.toPx(),
                center = Offset(posX.toFloat(), posY),
                alpha = if (bgData.filledGap) 0.5f else 1f
            )
        }

        val renderPredictions = predictions.filter { state.windowStart <= it.timestamp && it.timestamp <= state.windowEnd }

        for ((timestamp, type, value) in renderPredictions) {
            val posX = (timestamp - state.windowStart) / durationPerPx
            val posY = size.height - size.height / maxValue * value
            val color = when (type) {
                RemoraStatusData.PredictionType.IOB   -> iobColor
                RemoraStatusData.PredictionType.COB   -> cobColor
                RemoraStatusData.PredictionType.A_COB -> aCobColor
                RemoraStatusData.PredictionType.UAM   -> uamColor
                RemoraStatusData.PredictionType.ZT    -> ztColor
            }

            drawCircle(
                color = color,
                radius = 2.dp.toPx(),
                center = Offset(posX.toFloat(), posY)
            )
        }
    }
}

@Composable
fun BgLabels(
    modifier: Modifier = Modifier,
    usesMgdl: Boolean,
    maxValue: Float,
) {
    // We define a list of step sizes and use the one most granular one that still fits on screen.
    val levels = if (usesMgdl) {
        listOf(
            generateSequence(0) { it + 10 }.takeWhile { it <= maxValue }.toList(),
            generateSequence(0) { it + 20 }.takeWhile { it <= maxValue }.toList(),
            generateSequence(0) { it + 50 }.takeWhile { it <= maxValue }.toList(),
            generateSequence(0) { it + 100 }.takeWhile { it <= maxValue }.toList(),
        )
    } else {
        listOf(
            generateSequence(0) { it + 1 }.takeWhile { it <= maxValue.toMmoll() }.toList(),
            generateSequence(0) { it + 2 }.takeWhile { it <= maxValue.toMmoll() }.toList(),
            generateSequence(0) { it + 5 }.takeWhile { it <= maxValue.toMmoll() }.toList(),
            generateSequence(0) { it + 10 }.takeWhile { it <= maxValue.toMmoll() }.toList(),
        )
    }

    SubcomposeLayout(modifier) { constraints ->
        val maxValueInUnits = if (usesMgdl) maxValue else maxValue.toMmoll()
        levels.forEachIndexed { index, level ->
            val placeables = level.map { label ->
                subcompose(index * 100000 + label) {
                    Text(
                        text = label.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }.first().measure(Constraints())
            }

            val height = placeables.sumOf { it.height } + 16.dp.roundToPx() * (placeables.size - 1)

            if (height > constraints.maxHeight) return@forEachIndexed

            return@SubcomposeLayout layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val label = level[index]
                    Log.d("BgGraphLabels", "Label: $label")
                    val yOffset = constraints.maxHeight - constraints.maxHeight / maxValueInUnits * label
                    val yOffsetCentered = yOffset + placeable.height / 2
                    if (0 <= yOffsetCentered && yOffsetCentered + placeable.height <= constraints.maxHeight) {
                        placeable.placeRelative(IntOffset(16.dp.roundToPx(), yOffsetCentered.roundToInt()))
                    }
                }
            }
        }
        layout(constraints.maxWidth, constraints.maxHeight) {}
    }
}