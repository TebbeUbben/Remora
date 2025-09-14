package de.tebbeubben.remora.ui.time_axis

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.time.times

class TimeAxisState(
    initialStart: Instant,
    initialEnd: Instant,
    initialWindowStart: Instant,
    initialWindowWidth: Duration,
    initialMinWindowWidth: Duration,
    initialMaxWindowWidth: Duration,
) {

    private var _start by mutableStateOf(initialStart)
    private var _end by mutableStateOf(initialEnd)
    private var _minWindowWidth by mutableStateOf(initialMinWindowWidth)
    private var _maxWindowWidth by mutableStateOf(initialMaxWindowWidth)


    var windowStart by mutableStateOf(initialWindowStart)
        private set
    var windowWidth by mutableStateOf(initialWindowWidth)
        private set

    val windowEnd get() = windowStart + windowWidth

    init {
        require(_end > _start) { "End must be after start." }
        require(_minWindowWidth > Duration.ZERO) { "Minimum window width must be greater than zero." }
        require(_minWindowWidth <= _end - _start) { "Minimum window width must not be greater than total time window." }
        require(_maxWindowWidth >= _minWindowWidth) { "Maximum window width must not be smaller than minimum." }

        // Grow current window width if it falls below minimum
        if (windowWidth < _minWindowWidth) windowWidth = _minWindowWidth
        // Shrink current window width if it exceeds total window size
        else if (windowWidth > _end - _start) windowWidth = _end - _start
        // Shrink current window width if it exceeds maximum
        else if (windowWidth > _maxWindowWidth) windowWidth = _maxWindowWidth

        // Move current window if it overlaps the start
        if (windowStart < _start) windowStart = _start
        // Move current window if it overlaps the end
        else if (windowStart + windowWidth > _end) windowStart = _end - windowWidth
    }

    var start: Instant
        get() = _start
        set(value) {
            require(_end > value) { "Start must be before end" }
            require(_minWindowWidth <= _end - value) { "New value of start would result in a total window smaller than current minimum window width." }
            _start = value
            // Shrink current window width if it exceeds new total window size
            if (windowWidth > _end - value) windowWidth = _end - value
            // Move current window if it overlaps new start
            if (windowStart < value) windowStart = value
        }

    var end: Instant
        get() = _end
        set(value) {
            require(value > _start) { "End must be after start." }
            require(_minWindowWidth <= value - _start) { "New value of end would result in a total window smaller than current minimum window width." }
            _end = value
            // Shrink current window width if it exceeds new total window size
            if (windowWidth > value - _start) windowWidth = value - _start
            // Move current window if it overlaps new end
            if (windowStart + windowWidth > value) windowStart = _end - windowWidth
        }

    var minWindowWidth: Duration
        get() = this@TimeAxisState._minWindowWidth
        set(value) {
            require(value > Duration.ZERO) { "Minimum window width must be greater than zero." }
            require(value <= _end - _start) { "Minimum window width must not be greater than total time window." }
            require(_maxWindowWidth >= value) { "Minimum window width must not be greater than maximum." }
            _minWindowWidth = value
            // Grow current window if it falls below new minimum
            if (windowWidth < value) windowWidth = value
            // Move current window if new size overlaps the end
            if (windowStart + windowWidth > end) windowStart = end - windowWidth
        }

    var maxWindowWidth: Duration
        get() = _maxWindowWidth
        set(value) {
            require(value >= _minWindowWidth) { "Maximum window width must not be smaller than minimum." }
            _maxWindowWidth = value
            // Shrink current window width if it exceeds new maximum
            if (windowWidth > value) windowWidth = value
        }

    private val transformScope = object : TimeAxisWindowTransformScope {
        override val start: Instant get() = _start
        override val end: Instant get() = _end
        override val minWindowWidth: Duration get() = _minWindowWidth
        override val maxWindowWidth: Duration get() = _maxWindowWidth

        override var windowStart: Instant
            get() = this@TimeAxisState.windowStart
            set(value) {
                this@TimeAxisState.windowStart = value.coerceIn(start, end - windowWidth)
            }
        override var windowWidth: Duration
            get() = this@TimeAxisState.windowWidth
            set(value) {
                this@TimeAxisState.windowWidth = value.coerceIn(minWindowWidth, minOf(maxWindowWidth, end - start))
                // Move current window if new size overlaps the end
                if (windowStart + windowWidth > end) windowStart = end - windowWidth
            }

    }

    private val mutatorMutex = MutatorMutex()

    suspend fun <T> mutate(mutatePriority: MutatePriority = MutatePriority.Default, block: suspend TimeAxisWindowTransformScope.() -> T) =
        mutatorMutex.mutateWith(transformScope, mutatePriority, block)

    suspend fun cancelCurrentMutation(mutatePriority: MutatePriority = MutatePriority.Default) = mutatorMutex.mutate(mutatePriority) {  }

    companion object {

        val Saver: Saver<TimeAxisState, *> = mapSaver(
            save = {
                mapOf(
                    "startEpochSeconds" to it._start.epochSeconds,
                    "startNanosecondsOfSecond" to it._start.nanosecondsOfSecond,
                    "endEpochSeconds" to it._end.epochSeconds,
                    "endNanosecondsOfSecond" to it._end.nanosecondsOfSecond,
                    "windowStartEpochSeconds" to it.windowStart.epochSeconds,
                    "windowStartNanosecondsOfSecond" to it.windowStart.nanosecondsOfSecond,
                    "windowWidth" to it.windowWidth.toIsoString(),
                    "minWindowWidth" to it._minWindowWidth.toIsoString(),
                    "maxWindowWidth" to it._maxWindowWidth.toIsoString()
                )
            },
            restore = {
                val start = Instant.fromEpochSeconds(it["startEpochSeconds"] as Long, it["startNanosecondsOfSecond"] as Int)
                val end = Instant.fromEpochSeconds(it["endEpochSeconds"] as Long, it["endNanosecondsOfSecond"] as Int)
                val windowStart = Instant.fromEpochSeconds(it["windowStartEpochSeconds"] as Long, it["windowStartNanosecondsOfSecond"] as Int)
                val windowWidth = Duration.parseIsoString(it["windowWidth"] as String)
                val minWindowWidth = Duration.parseIsoString(it["minWindowWidth"] as String)
                val maxWindowWidth = Duration.parseIsoString(it["maxWindowWidth"] as String)
                TimeAxisState(start, end, windowStart, windowWidth, minWindowWidth, maxWindowWidth)
            }
        )
    }
}

