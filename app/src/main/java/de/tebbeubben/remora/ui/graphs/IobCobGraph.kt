package de.tebbeubben.remora.ui.graphs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.ui.time_axis.TimeAxisState
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Instant

@Composable
fun IobCobLabels(
    modifier: Modifier = Modifier,
    maxIobRange: Int,
    bolusColor: Color,
    maxCobRange: Int,
    carbsColor: Color,
) {
    SubcomposeLayout(modifier) { constraints ->
        val iobHigh = subcompose(maxIobRange - 1) {
            Text(
                text = maxIobRange.toString(),
                color = bolusColor,
                style = MaterialTheme.typography.labelSmall
            )
        }.first().measure(Constraints())
        val iobNeutral = subcompose(0) {
            Text(
                text = "0",
                color = bolusColor,
                style = MaterialTheme.typography.labelSmall
            )
        }.first().measure(Constraints())
        val iobLow = subcompose(-maxIobRange + 1) {
            Text(
                text = (-maxIobRange).toString(),
                color = bolusColor,
                style = MaterialTheme.typography.labelSmall
            )
        }.first().measure(Constraints())

        layout(constraints.maxWidth, constraints.maxHeight) {
            iobHigh.place(
                x = 16.dp.roundToPx(),
                y = (constraints.maxHeight * (0.5f - 0.5f / maxIobRange * (maxIobRange - 1))).roundToInt() - iobHigh.height / 2
            )
            iobNeutral.place(
                x = 16.dp.roundToPx(),
                y = constraints.maxHeight / 2 - iobHigh.height / 2
            )
            iobLow.place(
                x = 16.dp.roundToPx(),
                y = (constraints.maxHeight * (0.5f - 0.5f / maxIobRange * (-maxIobRange + 1))).roundToInt() - iobHigh.height / 2
            )
        }
    }

    SubcomposeLayout(
        modifier = Modifier
            .fillMaxSize()
    ) { constraints ->
        val cobHigh = subcompose(maxCobRange - 10) {
            Text(
                text = (maxCobRange - 10).toString(),
                color = carbsColor,
                style = MaterialTheme.typography.labelSmall
            )
        }.first().measure(Constraints())
        val cobNeutral = subcompose(0) {
            Text(
                text = "0",
                color = carbsColor,
                style = MaterialTheme.typography.labelSmall
            )
        }.first().measure(Constraints())

        layout(constraints.maxWidth, constraints.maxHeight) {
            cobHigh.place(
                x = constraints.maxWidth - 16.dp.roundToPx() - cobHigh.width,
                y = (constraints.maxHeight * (0.5f - 0.5f / maxCobRange * (maxCobRange - 10))).roundToInt() - cobHigh.height / 2
            )
            cobNeutral.place(
                x = constraints.maxWidth - 16.dp.roundToPx() - cobNeutral.width,
                y = constraints.maxHeight / 2 - cobHigh.height / 2
            )
        }
    }
}

