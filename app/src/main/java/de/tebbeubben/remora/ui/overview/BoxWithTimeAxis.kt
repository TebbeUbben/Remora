package de.tebbeubben.remora.ui.overview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import de.tebbeubben.remora.ui.overview.time_axis.TimeAxisState
import de.tebbeubben.remora.ui.overview.time_axis.timeAxis
import kotlin.time.Duration
import kotlin.time.Instant

@Composable
fun BoxWithTimeAxis(
    modifier: Modifier = Modifier,
    state: TimeAxisState,
    content: @Composable BoxWithTimeAxisScope.() -> Unit,
) {

    var boxSize by remember { mutableStateOf(IntSize(0, 0)) }

    val overscroll = rememberOverscrollEffect()

    Box(
        modifier = modifier
            .onSizeChanged { boxSize = it }
            .clipToBounds()
            .overscroll(overscroll)
            .timeAxis(state, overscroll)
    ) {

        val boxWithTimeAxisScope = remember {
            object : BoxWithTimeAxisScope(this) {
                override val start: Instant get() = start
                override val end: Instant get() = end
                override val size: IntSize get() = boxSize
                override val windowStart: Instant get() = state.windowStart
                override val windowWidth: Duration get() = state.windowWidth

                override fun getHorizontalPosition(time: Instant): Float {
                    val durationPerPx = state.windowWidth / boxSize.width
                    return ((time - state.windowStart) / durationPerPx).toFloat()
                }
            }
        }

        boxWithTimeAxisScope.content()
    }
}

interface TimeSeriesScope {

    val start: Instant
    val end: Instant
    val size: IntSize
    val windowStart: Instant
    val windowWidth: Duration
    val windowEnd: Instant get() = windowStart + windowWidth

    fun getHorizontalPosition(time: Instant): Float
}

abstract class BoxWithTimeAxisScope(boxScope: BoxScope) : TimeSeriesScope, BoxScope by boxScope