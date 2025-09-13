package de.tebbeubben.remora.ui

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import kotlin.time.Instant
import kotlin.time.times

@Composable
fun BoxWithTimeAxis(
    modifier: Modifier = Modifier,
    start: Instant,
    end: Instant,
    minWindowWidth: Duration,
    viewableTimeWindowState: ViewableTimeWindowState,
    content: @Composable BoxWithTimeAxisScope.() -> Unit,
) {

    LaunchedEffect(start, end, minWindowWidth) {
        viewableTimeWindowState.start = start
        viewableTimeWindowState.end = end
        viewableTimeWindowState.minWindowWidth = minWindowWidth
        viewableTimeWindowState.mutate {
            if (windowWidth > end - start) {
                windowWidth = end - start
            } else if (windowWidth < minWindowWidth) {
                windowWidth = minWindowWidth
            }
            if (windowStart < start) {
                windowStart = start
            } else if (windowStart >= end - windowWidth) {
                windowStart = end - windowWidth
            }
        }
    }

    var boxSize by remember { mutableStateOf(IntSize(0, 0)) }

    val overscroll = rememberOverscrollEffect()
    val scope = rememberCoroutineScope()
    val pxToScaleRatio = with(LocalDensity.current) { 128.dp.toPx() }
    val density = LocalDensity.current

    val fling: suspend TransformScope.(Float) -> Float = { velocity ->
        var prev = 0f
        var remainingVelocity = 0f
        AnimationState(prev, velocity).animateDecay(splineBasedDecay(density)) {
            val durationPerPx = viewableTimeWindowState.windowWidth / boxSize.width
            val delta = value - prev
            prev = value
            val durationScrolled = scrollBy(-durationPerPx * delta.toDouble())
            if (durationScrolled == Duration.ZERO) {
                cancelAnimation()
                remainingVelocity = this.velocity
            }
        }
        remainingVelocity
    }

    Box(
        modifier = modifier
            .onSizeChanged { boxSize = it }
            .clipToBounds()
            .overscroll(overscroll)
            .pointerInput(Unit) {
                detectGraphTransformGestures(
                    onTransform = { centroid, zoom, pan ->
                        val durationPerPx = viewableTimeWindowState.windowWidth / boxSize.width
                        scope.launch {
                            viewableTimeWindowState.mutate {
                                zoom(windowStart + durationPerPx * centroid.toDouble(), zoom.toDouble())
                                overscroll?.applyToScroll(Offset(pan, 0f), NestedScrollSource.UserInput) { (x, _) ->
                                    Offset(-(scrollBy(-durationPerPx * x.toDouble()) / durationPerPx).toFloat(), 0f)
                                } ?: (scrollBy(-durationPerPx * pan.toDouble()) / durationPerPx)
                            }
                        }
                    },
                    onFling = { velocity ->
                        scope.launch {
                            viewableTimeWindowState.mutate {
                                overscroll?.applyToFling(Velocity(velocity, 0f)) { (velocityX, _) ->
                                    Velocity(velocityX - fling(velocityX), 0f)
                                } ?: fling(velocity)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        scope.launch { viewableTimeWindowState.mutate { } }
                    },
                    onDoubleTap = { offset ->
                        scope.launch {
                            val durationPerPx = viewableTimeWindowState.windowWidth / boxSize.width
                            viewableTimeWindowState.animateToZoomLevel(viewableTimeWindowState.windowStart + durationPerPx * offset.x.toDouble(), 2f)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragAfterDoubleTap { change, dragAmount ->
                    val zoom = 1.0 + dragAmount / pxToScaleRatio
                    val durationPerPx = viewableTimeWindowState.windowWidth / boxSize.width
                    val focalPoint = viewableTimeWindowState.windowStart + durationPerPx * change.position.x.toDouble()
                    scope.launch {
                        viewableTimeWindowState.mutate {
                            zoom(focalPoint, zoom)
                        }
                    }
                }
            }
    ) {

        val boxWithTimeAxisScope = remember {
            object : BoxWithTimeAxisScope(this) {
                override val start: Instant get() = start
                override val end: Instant get() = end
                override val size: IntSize get() = boxSize
                override val windowStart: Instant get() = viewableTimeWindowState.windowStart
                override val windowWidth: Duration get() = viewableTimeWindowState.windowWidth

                override fun getHorizontalPosition(time: Instant): Float {
                    val durationPerPx = viewableTimeWindowState.windowWidth / boxSize.width
                    return ((time - viewableTimeWindowState.windowStart) / durationPerPx).toFloat()
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

class ViewableTimeWindowState(
    initialWindowStart: Instant,
    initialWindowWidth: Duration,
) {

    var start = Instant.DISTANT_PAST
        set(value) {
            field = value
            if (windowStart < value) {
                windowStart = value
            }
        }
    var end = Instant.DISTANT_FUTURE
        set(value) {
            field = value
            if (windowStart > value - windowWidth) {
                windowStart = value - windowWidth
            }
        }

    var minWindowWidth = Duration.ZERO
        set(value) {
            field = value
            if (windowWidth < value) {
                windowWidth = value
            }
        }

    var windowStart by mutableStateOf(initialWindowStart)
        private set
    var windowWidth by mutableStateOf(initialWindowWidth)
        private set

    private val mutatorMutex = MutatorMutex()

    private val transformScope = object : TransformScope {
        override fun scrollBy(duration: Duration): Duration {
            val old = windowStart
            val new = (windowStart + duration).coerceIn(start, end - windowWidth)
            windowStart = new
            return new - old
        }

        override fun zoom(focalPoint: Instant, zoom: Double) {
            val oldWidth = windowWidth
            windowWidth = oldWidth / zoom

            val focalPointOffset = focalPoint - windowStart
            val drift = focalPointOffset / oldWidth * windowWidth - focalPointOffset

            windowStart = (windowStart - drift)
        }

        override var windowWidth: Duration
            get() = this@ViewableTimeWindowState.windowWidth
            set(value) {
                this@ViewableTimeWindowState.windowWidth = value.coerceIn(minWindowWidth, end - start)
            }

        override var windowStart: Instant
            get() = this@ViewableTimeWindowState.windowStart
            set(value) {
                this@ViewableTimeWindowState.windowStart = value.coerceIn(start, end - windowWidth)
            }

    }

    suspend fun <T> mutate(mutatePriority: MutatePriority = MutatePriority.Default, block: suspend TransformScope.() -> T) =
        mutatorMutex.mutateWith(transformScope, mutatePriority, block)

    suspend fun animateToZoomLevel(focalPoint: Instant, zoom: Float) {
        val oldStart = windowStart
        val oldWidth = windowWidth
        val focalPointOffset = focalPoint - oldStart

        mutate {
            AnimationState(
                initialValue = 1f,
            ).animateTo(zoom) {
                var newWidth = (oldWidth / value.toDouble())
                if (newWidth <= minWindowWidth) {
                    newWidth = minWindowWidth
                    cancelAnimation()
                }
                val drift = focalPointOffset / oldWidth * newWidth - focalPointOffset
                windowWidth = newWidth
                windowStart = oldStart - drift
            }
        }
    }
}

interface TransformScope {

    fun scrollBy(duration: Duration): Duration

    fun zoom(focalPoint: Instant, zoom: Double)

    var windowStart: Instant
    var windowWidth: Duration
}