package de.tebbeubben.remora.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R

@Composable
fun MissingDataScreen() {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val c = MaterialTheme.colorScheme.primary
            val r = c.red
            val g = c.green
            val b = c.blue

            // standard sRGB luminance weights
            val Lr = 0.2126f; val Lg = 0.7152f; val Lb = 0.0722f

            val matrix = floatArrayOf(
                r*Lr, r*Lg, r*Lb, 0f, 0f,   // R' = L * r
                g*Lr, g*Lg, g*Lb, 0f, 0f,   // G' = L * g
                b*Lr, b*Lg, b*Lb, 0f, 0f,   // B' = L * b
                0f,   0f,   0f,   1f, 0f    // keep alpha
            )

            Box(
                modifier = Modifier.width(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    painter = painterResource(R.drawable.missing_data_figure_background),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primaryFixed, BlendMode.SrcAtop)
                )
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    painter = painterResource(R.drawable.missing_data_figure_fill),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primaryFixedDim, BlendMode.SrcAtop)
                )
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    painter = painterResource(R.drawable.missing_data_figure_stroke),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryFixedVariant, BlendMode.SrcAtop)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No data available",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Waiting for AndroidAPS to send data to Remora…",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}