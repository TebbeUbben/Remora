package de.tebbeubben.remora.ui.overview.graphs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.ui.overview.time_axis.TimeAxisState
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Composable
fun TimeLabels(
    modifier: Modifier = Modifier,
    state: TimeAxisState,
    fullHours: List<Pair<Instant, Int>>
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val durationPerPx = state.windowWidth / constraints.maxWidth.toDouble()

        val reference = subcompose("reference") {
            Text(
                text = "00",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }.first().measure(Constraints())

        val placeables = fullHours.map { (instant, text) ->
            subcompose(instant) {
                Text(
                    text = text.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }.first().measure(Constraints())
        }

        val hours = state.windowWidth / 1.hours

        var steps = 1
        do {
            val numLabels = hours / steps
            if (numLabels * reference.width + (32 * numLabels).dp.roundToPx() <= constraints.maxWidth) break
            steps *= 2
        } while (true)


        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val (instant, hour) = fullHours[index]
                if (hour % steps != 0) return@forEachIndexed
                val xOffset = (instant - state.windowStart) / durationPerPx
                val yOffset = constraints.maxHeight - placeable.height

                val centeredXOffset = xOffset - (placeable.width / 2)
                placeable.placeRelative(IntOffset(centeredXOffset.roundToInt(), yOffset))
            }
        }
    }
}