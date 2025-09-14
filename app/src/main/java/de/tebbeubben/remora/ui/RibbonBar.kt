package de.tebbeubben.remora.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R
import de.tebbeubben.remora.lib.model.RemoraStatusData
import de.tebbeubben.remora.util.formatBG
import de.tebbeubben.remora.util.toMinimalLocalizedString
import kotlin.time.Instant

@Composable
fun RibbonBar(
    modifier: Modifier = Modifier,
    statusData: RemoraStatusData,
    currentTime: Instant
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val profilePercentage = if (statusData.short.activeProfilePercentage == 100) null else "${statusData.short.activeProfilePercentage}%"
        val profileShift = when {
            statusData.short.activeProfileShift == 0 -> null
            statusData.short.activeProfileShift > 0  -> "+${statusData.short.activeProfileShift}h"
            else                                     -> "${statusData.short.activeProfileShift}h"
        }
        val profileDetails = when {
            profilePercentage != null && profileShift != null -> "$profilePercentage $profileShift"
            profilePercentage != null                         -> profilePercentage
            profileShift != null                              -> profileShift
            else                                              -> null
        }

        val profileRemainingDuration = statusData.short.activeProfileDuration?.let { duration ->
            val end = statusData.short.activeProfileStart + duration
            val remainingDuration = end - currentTime
            remainingDuration.toMinimalLocalizedString()
        }

        RibbonItem(
            modifier = Modifier.weight(1f),
            icon = painterResource(R.drawable.kid_star_24px),
            description = "Active Profile",
            text = statusData.short.activeProfile,
            activeText = when {
                profileDetails != null && profileRemainingDuration != null -> "$profileDetails, $profileRemainingDuration"
                profileRemainingDuration != null                           -> profileRemainingDuration
                profileDetails != null                                     -> profileDetails
                else                                                       -> null
            }
        )

        val targetStart = statusData.short.tempTargetStart
        val targetDuration = statusData.short.tempTargetDuration

        val targetRemainingDuration = if (targetStart != null && targetDuration != null) {
            val end = targetStart + targetDuration
            val remainingDuration = end - currentTime
            remainingDuration.toMinimalLocalizedString()
        } else null

        RibbonItem(
            modifier = Modifier.weight(1f),
            icon = painterResource(R.drawable.recenter_24px),
            description = "Current Target",
            text = statusData.short.target.formatBG(statusData.short.usesMgdl),
            activeText = targetRemainingDuration
        )
    }
}