package de.tebbeubben.remora.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

import de.tebbeubben.remora.R

@OptIn(ExperimentalTextApi::class) var quicksandFontFamily =
    FontFamily(
        listOf(100, 200, 300, 400, 500, 600, 700, 800, 900).flatMap {
            listOf(
                Font(
                    R.font.quicksand,
                    variationSettings = FontVariation.Settings(
                        FontVariation.weight(it)
                    ),
                    weight = FontWeight(it),
                ),
                Font(
                    R.font.quicksand,
                    variationSettings = FontVariation.Settings(
                        FontVariation.weight(it),
                        FontVariation.italic(1f)
                    ),
                    weight = FontWeight(it),
                    style = FontStyle.Italic,
                )
            )
        }
    )

val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = quicksandFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = quicksandFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = quicksandFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = quicksandFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = quicksandFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = quicksandFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = quicksandFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = quicksandFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = quicksandFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = quicksandFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = quicksandFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = quicksandFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = quicksandFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = quicksandFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = quicksandFontFamily),
)

