package de.tebbeubben.remora.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R
import de.tebbeubben.remora.lib.model.status.RemoraStatusData
import de.tebbeubben.remora.util.formatBG
import de.tebbeubben.remora.util.toMinimalLocalizedString
import kotlin.time.Instant

@Composable
fun RibbonBar(
    modifier: Modifier = Modifier,
    currentTime: Instant,
    activeProfile: RemoraStatusData.ActiveProfile,
    currentTarget: RemoraStatusData.CurrentTarget,
    usesMgdl: Boolean,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val profilePercentage = if (activeProfile.percentage == 100) null else "${activeProfile.percentage}%"
        val profileShift = when {
            activeProfile.timeShift == 0 -> null
            activeProfile.timeShift > 0  -> "+${activeProfile.timeShift}h"
            else                                     -> "${activeProfile.timeShift}h"
        }
        val profileDetails = when {
            profilePercentage != null && profileShift != null -> "$profilePercentage $profileShift"
            profilePercentage != null                         -> profilePercentage
            profileShift != null                              -> profileShift
            else                                              -> null
        }

        val profileRemainingDuration = activeProfile.duration?.let { duration ->
            val end = activeProfile.start + duration
            val remainingDuration = end - currentTime
            remainingDuration.toMinimalLocalizedString()
        }

        RibbonItem(
            modifier = Modifier.weight(1f),
            icon = painterResource(R.drawable.kid_star_24px),
            description = stringResource(R.string.active_profile),
            text = activeProfile.name,
            activeText = when {
                profileDetails != null && profileRemainingDuration != null -> "$profileDetails, $profileRemainingDuration"
                profileRemainingDuration != null                           -> profileRemainingDuration
                profileDetails != null                                     -> profileDetails
                else                                                       -> null
            }
        )

        val targetStart = currentTarget.tempTargetStart
        val targetDuration = currentTarget.tempTargetDuration

        val targetRemainingDuration = if (targetStart != null && targetDuration != null) {
            val end = targetStart + targetDuration
            val remainingDuration = end - currentTime
            remainingDuration.toMinimalLocalizedString()
        } else null

        RibbonItem(
            modifier = Modifier.weight(1f),
            icon = painterResource(R.drawable.recenter_24px),
            description = stringResource(R.string.current_target),
            text = currentTarget.target.formatBG(usesMgdl),
            activeText = targetRemainingDuration
        )
    }
}