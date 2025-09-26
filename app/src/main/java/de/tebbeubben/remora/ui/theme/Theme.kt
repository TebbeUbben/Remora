package de.tebbeubben.remora.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color,
)

@Immutable
data class ExtendedColorScheme(
    val bolus: ColorFamily,
    val carbs: ColorFamily,
    val basal: Color,
    val autosens: Color,

    val insulinActivity: Color,

    val statusLightWarning: Color,
    val statusLightCritical: Color,

    val negativeDeviation: Color,
    val positiveDeviation: Color,
    val uamDeviation: Color,

    val iobPredictionColor: Color,
    val cobPredictionColor: Color,
    val aCobPredictionColor: Color,
    val uamPredictionColor: Color,
    val ztPredictionColor: Color,
)

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val extendedLight = ExtendedColorScheme(
    bolus = ColorFamily(
        bolusLight,
        onBolusLight,
        bolusContainerLight,
        onBolusContainerLight,
    ),
    carbs = ColorFamily(
        carbsLight,
        onCarbsLight,
        carbsContainerLight,
        onCarbsContainerLight,
    ),
    basal = basalLight,
    autosens = autosensLight,
    insulinActivity = insulinActivityLight,
    statusLightWarning = statusLightWarningLight,
    statusLightCritical = statusLightCriticalLight,
    negativeDeviation = negativeDeviationLight,
    positiveDeviation = positiveDeviationLight,
    uamDeviation = uamDeviationLight,
    iobPredictionColor = iobPredictionLight,
    cobPredictionColor = cobPredictionLight,
    aCobPredictionColor = aCobPredictionLight,
    uamPredictionColor = uamPredictionLight,
    ztPredictionColor = ztPredictionLight
)

val extendedDark = ExtendedColorScheme(
    bolus = ColorFamily(
        bolusDark,
        onBolusDark,
        bolusContainerDark,
        onBolusContainerDark,
    ),
    carbs = ColorFamily(
        carbsDark,
        onCarbsDark,
        carbsContainerDark,
        onCarbsContainerDark,
    ),
    basal = basalDark,
    autosens = autosensDark,
    insulinActivity = insulinActivityDark,
    statusLightWarning = statusLightWarningDark,
    statusLightCritical = statusLightCriticalDark,
    negativeDeviation = negativeDeviationDark,
    positiveDeviation = positiveDeviationDark,
    uamDeviation = uamDeviationDark,
    iobPredictionColor = iobPredictionDark,
    cobPredictionColor = cobPredictionDark,
    aCobPredictionColor = aCobPredictionDark,
    uamPredictionColor = uamPredictionDark,
    ztPredictionColor = ztPredictionDark
)

val LocalExtendedColors = staticCompositionLocalOf { extendedLight }

@Composable
fun RemoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme                                                      -> darkScheme
        else                                                           -> lightScheme
    }

    val extendedColors = if (darkTheme) extendedDark else extendedLight

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

