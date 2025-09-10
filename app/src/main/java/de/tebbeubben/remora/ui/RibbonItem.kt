package de.tebbeubben.remora.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R
import de.tebbeubben.remora.ui.theme.RemoraTheme

@Composable
@Preview
fun RibbonItemPreviewLight() {
    RemoraTheme(darkTheme = false) {
        RibbonItemPreview()
    }
}

@Composable
@Preview
fun RibbonItemPreviewDark() {
    RemoraTheme(darkTheme = true) {
        RibbonItemPreview()
    }
}

@Composable
fun RibbonItemPreview() {
    RibbonItem(
        icon = painterResource(R.drawable.kid_star_24px),
        description = "Active Profile",
        text = "LocalProfile1",
        activeText = "90% +1h, 56m"
    )
}

@Composable
fun RibbonItem(modifier: Modifier = Modifier, icon: Painter, description: String, text: String, activeText: String?) {
    Surface(
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(50),
        tonalElevation = 8.dp,
        color = if (activeText != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(modifier = Modifier.size(16.dp), painter = icon, contentDescription = description)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (activeText != null) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = activeText,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}