@Composable
fun rememberTimeAxisState(
    initialStart: Instant,
    initialEnd: Instant,
    initialWindowStart: Instant,
    initialWindowWidth: Duration,
    initialMinWindowWidth: Duration,
    initialMaxWindowWidth: Duration,
): TimeAxisState =
    rememberSaveable(saver = TimeAxisState.Saver) {
        TimeAxisState(
            initialStart,
            initialEnd,
            initialWindowStart,
            initialWindowWidth,
            initialMinWindowWidth,
            initialMaxWindowWidth
        )
    }

interface TimeAxisWindowTransformScope {
    val start: Instant
    val end: Instant
    val minWindowWidth: Duration
    val maxWindowWidth: Duration
    var windowStart: Instant
    var windowWidth: Duration
}

fun TimeAxisWindowTransformScope.zoom(focalPoint: Duration, zoom: Float) {
    val oldWidth = windowWidth
    windowWidth = oldWidth / zoom.toDouble()
    val drift = focalPoint / oldWidth * windowWidth - focalPoint
    windowStart = (windowStart - drift)
}

suspend fun TimeAxisWindowTransformScope.animateToZoomLevel(focalPoint: Duration, zoom: Float) {
    val oldStart = windowStart
    val oldWidth = windowWidth
    AnimationState(
        initialValue = 1f,
    ).animateTo(zoom) {
        var newWidth = (oldWidth / value.toDouble())
        if (newWidth <= minWindowWidth) {
            newWidth = minWindowWidth
            cancelAnimation()
        }
        val drift = focalPoint / oldWidth * newWidth - focalPoint
        windowWidth = newWidth
        windowStart = oldStart - drift
    }
}