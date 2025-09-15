package de.tebbeubben.remora.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tebbeubben.remora.R
import de.tebbeubben.remora.lib.model.RemoraStatusData
import de.tebbeubben.remora.ui.theme.RemoraTheme

@Preview
@Composable
private fun GlucoseStatusPreviewLight() {
    RemoraTheme(darkTheme = false) {
        GlucoseStatusPreview()
    }
}

@Preview
@Composable
private fun GlucoseStatusPreviewDark() {
    RemoraTheme(darkTheme = true) {
        GlucoseStatusPreview()
    }
}

@Composable
private fun GlucoseStatusPreview() {
    Column {
        GlucoseStatus(
            value = "120",
            trendArrow = RemoraStatusData.TrendArrow.FLAT,
            glucoseAge = "3 minutes ago",
            delta = "+5",
            shortAverageDelta = "-15",
            longAverageDelta = "+20",
            runningMode = RemoraStatusData.RunningMode.CLOSED_LOOP,
            remainingDuration = null,
            bgClassification = BgClassification.IN_RANGE,
            isStale = false
        )
        GlucoseStatus(
            value = "56",
            trendArrow = RemoraStatusData.TrendArrow.FORTY_FIVE_DOWN,
            glucoseAge = "3 minutes ago",
            delta = "+5",
            shortAverageDelta = "-15",
            longAverageDelta = "+20",
            runningMode = RemoraStatusData.RunningMode.SUSPENDED_BY_USER,
            remainingDuration = null,
            bgClassification = BgClassification.BELOW_RANGE,
            isStale = false
        )
        GlucoseStatus(
            value = "213",
            trendArrow = RemoraStatusData.TrendArrow.DOUBLE_DOWN,
            glucoseAge = "26 minutes ago",
            delta = null,
            shortAverageDelta = null,
            longAverageDelta = null,
            runningMode = RemoraStatusData.RunningMode.OPEN_LOOP,
            remainingDuration = null,
            bgClassification = BgClassification.ABOVE_RANGE,
            isStale = true
        )
    }
}

@Composable
fun GlucoseStatus(
    modifier: Modifier = Modifier,
    value: String,
    isStale: Boolean,
    bgClassification: BgClassification,
    trendArrow: RemoraStatusData.TrendArrow,
    glucoseAge: String,
    delta: String?,
    shortAverageDelta: String?,
    longAverageDelta: String?,
    runningMode: RemoraStatusData.RunningMode,
    remainingDuration: String?,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.sweepGradient(
                        colors = when (bgClassification) {
                            BgClassification.IN_RANGE    -> listOf(Color(0xFF00FF00), Color(0x0000FF00))
                            BgClassification.ABOVE_RANGE -> listOf(Color(0xFFFFFF00), Color(0x00FFFF00))
                            BgClassification.BELOW_RANGE -> listOf(Color(0xFFFF0000), Color(0x00FF0000))
                        },
                        center = Offset.Infinite
                    )
                ),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BgValue(
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                value = value,
                trendArrow = trendArrow,
                glucoseAge = glucoseAge,
                delta = delta,
                shortAverageDelta = shortAverageDelta,
                longAverageDelta = longAverageDelta,
                isStale = isStale
            )
            LoopStatus(
                modifier = Modifier
                    .weight(1f),
                runningMode = runningMode,
                remainingDuration = remainingDuration
            )
        }
    }
}

