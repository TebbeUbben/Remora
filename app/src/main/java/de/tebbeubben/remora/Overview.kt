package de.tebbeubben.remora

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.model.RemoraStatusData
import de.tebbeubben.remora.ui.BasalRateClassification
import de.tebbeubben.remora.ui.BgClassification
import de.tebbeubben.remora.ui.BoxWithTimeAxis
import de.tebbeubben.remora.ui.GlucoseStatus
import de.tebbeubben.remora.ui.RibbonItem
import de.tebbeubben.remora.ui.StatusLight
import de.tebbeubben.remora.ui.TherapyIndicators
import de.tebbeubben.remora.ui.ViewableTimeWindowState
import de.tebbeubben.remora.ui.theme.LocalExtendedColors
import de.tebbeubben.remora.util.formatBG
import de.tebbeubben.remora.util.formatCarbs
import de.tebbeubben.remora.util.formatDaysAndHours
import de.tebbeubben.remora.util.formatInsulin
import de.tebbeubben.remora.util.toMinimalLocalizedString
import de.tebbeubben.remora.util.toRelativeString
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Composable
fun Overview(
    currentTime: Instant,
    statusData: RemoraStatusData,
) {
    Scaffold(
        bottomBar = {
            FlexibleBottomAppBar(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {

                IconButton(
                    onClick = {},
                    colors = IconButtonDefaults.iconButtonColors().copy(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.settings_24px),
                        contentDescription = "Settings"
                    )
                }

                FilledTonalIconButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.grain_24px),
                        contentDescription = "Enter Carbs"
                    )
                }

                FilledTonalIconButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.syringe_24px),
                        contentDescription = "Deliver Bolus"
                    )
                }

                FilledTonalIconButton(
                    onClick = {},
                ) {
                    Icon(
                        painter = painterResource(R.drawable.calculate_24px),
                        contentDescription = "Bolus Calculator"
                    )
                }

                IconButton(
                    onClick = {},
                    colors = IconButtonDefaults.iconButtonColors().copy(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert_24px),
                        contentDescription = "More"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            statusData.short.displayBg?.let { displayBg ->
                Spacer(modifier = Modifier.height(16.dp))

                val bg = (displayBg.smoothedValue ?: displayBg.value)

                val deltas = displayBg.deltas?.let { deltas ->
                    Triple(
                        first = (if (deltas.delta >= 0) "+" else "") + deltas.delta.formatBG(statusData.short.usesMgdl),
                        second = (if (deltas.shortAverageDelta >= 0) "+" else "") + deltas.shortAverageDelta.formatBG(statusData.short.usesMgdl),
                        third = (if (deltas.longAverageDelta >= 0) "+" else "") + deltas.longAverageDelta.formatBG(statusData.short.usesMgdl)
                    )
                }

                val runningModeRemainingDuration = statusData.short.runningModeDuration?.let { duration ->
                    val end = statusData.short.runningModeStart + duration
                    val remainingDuration = end - currentTime
                    remainingDuration.toMinimalLocalizedString()
                }

                GlucoseStatus(
                    modifier = Modifier.fillMaxWidth(),
                    value = bg.formatBG(statusData.short.usesMgdl),
                    isStale = (currentTime - displayBg.timestamp) >= 9.minutes,
                    bgClassification = when {
                        bg > statusData.short.highBgThreshold -> BgClassification.ABOVE_RANGE
                        bg < statusData.short.lowBgThreshold  -> BgClassification.BELOW_RANGE
                        else                                  -> BgClassification.IN_RANGE
                    },
                    trendArrow = displayBg.trendArrow,
                    glucoseAge = (displayBg.timestamp - currentTime).toRelativeString(),
                    delta = deltas?.first,
                    shortAverageDelta = deltas?.second,
                    longAverageDelta = deltas?.third,
                    runningMode = statusData.short.runningMode,
                    remainingDuration = runningModeRemainingDuration
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
            ) {
                var carbsText = statusData.short.displayCob?.formatCarbs()?.plus(" g") ?: "n/a"
                if (statusData.short.futureCarbs.roundToInt() > 0) {
                    carbsText += " (${statusData.short.futureCarbs.roundToInt()})"
                }

                val tempBasal = statusData.short.tempBasalAbsolute
                val baseBasal = statusData.short.baseBasal

                TherapyIndicators(
                    modifier = Modifier.fillMaxWidth(),
                    iob = (statusData.short.bolusIob + statusData.short.basalIob).formatInsulin() + " U",
                    cob = carbsText,
                    basalRate = (tempBasal ?: baseBasal).formatInsulin() + " U/h",
                    basalRateClassification = when {
                        tempBasal == null     -> BasalRateClassification.NEUTRAL
                        tempBasal > baseBasal -> BasalRateClassification.HIGH
                        else                  -> BasalRateClassification.LOW
                    },
                    autosensRatio = (statusData.short.autosensRatio * 100).roundToInt().toString() + "%"
                )

                Spacer(modifier = Modifier.height(1.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large.copy(topStart = CornerSize(0), topEnd = CornerSize(0))),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    StatusLight(
                        icon = painterResource(if (statusData.short.isCharging) R.drawable.mobile_charge_24px else R.drawable.mobile_24px),
                        description = "Phone Battery",
                        texts = listOf(statusData.short.deviceBattery.toString() + "%").map { it to Color.Unspecified }
                    )

                    statusData.short.cannulaChangedAt?.let { cannulaChangedAt ->
                        StatusLight(
                            icon = painterResource(R.drawable.cannula),
                            description = "Cannula",
                            texts = listOf((currentTime - cannulaChangedAt).formatDaysAndHours()).map { it to Color.Unspecified }
                        )
                    }

                    val reservoirText = statusData.short.reservoirLevel?.let { reservoirLevel ->
                        var text = reservoirLevel.roundToInt().toString()
                        if (statusData.short.isReservoirLevelMax) text += "+"
                        text += "U"
                        text
                    }

                    val podChangedAt = statusData.short.podChangedAt
                    if (podChangedAt != null) {
                        val podValues = mutableListOf<String>()
                        if (reservoirText != null) podValues += reservoirText
                        podValues +=
                            (currentTime - podChangedAt).formatDaysAndHours()

                        if (podValues.isNotEmpty()) {
                            StatusLight(
                                icon = painterResource(R.drawable.pod),
                                description = "Reservoir",
                                texts = podValues.map { it to Color.Unspecified }
                            )
                        }
                    } else {

                        val reservoirValues = mutableListOf<String>()
                        if (reservoirText != null) reservoirValues += reservoirText

                        statusData.short.insulinChangedAt?.let { insulinChangedAt ->
                            reservoirValues += (currentTime - insulinChangedAt).formatDaysAndHours()
                        }

                        if (reservoirValues.isNotEmpty()) {
                            StatusLight(
                                icon = painterResource(R.drawable.reservoir),
                                description = "Reservoir",
                                texts = reservoirValues.map { it to Color.Unspecified }
                            )
                        }
                    }

                    val pumpBatteryValues = mutableListOf<String>()

                    statusData.short.batteryLevel?.let { batteryLevel ->
                        pumpBatteryValues += "$batteryLevel%"
                    }

                    statusData.short.batteryChangedAt?.let { batteryChangedAt ->
                        pumpBatteryValues += (currentTime - batteryChangedAt).formatDaysAndHours()
                    }

                    if (pumpBatteryValues.isNotEmpty()) {
                        StatusLight(
                            icon = painterResource(R.drawable.battery_android_full_24px),
                            description = "Pump Battery",
                            texts = pumpBatteryValues.map { it to Color.Unspecified }
                        )
                    }

                    statusData.short.sensorChangedAt?.let { sensorChangedAt ->
                        StatusLight(
                            icon = painterResource(R.drawable.sensors_24px),
                            description = "Sensor",
                            texts = listOf((currentTime - sensorChangedAt).formatDaysAndHours()).map { it to Color.Unspecified }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.large
            ) {
                BoxWithTimeAxis(
                    start = currentTime - 24.hours,
                    end = currentTime + 3.hours,
                    minWindowWidth = 1.hours,
                    viewableTimeWindowState = remember {
                        ViewableTimeWindowState(
                            initialWindowStart = currentTime - 3.hours,
                            initialWindowWidth = 6.hours
                        )
                    }
                ) {

                    val timezone = TimeZone.currentSystemDefault()
                    val fullHours = remember(windowStart, windowWidth) {
                        val startDateTime = (windowStart - windowWidth).toLocalDateTime(timezone)
                        val withoutFractionalHours = LocalDateTime(
                            year = startDateTime.year,
                            month = startDateTime.month,
                            day = startDateTime.day,
                            hour = startDateTime.hour,
                            minute = 0,
                            second = 0,
                            nanosecond = 0
                        )
                        val startInstant = withoutFractionalHours.toInstant(timezone)
                        generateSequence(startInstant) { it + 1.hours }.takeWhile { it <= (windowStart + windowWidth * 2) }.toList()
                    }

                    val guidelineColor = LocalContentColor.current.copy(alpha = 0.05f)

                    Canvas(Modifier.fillMaxSize()) {
                        for (hour in fullHours) {
                            val x = getHorizontalPosition(hour)
                            drawLine(
                                color = guidelineColor,
                                strokeWidth = 1.dp.toPx(),
                                start = Offset(x - 1.dp.toPx() / 2, 16.dp.toPx()),
                                end = Offset(x - 1.dp.toPx() / 2, size.height - 32.dp.toPx()),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var bgGraphSize by remember { mutableStateOf(IntSize(0, 0)) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clipToBounds()
                                .weight(2f)
                                .onSizeChanged { bgGraphSize = it },
                        ) {
                            val maxBg = maxOf(statusData.bucketedData.mapNotNull { it.bgData?.value }.maxOrNull() ?: 100f, statusData.predictions.maxOfOrNull { it.value } ?: 100f) + 20

                            for (legend in listOf(50, 100, 150, 200, 250, 300, 350)) {
                                var textHeight by remember { mutableIntStateOf(0) }
                                Text(
                                    modifier = Modifier
                                        .offset { IntOffset(16.dp.roundToPx(), (bgGraphSize.height - bgGraphSize.height / maxBg * legend - textHeight / 2.0).roundToInt()) },
                                    text = legend.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    onTextLayout = { textHeight = it.size.height }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(1.0f / maxBg * (statusData.short.highBgThreshold - statusData.short.lowBgThreshold))
                                    .fillMaxWidth()
                                    .offset { IntOffset(0, (bgGraphSize.height - bgGraphSize.height / maxBg * statusData.short.highBgThreshold).roundToInt()) }
                                    .background(Color(0x4000FF00))
                            )

                            val context = LocalContext.current

                            for (bucket in statusData.bucketedData.filter { it.bgData != null }) {
                                val bgData = bucket.bgData!!
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .offset { IntOffset((this@BoxWithTimeAxis.getHorizontalPosition(bucket.timestamp) - 2.dp.toPx()).roundToInt(), (bgGraphSize.height - bgGraphSize.height / maxBg * bgData.value - 2.dp.toPx()).roundToInt()) }
                                        /*.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = false, radius = 8.dp)
                                        ) {
                                            Toast.makeText(context, bgData.value.formatBG(statusData.short.usesMgdl), Toast.LENGTH_SHORT).show()
                                        }*/
                                        .clip(RoundedCornerShape(50))
                                        .alpha(if (bgData.filledGap) 0.5f else 1f)
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )
                            }

                            val cobColor = LocalExtendedColors.current.carbs.color
                            val iobColor = LocalExtendedColors.current.bolus.color
                            val uamColor = Color(red = cobColor.red + 0.1f, green = cobColor.green + 0.1f, blue = cobColor.blue + 0.1f)
                            val ztColor = Color(red = iobColor.red + 0.1f, green = iobColor.green + 0.1f, blue = iobColor.blue + 0.1f)
                            val aCobColor = Color(red = cobColor.red - 0.1f, green = cobColor.green - 0.1f, blue = cobColor.blue - 0.1f)

                            for (prediction in statusData.predictions) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .offset {
                                            IntOffset(
                                                (this@BoxWithTimeAxis.getHorizontalPosition(prediction.timestamp) - 2.dp.toPx()).roundToInt(),
                                                (bgGraphSize.height - bgGraphSize.height / maxBg * prediction.value - 2.dp.toPx()).roundToInt()
                                            )
                                        }
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            when (prediction.type) {
                                                RemoraStatusData.PredictionType.IOB   -> iobColor
                                                RemoraStatusData.PredictionType.COB   -> cobColor
                                                RemoraStatusData.PredictionType.A_COB -> aCobColor
                                                RemoraStatusData.PredictionType.UAM   -> uamColor
                                                RemoraStatusData.PredictionType.ZT    -> ztColor
                                            }
                                        )
                                )
                            }
                        }
                        var iobCobGraphSize by remember { mutableStateOf(IntSize(0, 0)) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .onSizeChanged { iobCobGraphSize = it }
                        ) {
                            val iobValues = statusData.bucketedData.map { it.timestamp to (it.insulinData?.iob ?: 0f) }
                            val cobValues = statusData.bucketedData.map { Triple(it.timestamp, it.autosensData?.cob ?: 0f, it.autosensData?.carbsFromBolus ?: 0f) }

                            val maxIobValue = iobValues.maxOfOrNull { it.second } ?: 1.0f
                            val minIobValue = iobValues.minOfOrNull { it.second } ?: -1.0f
                            val maxIobRange = maxOf(1, ceil(maxOf(abs(maxIobValue), abs(minIobValue))).roundToInt())

                            val maxCobValue = cobValues.maxOfOrNull { it.second } ?: 10.0f
                            val maxCobRange = maxOf(10, ceil(maxCobValue / 10f).roundToInt() * 10)

                            val baselineColor = MaterialTheme.colorScheme.onSurfaceVariant

                            val iobLineColor = LocalExtendedColors.current.bolus.color
                            val iobFillColor = iobLineColor.copy(alpha = 0.3f)

                            val cobLineColor = LocalExtendedColors.current.carbs.color
                            val cobFillColor = cobLineColor.copy(alpha = 0.3f)

                            val getYForIob = { iob: Float -> iobCobGraphSize.height * (0.5f - 0.5f / maxIobRange * iob) }

                            val getYForCob = { cob: Float -> iobCobGraphSize.height * (0.5f - 0.5f / maxCobRange * cob) }

                            for (legend in listOf(-maxIobRange + 1, 0, maxIobRange - 1)) {
                                var textHeight by remember { mutableIntStateOf(1) }
                                //TODO: Proper NaN handling for text sizes
                                Text(
                                    modifier = Modifier
                                        .offset { IntOffset(16.dp.roundToPx(), (getYForIob(legend.toFloat()) - textHeight / 2).roundToInt()) },
                                    text = legend.toString(),
                                    color = LocalExtendedColors.current.bolus.color,
                                    style = MaterialTheme.typography.labelSmall,
                                    onTextLayout = { textHeight = it.size.height }
                                )
                            }

                            for (legend in listOf(-maxCobRange + 10, 0, maxCobRange - 10).distinct()) {
                                var textSize by remember { mutableStateOf(IntSize(1, 1)) }
                                Text(
                                    modifier = Modifier
                                        .offset { IntOffset(iobCobGraphSize.width - 16.dp.roundToPx() - textSize.width, (getYForCob(legend.toFloat()) - textSize.height / 2f).roundToInt()) },
                                    text = legend.toString(),
                                    color = LocalExtendedColors.current.carbs.color,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Right,
                                    onTextLayout = { textSize = it.size }
                                )
                            }

                            Canvas(
                                Modifier
                                    .fillMaxSize()
                                    .clipToBounds()
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val zeroY = canvasHeight / 2f

                                drawLine(
                                    color = baselineColor,
                                    strokeWidth = 1.dp.toPx(),
                                    start = Offset(0f, zeroY - 1.dp.toPx() / 2),
                                    end = Offset(canvasWidth, zeroY - 1.dp.toPx() / 2)
                                )

                                val linePath = Path()
                                val fillPath = Path()

                                var drawingActiveSegment = false
                                var previousY: Float

                                if (iobValues.isNotEmpty()) {
                                    val firstTimestamp = iobValues.first().first
                                    val firstIob = iobValues.first().second
                                    val firstX = this@BoxWithTimeAxis.getHorizontalPosition(firstTimestamp)
                                    val firstY = getYForIob(firstIob)

                                    if (firstIob != 0f) {
                                        linePath.moveTo(firstX, zeroY)
                                        linePath.lineTo(firstX, firstY)

                                        fillPath.moveTo(firstX, zeroY)
                                        fillPath.lineTo(firstX, firstY)

                                        drawingActiveSegment = true
                                    }

                                    previousY = firstY

                                    for (i in 1 until iobValues.size) {
                                        val currentTimestamp = iobValues[i].first
                                        val prevIob = iobValues[i - 1].second
                                        val currentIob = iobValues[i].second
                                        val currentX = this@BoxWithTimeAxis.getHorizontalPosition(currentTimestamp)
                                        val currentY = getYForIob(currentIob)

                                        if (currentIob != 0f) {
                                            if (!drawingActiveSegment) {
                                                linePath.moveTo(currentX, zeroY)
                                                linePath.lineTo(currentX, currentY)

                                                fillPath.moveTo(currentX, zeroY)
                                                fillPath.lineTo(currentX, currentY)

                                                drawingActiveSegment = true
                                            } else {
                                                if (abs(prevIob - currentIob) > 0.2) {
                                                    linePath.lineTo(currentX, previousY)
                                                    fillPath.lineTo(currentX, previousY)
                                                }
                                                linePath.lineTo(currentX, currentY)
                                                fillPath.lineTo(currentX, currentY)
                                            }
                                        } else {
                                            if (drawingActiveSegment) {
                                                linePath.lineTo(currentX, previousY)
                                                linePath.lineTo(currentX, zeroY)

                                                fillPath.lineTo(currentX, previousY)
                                                fillPath.lineTo(currentX, zeroY)

                                                drawingActiveSegment = false
                                            }
                                        }
                                        previousY = currentY
                                    }

                                    if (drawingActiveSegment) {
                                        val lastX = this@BoxWithTimeAxis.getHorizontalPosition(iobValues.last().first)
                                        linePath.lineTo(lastX, zeroY)
                                        fillPath.lineTo(lastX, zeroY)
                                    }

                                }

                                drawPath(
                                    path = fillPath,
                                    color = iobFillColor
                                )

                                drawPath(
                                    path = linePath,
                                    color = iobLineColor,
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                drawingActiveSegment = false
                                linePath.reset()
                                fillPath.reset()

                                if (cobValues.isNotEmpty()) {
                                    val firstTimestamp = cobValues.first().first
                                    val firstCob = cobValues.first().second
                                    val firstX = this@BoxWithTimeAxis.getHorizontalPosition(firstTimestamp)
                                    val firstY = getYForIob(firstCob)

                                    if (firstCob != 0f) {
                                        linePath.moveTo(firstX, zeroY)
                                        linePath.lineTo(firstX, firstY)

                                        fillPath.moveTo(firstX, zeroY)
                                        fillPath.lineTo(firstX, firstY)

                                        drawingActiveSegment = true
                                    }

                                    previousY = firstY

                                    for (i in 1 until cobValues.size) {
                                        val currentTimestamp = cobValues[i].first
                                        val currentCob = cobValues[i].second
                                        val currentCarbsFromBolus = cobValues[i].third
                                        val currentX = this@BoxWithTimeAxis.getHorizontalPosition(currentTimestamp)
                                        val currentY = getYForCob(currentCob)

                                        if (currentCob != 0f) {
                                            if (!drawingActiveSegment) {
                                                linePath.moveTo(currentX, zeroY)
                                                linePath.lineTo(currentX, currentY)

                                                fillPath.moveTo(currentX, zeroY)
                                                fillPath.lineTo(currentX, currentY)

                                                drawingActiveSegment = true
                                            } else {
                                                if (currentCarbsFromBolus != 0f) {
                                                    linePath.lineTo(currentX, previousY)
                                                    fillPath.lineTo(currentX, previousY)
                                                }
                                                linePath.lineTo(currentX, currentY)
                                                fillPath.lineTo(currentX, currentY)
                                            }
                                        } else {
                                            if (drawingActiveSegment) {
                                                linePath.lineTo(currentX, previousY)
                                                linePath.lineTo(currentX, zeroY)

                                                fillPath.lineTo(currentX, previousY)
                                                fillPath.lineTo(currentX, zeroY)

                                                drawingActiveSegment = false
                                            }
                                        }
                                        previousY = currentY
                                    }

                                    if (drawingActiveSegment) {
                                        val lastX = this@BoxWithTimeAxis.getHorizontalPosition(iobValues.last().first)
                                        linePath.lineTo(lastX, zeroY)
                                        fillPath.lineTo(lastX, zeroY)
                                    }

                                    drawPath(
                                        path = fillPath,
                                        color = cobFillColor
                                    )

                                    drawPath(
                                        path = linePath,
                                        color = cobLineColor,
                                        style = Stroke(width = 1.dp.toPx())
                                    )

                                }
                            }

                        }
                        var devGraphSize by remember { mutableStateOf(IntSize(0, 0)) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .onSizeChanged { devGraphSize = it }
                        ) {
                            val deviations = statusData.bucketedData.map { Triple(it.timestamp, it.autosensData?.deviation ?: 0f, it.autosensData?.type ?: RemoraStatusData.AutosensType.NEUTRAL) }

                            val maxDevValue = deviations.maxOfOrNull { it.second } ?: 10.0f
                            val minDevValue = deviations.minOfOrNull { it.second } ?: -10.0f
                            val maxDevRange = max(10, ceil(maxOf(abs(maxDevValue), abs(minDevValue)) / 10.0f).roundToInt() * 10)

                            val baselineColor = MaterialTheme.colorScheme.onSurfaceVariant

                            val getYForDev = { dev: Float -> iobCobGraphSize.height * (0.5f - 0.5f / maxDevRange * dev) }

                            val devWidth = (5.minutes * devGraphSize.width.toDouble() / this@BoxWithTimeAxis.windowWidth - with(LocalDensity.current) { 1.dp.toPx() }).toFloat()

                            val posColor = Color(0x8000FF00)
                            val negColor = Color(0x80FF0000)
                            val neutralColor = MaterialTheme.colorScheme.onSurfaceVariant
                            val uamColor = LocalExtendedColors.current.carbs.color

                            for (legend in listOf(-maxDevRange + 10, 0, maxDevRange - 10)) {
                                var textHeight by remember { mutableIntStateOf(0) }
                                Text(
                                    modifier = Modifier
                                        .offset { IntOffset(16.dp.roundToPx(), (getYForDev(legend.toFloat()) - textHeight / 2).roundToInt()) },
                                    text = legend.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    onTextLayout = { textHeight = it.size.height }
                                )
                            }

                            Canvas(
                                Modifier
                                    .fillMaxSize()
                                    .clipToBounds()
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val zeroY = canvasHeight / 2f

                                drawLine(
                                    color = baselineColor,
                                    strokeWidth = 1.dp.toPx(),
                                    start = Offset(0f, zeroY - 1.dp.toPx() / 2),
                                    end = Offset(canvasWidth, zeroY - 1.dp.toPx() / 2)
                                )

                                for (deviation in deviations) {
                                    if (deviation.second == 0f) continue
                                    val rectLeft = this@BoxWithTimeAxis.getHorizontalPosition(deviation.first) - devWidth
                                    val barHeight = canvasHeight * 0.5f / maxDevRange * abs(deviation.second) // Renamed for clarity
                                    val rectTopActual = if (deviation.second < 0) zeroY else zeroY - barHeight
                                    val rectRight = rectLeft + devWidth
                                    val rectBottomActual = if (deviation.second < 0) zeroY + barHeight else zeroY

                                    // Apply clipping for this specific bar
                                    clipRect(
                                        left = rectLeft,
                                        top = rectTopActual,
                                        right = rectRight,
                                        bottom = rectBottomActual
                                    ) {
                                        val radius = devWidth / 2
                                        val pathRectTop = if (deviation.second < 0) zeroY else zeroY - barHeight
                                        val pathRectBottom = if (deviation.second < 0) zeroY + barHeight else zeroY

                                        val barPath = Path().apply {
                                            if (deviation.second > 0) {
                                                moveTo(rectLeft + radius, pathRectTop)
                                                lineTo(rectRight - radius, pathRectTop)
                                                arcTo(
                                                    rect = Rect(rectRight - 2 * radius, pathRectTop, rectRight, pathRectTop + 2 * radius),
                                                    startAngleDegrees = -90f,
                                                    sweepAngleDegrees = 90f,
                                                    forceMoveTo = false
                                                )
                                                lineTo(rectRight, pathRectBottom)
                                                lineTo(rectLeft, pathRectBottom)
                                                arcTo(
                                                    rect = Rect(rectLeft, pathRectTop, rectLeft + 2 * radius, pathRectTop + 2 * radius),
                                                    startAngleDegrees = 180f,
                                                    sweepAngleDegrees = 90f,
                                                    forceMoveTo = false
                                                )
                                            } else if (deviation.second < 0) {
                                                moveTo(rectLeft, pathRectTop)
                                                lineTo(rectRight, pathRectTop)
                                                lineTo(rectRight, pathRectBottom - radius)
                                                arcTo(
                                                    rect = Rect(rectRight - 2 * radius, pathRectBottom - 2 * radius, rectRight, pathRectBottom),
                                                    startAngleDegrees = 0f,
                                                    sweepAngleDegrees = 90f,
                                                    forceMoveTo = false
                                                )
                                                lineTo(rectLeft + radius, pathRectBottom)
                                                arcTo(
                                                    rect = Rect(rectLeft, pathRectBottom - 2 * radius, rectLeft + 2 * radius, pathRectBottom),
                                                    startAngleDegrees = 90f,
                                                    sweepAngleDegrees = 90f,
                                                    forceMoveTo = false
                                                )
                                            }
                                            close()
                                        }

                                        drawPath(
                                            path = barPath,
                                            color = when (deviation.third) {
                                                RemoraStatusData.AutosensType.POSITIVE -> posColor
                                                RemoraStatusData.AutosensType.NEGATIVE -> negColor
                                                RemoraStatusData.AutosensType.UAM      -> uamColor
                                                else                                   -> neutralColor
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant

                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .offset { IntOffset(getHorizontalPosition(currentTime).roundToInt(), 0) }
                    ) {
                        drawLine(
                            color = lineColor,
                            strokeWidth = 1.dp.toPx(),
                            start = Offset(1.dp.toPx() / 2, 16.dp.toPx()),
                            end = Offset(1.dp.toPx() / 2, size.height - 32.dp.toPx()),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
                        )
                    }
                    for (hour in fullHours) {
                        val hourDateTime = hour.toLocalDateTime(timezone)
                        val hourText = hourDateTime.hour.toString()
                        Text(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .width(100.dp)
                                .offset { IntOffset((getHorizontalPosition(hour) - 50.dp.toPx()).roundToInt(), -8.dp.toPx().roundToInt()) },
                            text = hourText,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            val updated = (statusData.short.timestamp - currentTime).toRelativeString()
            Text("Last updated: $updated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

    }
}