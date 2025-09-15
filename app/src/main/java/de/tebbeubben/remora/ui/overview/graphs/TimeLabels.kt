package de.tebbeubben.remora.ui.overview.graphs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import de.tebbeubben.remora.ui.overview.time_axis.TimeAxisState
import kotlin.math.roundToInt
import kotlin.time.Instant

@Composable
fun TimeLabels(
    modifier: Modifier = Modifier,
    state: TimeAxisState,
    fullHours: List<Pair<Instant, String>>
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val durationPerPx = state.windowWidth / constraints.maxWidth.toDouble()

        val placeables = fullHours.map { (instant, text) ->
            subcompose(instant) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }.first().measure(Constraints())
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val (instant, _) = fullHours[index]
                val xOffset = (instant - state.windowStart) / durationPerPx
                val yOffset = constraints.maxHeight - placeable.height

                val centeredXOffset = xOffset - (placeable.width / 2)
                placeable.placeRelative(IntOffset(centeredXOffset.roundToInt(), yOffset))
            }
        }
    }
}