@Composable
private fun LoopStatus(
    modifier: Modifier = Modifier,
    runningMode: RemoraStatusData.RunningMode,
    remainingDuration: String?,
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(
                when (runningMode) {
                    RemoraStatusData.RunningMode.OPEN_LOOP         -> R.drawable.open_loop
                    RemoraStatusData.RunningMode.CLOSED_LOOP       -> R.drawable.loop
                    RemoraStatusData.RunningMode.CLOSED_LOOP_LGS   -> R.drawable.lgs
                    RemoraStatusData.RunningMode.DISABLED_LOOP     -> R.drawable.loop_disabled
                    RemoraStatusData.RunningMode.SUPER_BOLUS       -> R.drawable.loop_superbolus
                    RemoraStatusData.RunningMode.DISCONNECTED_PUMP -> R.drawable.power_off_24px
                    RemoraStatusData.RunningMode.SUSPENDED_BY_PUMP -> R.drawable.loop_paused
                    RemoraStatusData.RunningMode.SUSPENDED_BY_USER -> R.drawable.loop_paused
                }
            ),
            contentDescription = when (runningMode) {
                RemoraStatusData.RunningMode.OPEN_LOOP         -> "Open Loop"
                RemoraStatusData.RunningMode.CLOSED_LOOP       -> "Closed Loop"
                RemoraStatusData.RunningMode.CLOSED_LOOP_LGS   -> "Low Glucose Suspend"
                RemoraStatusData.RunningMode.DISABLED_LOOP     -> "Loop Disabled"
                RemoraStatusData.RunningMode.SUPER_BOLUS       -> "Super Bolus"
                RemoraStatusData.RunningMode.DISCONNECTED_PUMP -> "Pump Disconnected"
                RemoraStatusData.RunningMode.SUSPENDED_BY_PUMP -> "Suspended by Pump"
                RemoraStatusData.RunningMode.SUSPENDED_BY_USER -> "Suspended by User"
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        val modeText = when (runningMode) {
            RemoraStatusData.RunningMode.OPEN_LOOP         -> "Open Loop"
            RemoraStatusData.RunningMode.CLOSED_LOOP       -> "Closed Loop"
            RemoraStatusData.RunningMode.CLOSED_LOOP_LGS   -> "LGS"
            RemoraStatusData.RunningMode.DISABLED_LOOP     -> "Loop Disabled"
            RemoraStatusData.RunningMode.SUPER_BOLUS       -> "Super Bolus"
            RemoraStatusData.RunningMode.DISCONNECTED_PUMP -> "Pump Disconnected"
            RemoraStatusData.RunningMode.SUSPENDED_BY_PUMP -> "Suspended"
            RemoraStatusData.RunningMode.SUSPENDED_BY_USER -> "Suspended"
        }

        Text(
            text = modeText.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (remainingDuration != null) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = remainingDuration,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BgValue(
    modifier: Modifier = Modifier,
    value: String,
    isStale: Boolean,
    trendArrow: RemoraStatusData.TrendArrow,
    glucoseAge: String,
    delta: String?,
    shortAverageDelta: String?,
    longAverageDelta: String?,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.W900,
                fontSize = 57.sp,
                lineHeight = 57.sp,
                letterSpacing = 0.sp,
                textDecoration = if (isStale) TextDecoration.LineThrough else TextDecoration.None
            )
            Spacer(modifier = Modifier.width(8.dp))
            TrendArrow(trendArrow = trendArrow)
            Spacer(modifier = Modifier.width(8.dp))
            if (delta != null && shortAverageDelta != null && longAverageDelta != null) {
                Deltas(
                    delta = delta,
                    shortAverageDelta = shortAverageDelta,
                    longAverageDelta = longAverageDelta
                )
            }
        }
        Text(
            text = glucoseAge,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun Deltas(
    modifier: Modifier = Modifier,
    delta: String,
    shortAverageDelta: String,
    longAverageDelta: String,
) {
    Row(
        modifier = modifier
    ) {
        Column {
            Text(
                text = "",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "15m",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "40m",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = delta,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = shortAverageDelta,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = longAverageDelta,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TrendArrow(
    modifier: Modifier = Modifier,
    trendArrow: RemoraStatusData.TrendArrow,
) {
    Icon(
        modifier = modifier
            .rotate(
                when (trendArrow) {
                    RemoraStatusData.TrendArrow.NONE            -> 0f
                    RemoraStatusData.TrendArrow.TRIPLE_UP       -> -90f
                    RemoraStatusData.TrendArrow.DOUBLE_UP       -> -90f
                    RemoraStatusData.TrendArrow.SINGLE_UP       -> -90f
                    RemoraStatusData.TrendArrow.FORTY_FIVE_UP   -> -45f
                    RemoraStatusData.TrendArrow.FLAT            -> 0f
                    RemoraStatusData.TrendArrow.FORTY_FIVE_DOWN -> 45f
                    RemoraStatusData.TrendArrow.SINGLE_DOWN     -> 90f
                    RemoraStatusData.TrendArrow.DOUBLE_DOWN     -> 90f
                    RemoraStatusData.TrendArrow.TRIPLE_DOWN     -> 90f
                }
            )
            .size(48.dp),
        painter = painterResource(
            when (trendArrow) {
                RemoraStatusData.TrendArrow.NONE        -> R.drawable.question_mark_24px
                RemoraStatusData.TrendArrow.TRIPLE_UP   -> R.drawable.tripple_arow
                RemoraStatusData.TrendArrow.TRIPLE_DOWN -> R.drawable.tripple_arow
                RemoraStatusData.TrendArrow.DOUBLE_UP   -> R.drawable.double_arrow
                RemoraStatusData.TrendArrow.DOUBLE_DOWN -> R.drawable.double_arrow
                else                                    -> R.drawable.arrow
            }
        ),
        contentDescription = when (trendArrow) {
            RemoraStatusData.TrendArrow.NONE            -> "No Trend Arrow"
            RemoraStatusData.TrendArrow.TRIPLE_UP       -> "Rising extremely fast"
            RemoraStatusData.TrendArrow.DOUBLE_UP       -> "Rising very fast"
            RemoraStatusData.TrendArrow.SINGLE_UP       -> "Rising fast"
            RemoraStatusData.TrendArrow.FORTY_FIVE_UP   -> "Rising slowly"
            RemoraStatusData.TrendArrow.FLAT            -> "Steady"
            RemoraStatusData.TrendArrow.FORTY_FIVE_DOWN -> "Falling slowly"
            RemoraStatusData.TrendArrow.SINGLE_DOWN     -> "Falling fast"
            RemoraStatusData.TrendArrow.DOUBLE_DOWN     -> "Falling very fast"
            RemoraStatusData.TrendArrow.TRIPLE_DOWN     -> "Falling extremely fast"
        },
    )
}

enum class BgClassification {
    IN_RANGE,
    ABOVE_RANGE,
    BELOW_RANGE
}