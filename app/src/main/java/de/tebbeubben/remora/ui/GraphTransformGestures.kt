package de.tebbeubben.remora.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMaxOfOrNull
import kotlin.math.abs

suspend fun PointerInputScope.detectGraphTransformGestures(
    onTransform: (centroid: Float, zoom: Float, pan: Float) -> Unit,
    onFling: (velocity: Float) -> Unit,
) {
    val velocityTracker = VelocityTracker()
    awaitEachGesture {
        var pastTouchSlop = false
        var zoom = 1f
        var pan = Offset.Zero
        val touchSlop = viewConfiguration.touchSlop
        var isZooming = false
        var panSinceZooming = 0f

        var virtualPointerPosition = Offset.Zero

        val down = awaitFirstDown(requireUnconsumed = false)
        velocityTracker.resetTracking()
        velocityTracker.addPosition(down.uptimeMillis, virtualPointerPosition)

        do {
            val event = awaitPointerEvent()
            if (event.changes.fastAny { it.isConsumed }) {
                if (pastTouchSlop) {
                    // Allow overscroll to settle
                    onFling(0.0f)
                }
                return@awaitEachGesture
            }

            // Accumulate zoom and pan, but don't submit to callbacks before reaching touch slop
            zoom *= event.calculateZoom()
            pan += event.calculatePan()

            val centroid = event.calculateCentroid()
            val centroidSize = event.calculateCentroidSize()
            val zoomMotion = abs(1 - zoom) * centroidSize

            if (event.type == PointerEventType.Move) {
                // For better multitouch support (i.e. flinging with multiple fingers), we drive a virtual pointer that's submitted to VelocityTracker.
                // We calculate the centroid of all current pointers and then offset the virtual pointer based on how much the centroid has moved.
                // We also user inter-frame ("historical") movements for higher precision.
                val previousCentroid = event.changes.map { it.previousPosition.x }.sum() / event.changes.size
                val newCentroid = event.changes.map { it.position.x }.sum() / event.changes.size
                val historicalCentroids = event.changes
                    .flatMap { it.historical }
                    .groupBy { it.uptimeMillis }
                    .toSortedMap()
                    .map { (uptimeMillis, changes) ->
                        uptimeMillis to changes.map { it.position.x }.sum() / changes.size
                    }
                val centroids = listOf((0L to previousCentroid)) + historicalCentroids + (event.changes.fastMaxOfOrNull { it.uptimeMillis }!! to newCentroid)
                val pans = centroids.windowed(2) { (prev, next) -> next.first to next.second - prev.second }
                pans.forEach { (uptimeMillis, pan) ->
                    virtualPointerPosition += Offset(pan, 0f)
                    velocityTracker.addPosition(uptimeMillis, virtualPointerPosition)
                }
            }

            pastTouchSlop = pastTouchSlop || zoomMotion > touchSlop || abs(pan.x) > touchSlop

            // Don't interfere with vertically scrolling parent.
            if (!pastTouchSlop && abs(pan.y) >= touchSlop && event.changes.size <= 1) return@awaitEachGesture

            if (pastTouchSlop) {
                // Don't fling if zoom gesture dominates pan gesture
                if (zoomMotion > abs(pan.x)) {
                    isZooming = true
                    panSinceZooming = 0f
                } else if (isZooming) {
                    // Pan gesture needs to overcome touch slop again before flinging
                    panSinceZooming += pan.x
                    isZooming = abs(panSinceZooming) <= touchSlop
                }

                if (zoom != 1f || pan.x != 0f) {
                    onTransform(centroid.x, zoom, pan.x)
                }

                zoom = 1f
                pan = Offset.Zero

                event.changes.fastForEach {
                    it.consume()
                }
            }
        } while (event.changes.fastAny { it.pressed })


        if (pastTouchSlop) {
            var velocity = velocityTracker.calculateVelocity(Velocity(viewConfiguration.maximumFlingVelocity, viewConfiguration.maximumFlingVelocity)).x
            if (isZooming || abs(velocity) < viewConfiguration.minimumFlingVelocity) {
                velocity = 0f
            }
            onFling(velocity)
        }
    }
}