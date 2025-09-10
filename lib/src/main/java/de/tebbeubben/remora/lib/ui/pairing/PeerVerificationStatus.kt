package de.tebbeubben.remora.lib.ui.pairing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R

@Composable
internal fun PeerVerificationStatus(hasPeerVerified: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (hasPeerVerified) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (hasPeerVerified) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current,
        border = if (hasPeerVerified) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasPeerVerified) {
                Icon(
                    modifier = Modifier.size(12.dp),
                    painter = painterResource(R.drawable.check_circle_24px),
                    contentDescription = null,
                )
            } else {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (hasPeerVerified) "Peer has verified" else "Peer not verified yet",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
