package de.tebbeubben.remora.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitVerticalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange

suspend fun PointerInputScope.detectVerticalDragAfterDoubleTap(
    onDragStart: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDrag: (change: PointerInputChange, dragAmount: Float) -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown()
        val up = waitForUpOrCancellation() ?: return@awaitEachGesture
        val secondDown = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
            val minUptime = up.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
            var change: PointerInputChange
            do {
                change = awaitFirstDown()
            } while (change.uptimeMillis < minUptime)
            change
        } ?: return@awaitEachGesture
        val slopChange = awaitVerticalTouchSlopOrCancellation(secondDown.id) { change, overSlop ->
            onDragStart(change.position)
            onDrag(change, overSlop)
            change.consume()
        } ?: return@awaitEachGesture
        val hasEndedNormally = verticalDrag(slopChange.id) { change ->
            onDrag(change, change.positionChange().y)
            change.consume()
        }
        if (hasEndedNormally) {
            onDragEnd()
        } else {
            onDragCancel()
        }
    }
}