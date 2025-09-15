package de.tebbeubben.remora.ui.overview

import android.util.Log
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.ui.theme.RemoraTheme
import de.tebbeubben.remora.ui.overview.time_axis.detectVerticalDragAfterDoubleTap
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.time.times

@Composable
@Preview
fun CanvasTest() {
    RemoraTheme {
        var end by remember { mutableStateOf(Instant.fromEpochSeconds(1757102465)) }
        var start by remember { mutableStateOf(end - 24.hours) }

        var windowWidth by remember { mutableStateOf(6.hours) }
        var windowStart by remember { mutableStateOf(end - windowWidth) }

        var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }

        val timezone = TimeZone.currentSystemDefault()

        val fullHours = remember(start, end) {
            val startDateTime = start.toLocalDateTime(timezone)
            val withoutFractionalHours = LocalDateTime(
                year = startDateTime.year,
                month = startDateTime.month,
                day = startDateTime.day,
                hour = startDateTime.hour,
                minute = 0,
                second = 0,
                nanosecond = 0
            )
            val startInstant = withoutFractionalHours.toInstant(timezone)
            generateSequence(startInstant) { it + 1.hours }.takeWhile { it <= end }.toList()
        }

        fun DrawScope.xAxisToPixel(value: Instant): Float {
            val relativePosition = (value - windowStart).inWholeSeconds.toDouble() /
                windowWidth.inWholeSeconds.toDouble()
            return (relativePosition * size.width).toFloat()
        }

        val scope = rememberCoroutineScope()
        var zoomAnimJob by remember { mutableStateOf<Job>(Job()) }

        val textMeasurer = rememberTextMeasurer()
        val labelStyle = MaterialTheme.typography.labelSmall
        val minWindowWidth = 1.hours
        val maxWindowWidth = end - start

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .height(500.dp)
                    .fillMaxWidth()
                    .background(Color.Blue)
            )
            val overscroll = rememberOverscrollEffect()

            val scrollableState = rememberScrollableState { delta: Float ->
                Log.d("Gestures", "Scroll!")
                zoomAnimJob.cancel()
                val durationPerPx = windowWidth / canvasSize.width
                val old = windowStart
                val new = (windowStart - durationPerPx * delta.toDouble()).coerceIn(start, end - windowWidth)
                windowStart = new
                -((new - old) / durationPerPx).toFloat()
            }

            val pxToScaleRatio = with(LocalDensity.current) { 128.dp.toPx() }

            fun animateToZoomLevel(centroid: Instant, zoom: Float) {
                zoomAnimJob.cancel()
                zoomAnimJob = scope.launch {
                    val oldWidth = windowWidth
                    val oldStart = windowStart
                    val centroidOffset = centroid - windowStart
                    AnimationState(
                        initialValue = 1f,
                    ).animateTo(zoom) {
                        var newWidth = (oldWidth / value.toDouble())
                        if (newWidth <= minWindowWidth) {
                            newWidth = minWindowWidth
                            cancelAnimation()
                        }
                        val drift = centroidOffset / oldWidth * newWidth - centroidOffset
                        windowWidth = newWidth
                        windowStart = (oldStart - drift).coerceIn(start, end - newWidth)
                    }
                }
            }

            fun zoom(centroid: Instant, zoom: Double, pan: Duration = Duration.ZERO) {
                zoomAnimJob.cancel()

                val oldWidth = windowWidth
                val requestedWidth = oldWidth / zoom
                windowWidth = requestedWidth.coerceIn(minWindowWidth, maxWindowWidth)

                val centroidOffset = centroid - windowStart
                val drift = centroidOffset / oldWidth * windowWidth - centroidOffset

                windowStart = (windowStart - drift - pan).coerceIn(start, end - windowWidth)
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .border(1.dp, Color.White)
                    .onSizeChanged { canvasSize = it }
                    .clipToBounds()
                    .overscroll(overscroll)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { zoomAnimJob.cancel() },
                            onDoubleTap = { offset ->
                                zoomAnimJob = scope.launch {
                                    animateToZoomLevel(windowStart + windowWidth / canvasSize.width * offset.x.toDouble(), 2f)
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.size <= 1) continue
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                val centroid = event.calculateCentroid()

                                zoom(windowStart + windowWidth / canvasSize.width * centroid.x.toDouble(), zoom.toDouble(), windowWidth / canvasSize.width * pan.x.toDouble())
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                    .scrollable(
                        state = scrollableState,
                        orientation = Orientation.Horizontal,
                        overscrollEffect = overscroll,
                        flingBehavior = ScrollableDefaults.flingBehavior()
                    )
                    /*.pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            Log.d("Gestures", "Transform!")
                            val durationPerPx = viewableTimeWindowState.windowWidth / boxSize.width
                            val panTime = durationPerPx * -pan.x.toDouble()
                            val focalPoint = viewableTimeWindowState.windowStart + durationPerPx * centroid.x.toDouble()
                            /*runBlocking {
                                viewableTimeWindowState.mutate {
                                    zoom(focalPoint, zoom.toDouble(), panTime)
                                }
                            }*/
                            val panTime = windowWidth / canvasSize.width * panChange.x.toDouble()
                            val centroid = windowStart + windowWidth / canvasSize.width * centroid.x.toDouble()
                            zoom(centroid, zoomChange.toDouble(), panTime)
                        }
                    }*/
                    .pointerInput(Unit) {
                        detectVerticalDragAfterDoubleTap() { change, dragAmount ->
                            zoomAnimJob.cancel()
                            val zoom = 1.0 + dragAmount / pxToScaleRatio
                            val centroid = windowStart + windowWidth / canvasSize.width * change.position.y.toDouble()
                            zoom(centroid, zoom)
                        }
                    }
            ) {
                var current = start
                while (current <= end) {
                    val sinValue = sin(2 * PI / 2.hours.inWholeSeconds * current.epochSeconds) + 0.5
                    val xPosition = xAxisToPixel(current)
                    val y = (size.height - 800) * sinValue + 400
                    drawCircle(
                        Color.White,
                        radius = 10f,
                        center = Offset(xPosition, y.toFloat())
                    )
                    current += 5.minutes
                }
                fullHours.forEach { hourInstant ->
                    val xPosition = xAxisToPixel(hourInstant)
                    val hourDateTime = hourInstant.toLocalDateTime(timezone)
                    val hourText = hourDateTime.hour.toString()

                    val measuredText =
                        textMeasurer.measure(
                            text = hourText,
                            style = labelStyle
                        )
                    drawText(
                        measuredText,
                        color = Color.White,
                        topLeft = Offset(xPosition - measuredText.size.width / 2, size.height - measuredText.size.height)

                    )
                }
            }
            Box(
                modifier = Modifier
                    .height(500.dp)
                    .fillMaxWidth()
                    .background(Color.Red)
            )
        }
    }
}