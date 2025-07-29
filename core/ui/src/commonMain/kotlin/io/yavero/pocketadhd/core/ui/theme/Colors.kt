package io.yavero.pocketadhd.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ADHD-friendly color palette - calmer tones with strong contrast
object AdhdColors {
    // Primary colors - calming blue-green
    val Primary50 = Color(0xFFE0F7FA)
    val Primary100 = Color(0xFFB2EBF2)
    val Primary200 = Color(0xFF80DEEA)
    val Primary300 = Color(0xFF4DD0E1)
    val Primary400 = Color(0xFF26C6DA)
    val Primary500 = Color(0xFF00BCD4) // Main primary
    val Primary600 = Color(0xFF00ACC1)
    val Primary700 = Color(0xFF0097A7)
    val Primary800 = Color(0xFF00838F)
    val Primary900 = Color(0xFF006064)
    
    // Secondary colors - warm orange for accents
    val Secondary50 = Color(0xFFFFF3E0)
    val Secondary100 = Color(0xFFFFE0B2)
    val Secondary200 = Color(0xFFFFCC80)
    val Secondary300 = Color(0xFFFFB74D)
    val Secondary400 = Color(0xFFFFA726)
    val Secondary500 = Color(0xFFFF9800) // Main secondary
    val Secondary600 = Color(0xFFFB8C00)
    val Secondary700 = Color(0xFFF57C00)
    val Secondary800 = Color(0xFFEF6C00)
    val Secondary900 = Color(0xFFE65100)
    
    // Success colors - gentle green
    val Success50 = Color(0xFFE8F5E8)
    val Success300 = Color(0xFF81C784)
    val Success500 = Color(0xFF4CAF50)
    val Success700 = Color(0xFF388E3C)
    
    // Warning colors - soft yellow
    val Warning50 = Color(0xFFFFFDE7)
    val Warning500 = Color(0xFFFFC107)
    val Warning700 = Color(0xFFF57F17)
    
    // Error colors - muted red
    val Error50 = Color(0xFFFFEBEE)
    val Error300 = Color(0xFFE57373)
    val Error500 = Color(0xFFF44336)
    val Error700 = Color(0xFFD32F2F)
    
    // Neutral colors - high contrast
    val Neutral0 = Color(0xFFFFFFFF)
    val Neutral50 = Color(0xFFFAFAFA)
    val Neutral100 = Color(0xFFF5F5F5)
    val Neutral200 = Color(0xFFEEEEEE)
    val Neutral300 = Color(0xFFE0E0E0)
    val Neutral400 = Color(0xFFBDBDBD)
    val Neutral500 = Color(0xFF9E9E9E)
    val Neutral600 = Color(0xFF757575)
    val Neutral700 = Color(0xFF616161)
    val Neutral800 = Color(0xFF424242)
    val Neutral900 = Color(0xFF212121)
    val Neutral950 = Color(0xFF0D0D0D)
    
    // Focus states - for timers and active elements
    val FocusActive = Color(0xFF00E676)
    val FocusPaused = Color(0xFFFFAB00)
    val FocusComplete = Color(0xFF00C853)
    
    // Mood colors - for mood tracking
    val MoodVeryBad = Color(0xFFD32F2F)
    val MoodBad = Color(0xFFFF5722)
    val MoodNeutral = Color(0xFF9E9E9E)
    val MoodGood = Color(0xFF4CAF50)
    val MoodVeryGood = Color(0xFF2E7D32)
}

val LightColorScheme = lightColorScheme(
    primary = AdhdColors.Primary500,
    onPrimary = AdhdColors.Neutral0,
    primaryContainer = AdhdColors.Primary100,
    onPrimaryContainer = AdhdColors.Primary900,
    
    secondary = AdhdColors.Secondary500,
    onSecondary = AdhdColors.Neutral0,
    secondaryContainer = AdhdColors.Secondary100,
    onSecondaryContainer = AdhdColors.Secondary900,
    
    tertiary = AdhdColors.Success500,
    onTertiary = AdhdColors.Neutral0,
    tertiaryContainer = AdhdColors.Success50,
    onTertiaryContainer = AdhdColors.Success700,
    
    error = AdhdColors.Error500,
    onError = AdhdColors.Neutral0,
    errorContainer = AdhdColors.Error50,
    onErrorContainer = AdhdColors.Error700,
    
    background = AdhdColors.Neutral50,
    onBackground = AdhdColors.Neutral900,
    surface = AdhdColors.Neutral0,
    onSurface = AdhdColors.Neutral900,
    surfaceVariant = AdhdColors.Neutral100,
    onSurfaceVariant = AdhdColors.Neutral700,
    
    outline = AdhdColors.Neutral300,
    outlineVariant = AdhdColors.Neutral200,
    scrim = AdhdColors.Neutral900.copy(alpha = 0.32f)
)

val DarkColorScheme = darkColorScheme(
    primary = AdhdColors.Primary300,
    onPrimary = AdhdColors.Neutral900,
    primaryContainer = AdhdColors.Primary800,
    onPrimaryContainer = AdhdColors.Primary100,
    
    secondary = AdhdColors.Secondary300,
    onSecondary = AdhdColors.Neutral900,
    secondaryContainer = AdhdColors.Secondary800,
    onSecondaryContainer = AdhdColors.Secondary100,
    
    tertiary = AdhdColors.Success300,
    onTertiary = AdhdColors.Neutral900,
    tertiaryContainer = AdhdColors.Success700,
    onTertiaryContainer = AdhdColors.Success50,
    
    error = AdhdColors.Error300,
    onError = AdhdColors.Neutral900,
    errorContainer = AdhdColors.Error700,
    onErrorContainer = AdhdColors.Error50,
    
    background = AdhdColors.Neutral950,
    onBackground = AdhdColors.Neutral100,
    surface = AdhdColors.Neutral900,
    onSurface = AdhdColors.Neutral100,
    surfaceVariant = AdhdColors.Neutral800,
    onSurfaceVariant = AdhdColors.Neutral300,
    
    outline = AdhdColors.Neutral600,
    outlineVariant = AdhdColors.Neutral700,
    scrim = AdhdColors.Neutral950.copy(alpha = 0.32f)
)