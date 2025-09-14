package de.tebbeubben.remora.ui.graphs

import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.ui.time_axis.TimeAxisState
import kotlin.time.Instant

@Composable
fun TimeGrid(
    modifier: Modifier = Modifier,
    state: TimeAxisState,
    fullHours: List<Instant>,
    lineColor: Color = LocalContentColor.current.copy(alpha = 0.1f)
) {
    Canvas(
        modifier = modifier
    ) {
        val durationPerPx = state.windowWidth / size.width.toDouble()
        for (instant in fullHours) {
            val xOffset = ((instant - state.windowStart) / durationPerPx).toFloat() + 1.dp.toPx() / 2
            drawLine(
                color = lineColor,
                strokeWidth = 1.dp.toPx(),
                start = Offset(xOffset, 0f),
                end = Offset(xOffset, size.height)
            )
        }
    }
}