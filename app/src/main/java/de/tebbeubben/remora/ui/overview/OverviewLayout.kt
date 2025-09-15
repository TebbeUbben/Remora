package de.tebbeubben.remora.ui.overview

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * This layout places all items vertically.
 * An element can be specified with [Modifier.overviewLayoutData] to take up all remaining space.
 * The modifier also allows you specify a minimum height for this view.
 * If the total height is greater than the available space, it will use vertical scrolling.
 */
@Composable
fun OverviewLayout(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        SubcomposeLayout(
            Modifier.verticalScroll(scrollState)
        ) { constraints ->
            val measureables = subcompose(content) {
                content()
            }

            var height = 0
            val placeables = measureables
                .map {
                    if ((it.parentData as? OverviewLayoutData)?.fillRemainingSpace == true) return@map null
                    val placeable = it.measure(Constraints(maxWidth = constraints.maxWidth))
                    height += placeable.height
                    placeable
                }
                .toMutableList()

            val measureable = measureables.first { (it.parentData as? OverviewLayoutData)?.fillRemainingSpace == true }
            val minHeight = (measureable.parentData as OverviewLayoutData).minHeight?.roundToPx()
            var remainingHeight = (this@BoxWithConstraints.constraints.maxHeight - height)
            if (minHeight != null && remainingHeight < minHeight) remainingHeight = minHeight
            val placeable = measureable.measure(Constraints(maxWidth = constraints.maxWidth, maxHeight = remainingHeight))
            height += placeable.height

            placeables[placeables.indexOfFirst { it == null }] = placeable

            layout(constraints.maxWidth, height) {
                var currentY = 0
                for (placeable in placeables) {
                    placeable!!.place(0, currentY)
                    currentY += placeable.height
                }
            }
        }
    }
}

private data class OverviewLayoutData(val fillRemainingSpace: Boolean, val minHeight: Dp?)

private data class OverviewLayoutModifier(val data: OverviewLayoutData) : ParentDataModifier {

    override fun Density.modifyParentData(parentData: Any?): Any = data
}

fun Modifier.overviewLayoutData(fillRemainingSpace: Boolean, minHeight: Dp? = null) =
    this.then(OverviewLayoutModifier(OverviewLayoutData(fillRemainingSpace, minHeight)))