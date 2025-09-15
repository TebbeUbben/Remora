package de.tebbeubben.remora.ui.overview.time_axis

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.time.Duration

@Composable
fun Modifier.timeAxis(
    state: TimeAxisState,
    overscrollEffect: OverscrollEffect?,
): Modifier {
    var viewSize by remember { mutableStateOf(IntSize(0, 0)) }
    val durationPerPx by rememberUpdatedState(state.windowWidth / viewSize.width)
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val pxToScaleRatio = remember(density) { with(density) { 128.dp.toPx() } }
    val flingAnimationSpec = rememberSplineBasedDecay<Float>()

    val fling: suspend TimeAxisWindowTransformScope.(Float) -> Float = { velocity ->
        var prev = 0f
        var remainingVelocity = 0f
        AnimationState(prev, velocity).animateDecay(flingAnimationSpec) {
            val delta = value - prev
            prev = value
            val previousValue = windowStart
            windowStart = windowStart - durationPerPx * delta.toDouble()
            if (windowStart - previousValue == Duration.ZERO) {
                cancelAnimation()
                remainingVelocity = this.velocity
            }
        }
        remainingVelocity
    }

    val scroll: TimeAxisWindowTransformScope.(Float) -> Float = { delta ->
            val previousValue = windowStart
            windowStart = windowStart - durationPerPx * delta.toDouble()
            ((previousValue - windowStart) / durationPerPx).toFloat()
        }

    val mutate: (suspend TimeAxisWindowTransformScope.() -> Unit) -> Unit = { block ->
        scope.launch { state.mutate(MutatePriority.UserInput, block = block) }
    }

    return this
        .onSizeChanged { viewSize = it }
        .pointerInput(Unit) {
            detectTimeAxisTransformGestures(
                onTransform = { centroid, zoom, pan ->
                    mutate {
                        zoom(durationPerPx * centroid.toDouble(), zoom)
                        overscrollEffect?.applyToScroll(Offset(pan, 0f), NestedScrollSource.UserInput) { (delta, _) ->
                            Offset(scroll(delta), 0f)
                        } ?: scroll(pan)
                    }
                },
                onFling = { velocity ->
                    mutate {
                        overscrollEffect?.applyToFling(Velocity(velocity, 0f)) { (velocityX, _) ->
                            Velocity(velocityX - fling(velocityX), 0f)
                        } ?: fling(velocity)
                    }
                }
            )
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { mutate { animateToZoomLevel(durationPerPx * it.x.toDouble(), 2f) } }
            )
        }
        .pointerInput(Unit) {
            detectVerticalDragAfterDoubleTap { change, dragAmount ->
                val zoom = 1f + dragAmount / pxToScaleRatio
                mutate { zoom(durationPerPx * change.position.x.toDouble(), zoom) }
            }
        }
}