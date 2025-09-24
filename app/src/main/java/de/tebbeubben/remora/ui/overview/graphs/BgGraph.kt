package de.tebbeubben.remora.ui.overview.graphs

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.model.status.RemoraStatusData
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BasalDataPoint
import de.tebbeubben.remora.ui.overview.time_axis.TimeAxisState
import de.tebbeubben.remora.ui.theme.LocalExtendedColors
import de.tebbeubben.remora.util.formatInsulin
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
    basalData: List<BasalDataPoint>,
    basalLineColor: Color,
    basalFillColor: Color,
    smbs: List<Pair<Instant, Float>>,
    smbColor: Color,
    boluses: List<Triple<Instant, Float, Float>>,
    bolusColor: Color,
    bolusTextStyle: TextStyle,
    carbs: List<Triple<Instant, Float, Float>>,
    carbsColor: Color,
    carbsTextStyle: TextStyle,
    targetData: List<RemoraStatusData.TargetDataPoint>,
    targetColor: Color,
) {
    val basalMaxValue = basalData.maxOf { maxOf(it.baselineBasal, it.tempBasalAbsolute ?: 0f) }.coerceAtLeast(0.1f)

    LocalExtendedColors.current.basal

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier) {
        val durationPerPx = state.windowWidth / size.width.toDouble()

        val baselinePath = Path()
        var previousBaselineY: Float? = null

        val basalPath = Path()
        var previousBasalY: Float? = null

        val basalFillPath = Path()
        var previousBasalFillY: Float? = null

        for ((timestamp, baseline, temp) in basalData) {
            val posX = ((timestamp - state.windowStart) / durationPerPx).toFloat()
            val baselineY = size.height - size.height / maxValue * lowBgThreshold * 0.85f / basalMaxValue * baseline

            if (previousBaselineY == null) {
                baselinePath.moveTo(posX, baselineY)
                previousBaselineY = baselineY
            } else {
                baselinePath.lineTo(posX, previousBaselineY)
                baselinePath.lineTo(posX, baselineY)
                previousBaselineY = baselineY
            }

            val basalY = size.height - size.height / maxValue * lowBgThreshold * 0.85f / basalMaxValue * (temp ?: baseline)
            if (previousBasalY == null) {
                basalPath.moveTo(posX, basalY)
                previousBasalY = basalY
            } else {
                basalPath.lineTo(posX, previousBasalY)
                basalPath.lineTo(posX, basalY)
                previousBasalY = basalY
            }

            if (previousBasalFillY == null) {
                basalFillPath.moveTo(posX, size.height)
                basalFillPath.lineTo(posX, basalY)
                previousBasalFillY = basalY
            } else {
                basalFillPath.lineTo(posX, previousBasalFillY)
                basalFillPath.lineTo(posX, basalY)
                previousBasalFillY = basalY
            }
        }

        if (previousBaselineY != null) {
            baselinePath.lineTo(size.width, previousBaselineY)
        }

        if (previousBasalY != null) {
            basalPath.lineTo(size.width, previousBasalY)
        }

        if (previousBasalFillY != null) {
            basalFillPath.lineTo(size.width, previousBasalFillY)
            basalFillPath.lineTo(size.width, size.height)
        }

        drawPath(
            path = basalFillPath,
            color = basalFillColor
        )

        drawPath(
            path = baselinePath,
            color = basalLineColor,
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx()), 0f))
        )

        drawPath(
            path = basalPath,
            color = basalLineColor,
            style = Stroke(width = 1.dp.toPx())
        )

        drawRect(
            color = Color(0x4000FF00),
            topLeft = Offset(0f, size.height - size.height / maxValue * highBgThreshold),
            size = Size(size.width, size.height / maxValue * (highBgThreshold - lowBgThreshold))
        )

        val targetPath = Path()
        var previousTargetY: Float? = null
        for ((timestamp, target) in targetData) {
            val posX = ((timestamp - state.windowStart) / durationPerPx).toFloat()
            val targetY = size.height - size.height / maxValue * target
            if (previousTargetY == null) {
                targetPath.moveTo(posX, targetY)
                previousTargetY = targetY
            } else {
                targetPath.lineTo(posX, previousTargetY)
                targetPath.lineTo(posX, targetY)
                previousTargetY = targetY
            }
        }

        if (previousTargetY != null) {
            targetPath.lineTo(size.width, previousTargetY)
        }

        drawPath(
            path = targetPath,
            color = targetColor,
            style = Stroke(width = 1.dp.toPx())
        )

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

        val smbPosX = size.height - size.height / maxValue * lowBgThreshold
        for ((timestamp, value) in smbs) {
            val posX = (timestamp - state.windowStart) / durationPerPx
            drawCircle(
                color = smbColor,
                radius = (8.dp * value).toPx(),
                center = Offset(posX.toFloat(), smbPosX)
            )
        }

        for ((timestamp, value, bg) in boluses) {
            val posX = (timestamp - state.windowStart) / durationPerPx
            val posY = size.height - size.height / maxValue * bg
            drawCircle(
                color = bolusColor,
                radius = 2.dp.toPx(),
                center = Offset(posX.toFloat(), posY)
            )
            val text = value.formatInsulin() + " U"

            val textLayoutResult = textMeasurer.measure(text, bolusTextStyle)
            rotate(-45f, pivot = Offset(posX.toFloat(), posY)) {
                drawText(textLayoutResult, color = bolusColor, topLeft = Offset(posX.toFloat() + 8.dp.toPx(), posY - textLayoutResult.size.height / 2))
            }
        }

        for ((timestamp, value, bg) in carbs) {
            val posX = (timestamp - state.windowStart) / durationPerPx
            val posY = size.height - size.height / maxValue * bg
            drawCircle(
                color = carbsColor,
                radius = 2.dp.toPx(),
                center = Offset(posX.toFloat(), posY)
            )
            val text = value.roundToInt().toString() + " g"

            val textLayoutResult = textMeasurer.measure(text, carbsTextStyle)
            rotate(-45f, pivot = Offset(posX.toFloat(), posY)) {
                drawText(textLayoutResult, color = carbsColor, topLeft = Offset(posX.toFloat() + 8.dp.toPx(), posY - textLayoutResult.size.height / 2))
            }
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
    val levels by remember(usesMgdl, maxValue) {
        derivedStateOf {
            if (usesMgdl) {
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
        }
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
                    val yOffset = constraints.maxHeight - constraints.maxHeight / maxValueInUnits * label
                    val yOffsetCentered = yOffset - placeable.height / 2
                    if (0 <= yOffsetCentered && yOffsetCentered + placeable.height <= constraints.maxHeight) {
                        placeable.placeRelative(IntOffset(16.dp.roundToPx(), yOffsetCentered.roundToInt()))
                    }
                }
            }
        }
        layout(constraints.maxWidth, constraints.maxHeight) {}
    }
}