@Composable
fun IobCobCanvas(
    modifier: Modifier = Modifier,
    state: TimeAxisState,
    iobValues: List<Pair<Instant, Float>>,
    cobValues: List<Triple<Instant, Float, Float>>,
    maxIobRange: Int,
    maxCobRange: Int,
    baselineColor: Color,
    iobStrokeColor: Color,
    iobFillColor: Color,
    cobStrokeColor: Color,
    cobFillColor: Color
) {
    Canvas(modifier) {
        val getYForIob = { iob: Float -> size.height * (0.5f - 0.5f / maxIobRange * iob) }
        val getYForCob = { cob: Float -> size.height * (0.5f - 0.5f / maxCobRange * cob) }

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

        val linePath = Path()
        val fillPath = Path()

        var drawingActiveSegment = false
        var previousY: Float

        if (iobValues.isNotEmpty()) {
            val firstTimestamp = iobValues.first().first
            val firstIob = iobValues.first().second
            val firstX = ((firstTimestamp - state.windowStart) / durationPerPx).toFloat()
            val firstY = getYForIob(firstIob)

            if (firstIob != 0f) {
                linePath.moveTo(firstX, zeroY)
                linePath.lineTo(firstX, firstY)

                fillPath.moveTo(firstX, zeroY)
                fillPath.lineTo(firstX, firstY)

                drawingActiveSegment = true
            }

            previousY = firstY

            for (i in 1 until iobValues.size) {
                val currentTimestamp = iobValues[i].first
                val prevIob = iobValues[i - 1].second
                val currentIob = iobValues[i].second
                val currentX = ((currentTimestamp - state.windowStart) / durationPerPx).toFloat()
                val currentY = getYForIob(currentIob)

                if (currentIob != 0f) {
                    if (!drawingActiveSegment) {
                        linePath.moveTo(currentX, zeroY)
                        linePath.lineTo(currentX, currentY)

                        fillPath.moveTo(currentX, zeroY)
                        fillPath.lineTo(currentX, currentY)

                        drawingActiveSegment = true
                    } else {
                        if (abs(prevIob - currentIob) > 0.2) {
                            linePath.lineTo(currentX, previousY)
                            fillPath.lineTo(currentX, previousY)
                        }
                        linePath.lineTo(currentX, currentY)
                        fillPath.lineTo(currentX, currentY)
                    }
                } else {
                    if (drawingActiveSegment) {
                        linePath.lineTo(currentX, previousY)
                        linePath.lineTo(currentX, zeroY)

                        fillPath.lineTo(currentX, previousY)
                        fillPath.lineTo(currentX, zeroY)

                        drawingActiveSegment = false
                    }
                }
                previousY = currentY
            }

            if (drawingActiveSegment) {
                val lastX = ((iobValues.last().first - state.windowStart) / durationPerPx).toFloat()
                linePath.lineTo(lastX, zeroY)
                fillPath.lineTo(lastX, zeroY)
            }

        }

        drawPath(
            path = fillPath,
            color = iobFillColor
        )

        drawPath(
            path = linePath,
            color = iobStrokeColor,
            style = Stroke(width = 1.dp.toPx())
        )

        drawingActiveSegment = false
        linePath.reset()
        fillPath.reset()

        if (cobValues.isNotEmpty()) {
            val firstTimestamp = cobValues.first().first
            val firstCob = cobValues.first().second
            val firstX = ((firstTimestamp - state.windowStart) / durationPerPx).toFloat()
            val firstY = getYForIob(firstCob)

            if (firstCob != 0f) {
                linePath.moveTo(firstX, zeroY)
                linePath.lineTo(firstX, firstY)

                fillPath.moveTo(firstX, zeroY)
                fillPath.lineTo(firstX, firstY)

                drawingActiveSegment = true
            }

            previousY = firstY

            for (i in 1 until cobValues.size) {
                val currentTimestamp = cobValues[i].first
                val currentCob = cobValues[i].second
                val currentCarbsFromBolus = cobValues[i].third
                val currentX = ((currentTimestamp - state.windowStart) / durationPerPx).toFloat()
                val currentY = getYForCob(currentCob)

                if (currentCob != 0f) {
                    if (!drawingActiveSegment) {
                        linePath.moveTo(currentX, zeroY)
                        linePath.lineTo(currentX, currentY)

                        fillPath.moveTo(currentX, zeroY)
                        fillPath.lineTo(currentX, currentY)

                        drawingActiveSegment = true
                    } else {
                        if (currentCarbsFromBolus != 0f) {
                            linePath.lineTo(currentX, previousY)
                            fillPath.lineTo(currentX, previousY)
                        }
                        linePath.lineTo(currentX, currentY)
                        fillPath.lineTo(currentX, currentY)
                    }
                } else {
                    if (drawingActiveSegment) {
                        linePath.lineTo(currentX, previousY)
                        linePath.lineTo(currentX, zeroY)

                        fillPath.lineTo(currentX, previousY)
                        fillPath.lineTo(currentX, zeroY)

                        drawingActiveSegment = false
                    }
                }
                previousY = currentY
            }

            if (drawingActiveSegment) {
                val lastX = ((iobValues.last().first - state.windowStart) / durationPerPx).toFloat()
                linePath.lineTo(lastX, zeroY)
                fillPath.lineTo(lastX, zeroY)
            }

            drawPath(
                path = fillPath,
                color = cobFillColor
            )

            drawPath(
                path = linePath,
                color = cobStrokeColor,
                style = Stroke(width = 1.dp.toPx())
            )

        }
    }
}