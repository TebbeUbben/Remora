package de.tebbeubben.remora.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R
import de.tebbeubben.remora.ui.theme.LocalExtendedColors
import de.tebbeubben.remora.ui.theme.RemoraTheme

@Composable
@Preview
fun TherapyIndicatorsPreviewLight() {
    RemoraTheme(darkTheme = false) {
        TherapyIndicatorsPreview()
    }
}

@Composable
@Preview
fun TherapyIndicatorsPreviewDark() {
    RemoraTheme(darkTheme = true) {
        TherapyIndicatorsPreview()
    }
}

@Composable
fun TherapyIndicatorsPreview() {
    TherapyIndicators(
        iob = "0.65 U",
        cob = "25 g",
        basalRate = "1.02 U/h",
        basalRateClassification = BasalRateClassification.HIGH,
        autosensRatio = "102%"
    )
}

@Composable
fun TherapyIndicators(
    modifier: Modifier = Modifier,
    iob: String,
    cob: String,
    basalRate: String,
    basalRateClassification: BasalRateClassification,
    autosensRatio: String,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        TherapyIndicator(
            modifier = Modifier
                .weight(1f),
            icon = painterResource(R.drawable.water_drop_24px),
            description = "Insulin on Board",
            text = iob,
            color = LocalExtendedColors.current.bolus.color
        )
        TherapyIndicator(
            modifier = Modifier
                .weight(1f),
            icon = painterResource(R.drawable.grain_24px),
            description = "Carbs on Board",
            text = cob,
            color = LocalExtendedColors.current.carbs.color
        )
        TherapyIndicator(
            modifier = Modifier
                .weight(1f),
            icon = painterResource(
                when (basalRateClassification) {
                    BasalRateClassification.NEUTRAL -> R.drawable.neutral_basal
                    BasalRateClassification.HIGH    -> R.drawable.high_temp_basal
                    BasalRateClassification.LOW     -> R.drawable.low_temp_basal
                }
            ),
            description = "Basal Rate",
            text = basalRate,
            color = LocalExtendedColors.current.basal.color
        )
        TherapyIndicator(
            modifier = Modifier
                .weight(1f),
            icon = painterResource(R.drawable.autosens),
            description = "Autosens Ratio",
            text = autosensRatio,
            color = LocalExtendedColors.current.autosens.color
        )
    }
}

@Composable
private fun TherapyIndicator(
    modifier: Modifier,
    icon: Painter,
    description: String,
    text: String,
    color: Color,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = icon,
            contentDescription = description,
            tint = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

enum class BasalRateClassification {
    NEUTRAL,
    HIGH,
    LOW
}