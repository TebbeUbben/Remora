package de.tebbeubben.remora.ui.overview.graphs

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.model.status.RemoraStatusData
import de.tebbeubben.remora.ui.overview.time_axis.TimeAxisState
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Composable
fun DeviationLabels(
    modifier: Modifier = Modifier,
    maxDevRange: Int
) {
    SubcomposeLayout(modifier) { constraints ->
        val devHigh = subcompose(maxDevRange - 10) {
            Text(
                text = maxDevRange.toString(),
                style = MaterialTheme.typography.labelSmall
            )
        }.first().measure(Constraints())
        val devNeutral = subcompose(0) {
            Text(
                text = "0",
                style = MaterialTheme.typography.labelSmall
            )
        }.first().measure(Constraints())
        val devLow = subcompose(-maxDevRange + 10) {
            Text(
                text = (-maxDevRange).toString(),
                style = MaterialTheme.typography.labelSmall
            )
        }.first().measure(Constraints())

        layout(constraints.maxWidth, constraints.maxHeight) {
            devHigh.place(
                x = 16.dp.roundToPx(),
                y = (constraints.maxHeight * (0.5f - 0.5f / maxDevRange * (maxDevRange - 10))).roundToInt() - devHigh.height / 2
            )
            devNeutral.place(
                x = 16.dp.roundToPx(),
                y = constraints.maxHeight / 2 - devHigh.height / 2
            )
            devLow.place(
                x = 16.dp.roundToPx(),
                y = (constraints.maxHeight * (0.5f - 0.5f / maxDevRange * (-maxDevRange + 10))).roundToInt() - devHigh.height / 2
            )
        }
    }
}

@Composable
fun DeviationCanvas(
    modifier: Modifier = Modifier,
    state: TimeAxisState,
    baselineColor: Color,
    deviations: List<Triple<Instant, Float, RemoraStatusData.AutosensType>>,
    maxDevRange: Int,
    posColor: Color,
    negColor: Color,
    uamColor: Color,
    neutralColor: Color,
) {
    Canvas(modifier) {
        val devWidth = (5.minutes * size.width.toDouble() / state.windowWidth - 1.dp.toPx()).toFloat().coerceIn(1.dp.toPx(), 8.dp.toPx())
        val durationPerPx = state.windowWidth / size.width.toDouble()

        val canvasWidth = size.width
        val canvasHeight = size.height
        val zeroY = canvasHeight / 2f

        drawLine(
            color = baselineColor,
            strokeWidth = 1.dp.toPx(),
            start = Offset(0f, zeroY - 1.dp.toPx() / 2),
            end = Offset(canvasWidth, zeroY - 1.dp.toPx() / 2)
        )

        for (deviation in deviations) {
            if (deviation.second == 0f) continue
            val rectLeft = ((deviation.first - state.windowStart) / durationPerPx - devWidth).toFloat()
            val barHeight = canvasHeight * 0.5f / maxDevRange * abs(deviation.second)
            val rectTopActual = if (deviation.second < 0) zeroY else zeroY - barHeight
            val rectRight = rectLeft + devWidth
            val rectBottomActual = if (deviation.second < 0) zeroY + barHeight else zeroY

            clipRect(
                left = rectLeft,
                top = rectTopActual,
                right = rectRight,
                bottom = rectBottomActual
            ) {
                val radius = devWidth / 2
                val pathRectTop = if (deviation.second < 0) zeroY else zeroY - barHeight
                val pathRectBottom = if (deviation.second < 0) zeroY + barHeight else zeroY

                val barPath = Path().apply {
                    if (deviation.second > 0) {
                        moveTo(rectLeft + radius, pathRectTop)
                        lineTo(rectRight - radius, pathRectTop)
                        arcTo(
                            rect = Rect(rectRight - 2 * radius, pathRectTop, rectRight, pathRectTop + 2 * radius),
                            startAngleDegrees = -90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(rectRight, pathRectBottom)
                        lineTo(rectLeft, pathRectBottom)
                        arcTo(
                            rect = Rect(rectLeft, pathRectTop, rectLeft + 2 * radius, pathRectTop + 2 * radius),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                    } else if (deviation.second < 0) {
                        moveTo(rectLeft, pathRectTop)
                        lineTo(rectRight, pathRectTop)
                        lineTo(rectRight, pathRectBottom - radius)
                        arcTo(
                            rect = Rect(rectRight - 2 * radius, pathRectBottom - 2 * radius, rectRight, pathRectBottom),
                            startAngleDegrees = 0f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(rectLeft + radius, pathRectBottom)
                        arcTo(
                            rect = Rect(rectLeft, pathRectBottom - 2 * radius, rectLeft + 2 * radius, pathRectBottom),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                    }
                    close()
                }

                drawPath(
                    path = barPath,
                    color = when (deviation.third) {
                        RemoraStatusData.AutosensType.POSITIVE -> posColor
                        RemoraStatusData.AutosensType.NEGATIVE -> negColor
                        RemoraStatusData.AutosensType.UAM      -> uamColor
                        else                                   -> neutralColor
                    }
                )
            }
        }
    }
}