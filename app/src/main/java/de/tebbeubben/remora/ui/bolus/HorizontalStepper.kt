package de.tebbeubben.remora.ui.bolus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalStepperItem(
    stepNumber: String,
    text: String,
    isActive: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Unspecified)
                .border(1.dp, if (!isActive) LocalContentColor.current else Color.Unspecified, RoundedCornerShape(50))
                .size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelLarge,
                color = if (isActive) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
            )
        }

        AnimatedVisibility(
            visible = isActive
        ) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}