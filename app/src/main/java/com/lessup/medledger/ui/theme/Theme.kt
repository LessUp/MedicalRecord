package com.lessup.medledger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 医疗健康主题配色
private val MedPrimary = Color(0xFF2E7D32)           // 深绿色 - 健康、生命
private val MedOnPrimary = Color(0xFFFFFFFF)
private val MedPrimaryContainer = Color(0xFFA5D6A7)
private val MedOnPrimaryContainer = Color(0xFF1B5E20)
private val MedSecondary = Color(0xFF00838F)         // 青色 - 医疗、清洁
private val MedOnSecondary = Color(0xFFFFFFFF)
private val MedSecondaryContainer = Color(0xFFB2EBF2)
private val MedOnSecondaryContainer = Color(0xFF006064)
private val MedTertiary = Color(0xFFE65100)          // 橙色 - 提醒、重要
private val MedOnTertiary = Color(0xFFFFFFFF)
private val MedTertiaryContainer = Color(0xFFFFCC80)
private val MedOnTertiaryContainer = Color(0xFFE65100)
private val MedError = Color(0xFFB00020)
private val MedOnError = Color(0xFFFFFFFF)
private val MedErrorContainer = Color(0xFFFFDAD6)
private val MedOnErrorContainer = Color(0xFF410002)
private val MedBackground = Color(0xFFFAFDFA)
private val MedOnBackground = Color(0xFF1A1C19)
private val MedSurface = Color(0xFFFAFDFA)
private val MedOnSurface = Color(0xFF1A1C19)
private val MedSurfaceVariant = Color(0xFFE0E5DD)
private val MedOnSurfaceVariant = Color(0xFF43483F)
private val MedOutline = Color(0xFF73796E)

// 深色模式配色
private val MedPrimaryDark = Color(0xFF81C784)
private val MedOnPrimaryDark = Color(0xFF003909)
private val MedPrimaryContainerDark = Color(0xFF1B5E20)
private val MedOnPrimaryContainerDark = Color(0xFFA5D6A7)
private val MedSecondaryDark = Color(0xFF4DD0E1)
private val MedOnSecondaryDark = Color(0xFF003538)
private val MedSecondaryContainerDark = Color(0xFF006064)
private val MedOnSecondaryContainerDark = Color(0xFFB2EBF2)
private val MedTertiaryDark = Color(0xFFFFB74D)
private val MedOnTertiaryDark = Color(0xFF4E2600)
private val MedTertiaryContainerDark = Color(0xFFE65100)
private val MedOnTertiaryContainerDark = Color(0xFFFFCC80)
private val MedBackgroundDark = Color(0xFF1A1C19)
private val MedOnBackgroundDark = Color(0xFFE2E3DD)
private val MedSurfaceDark = Color(0xFF1A1C19)
private val MedOnSurfaceDark = Color(0xFFE2E3DD)
private val MedSurfaceVariantDark = Color(0xFF43483F)
private val MedOnSurfaceVariantDark = Color(0xFFC3C8BC)
private val MedOutlineDark = Color(0xFF8D9387)

private val LightColors = lightColorScheme(
    primary = MedPrimary,
    onPrimary = MedOnPrimary,
    primaryContainer = MedPrimaryContainer,
    onPrimaryContainer = MedOnPrimaryContainer,
    secondary = MedSecondary,
    onSecondary = MedOnSecondary,
    secondaryContainer = MedSecondaryContainer,
    onSecondaryContainer = MedOnSecondaryContainer,
    tertiary = MedTertiary,
    onTertiary = MedOnTertiary,
    tertiaryContainer = MedTertiaryContainer,
    onTertiaryContainer = MedOnTertiaryContainer,
    error = MedError,
    onError = MedOnError,
    errorContainer = MedErrorContainer,
    onErrorContainer = MedOnErrorContainer,
    background = MedBackground,
    onBackground = MedOnBackground,
    surface = MedSurface,
    onSurface = MedOnSurface,
    surfaceVariant = MedSurfaceVariant,
    onSurfaceVariant = MedOnSurfaceVariant,
    outline = MedOutline
)

private val DarkColors = darkColorScheme(
    primary = MedPrimaryDark,
    onPrimary = MedOnPrimaryDark,
    primaryContainer = MedPrimaryContainerDark,
    onPrimaryContainer = MedOnPrimaryContainerDark,
    secondary = MedSecondaryDark,
    onSecondary = MedOnSecondaryDark,
    secondaryContainer = MedSecondaryContainerDark,
    onSecondaryContainer = MedOnSecondaryContainerDark,
    tertiary = MedTertiaryDark,
    onTertiary = MedOnTertiaryDark,
    tertiaryContainer = MedTertiaryContainerDark,
    onTertiaryContainer = MedOnTertiaryContainerDark,
    background = MedBackgroundDark,
    onBackground = MedOnBackgroundDark,
    surface = MedSurfaceDark,
    onSurface = MedOnSurfaceDark,
    surfaceVariant = MedSurfaceVariantDark,
    onSurfaceVariant = MedOnSurfaceVariantDark,
    outline = MedOutlineDark
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
