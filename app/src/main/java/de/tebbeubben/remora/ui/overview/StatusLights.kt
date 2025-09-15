package de.tebbeubben.remora.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R
import de.tebbeubben.remora.ui.theme.RemoraTheme
import kotlin.collections.sumOf
import kotlin.math.pow

@Composable
@Preview
fun StatusLightsPreviewLight() {
    RemoraTheme(darkTheme = false) {
        StatusLightsPreview()
    }
}

@Composable
@Preview
fun StatusLightsPreviewDark() {
    RemoraTheme(darkTheme = true) {
        StatusLightsPreview()
    }
}

@Composable
fun StatusLightsPreview() {
    StatusLights(
        modifier = Modifier.width(500.dp)
    )
}

@Composable
fun StatusLights(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.large),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {

        StatusLight(
            icon = painterResource(R.drawable.mobile_24px),
            description = "Phone Battery",
            texts = listOf("100%" to Color.Unspecified)
        )
        StatusLight(
            icon = painterResource(R.drawable.cannula),
            description = "Cannula",
            texts = listOf("10d22h" to Color.Unspecified)
        )
        StatusLight(
            icon = painterResource(R.drawable.reservoir),
            description = "Reservoir",
            texts = listOf("230U" to Color.Unspecified, "10d22h" to Color.Unspecified)
        )
        StatusLight(
            icon = painterResource(R.drawable.battery_android_full_24px),
            description = "Pump Battery",
            texts = listOf("100%" to Color.Unspecified, "10d22h" to Color.Unspecified)
        )
        StatusLight(
            icon = painterResource(R.drawable.sensors_24px),
            description = "Sensor",
            texts = listOf("10d22h" to Color.Unspecified)
        )
    }
}
@Composable
fun RowScope.StatusLight(
    modifier: Modifier = Modifier,
    icon: Painter,
    description: String,
    texts: List<Pair<String, Color>>,
) {
    Column(
        modifier = modifier
            .weight((texts.sumOf { it.first.length } + texts.size - 1).toFloat().pow(0.5f))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            .padding(4.dp, 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = icon,
                contentDescription = description,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for ((text, color) in texts) {
                    Text(
                        text = text,
                        color = color,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

