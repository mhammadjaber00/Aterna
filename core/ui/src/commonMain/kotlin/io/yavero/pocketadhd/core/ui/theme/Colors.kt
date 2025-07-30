package io.yavero.pocketadhd.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ADHD-friendly palette: low–medium saturation, soft neutrals, steady contrast
object AdhdColors {
    // Primary — serene teal (base of the app)
    val Primary50 = Color(0xFFF2FBFA)
    val Primary100 = Color(0xFFDCF6F3)
    val Primary200 = Color(0xFFBCECE7)
    val Primary300 = Color(0xFF8FDAD4)
    val Primary400 = Color(0xFF62C7BD)
    val Primary500 = Color(0xFF3AB7A7) // Main primary
    val Primary600 = Color(0xFF2AA89A)
    val Primary700 = Color(0xFF1C9788)
    val Primary800 = Color(0xFF117E71)
    val Primary900 = Color(0xFF0B5E55)

    // Secondary — warm apricot (sparingly for calls to action)
    val Secondary50 = Color(0xFFFFF6EF)
    val Secondary100 = Color(0xFFFFE8D8)
    val Secondary200 = Color(0xFFFFD6B8)
    val Secondary300 = Color(0xFFFFC090)
    val Secondary400 = Color(0xFFFFAA6B)
    val Secondary500 = Color(0xFFF28C3D) // Main secondary (dark enough for white text)
    val Secondary600 = Color(0xFFDE7A2E)
    val Secondary700 = Color(0xFFC66520)
    val Secondary800 = Color(0xFFA55117)
    val Secondary900 = Color(0xFF7F3C0F)

    // Success — gentle green
    val Success50 = Color(0xFFEBF6EF)
    val Success300 = Color(0xFF7BC89A)
    val Success500 = Color(0xFF4FAE7F)
    val Success700 = Color(0xFF2F7D57)

    // Warning — muted amber
    val Warning50 = Color(0xFFFFF8E6)
    val Warning500 = Color(0xFFE5B029)
    val Warning700 = Color(0xFFB08900)

    // Error — softened crimson
    val Error50 = Color(0xFFFDEEEF)
    val Error300 = Color(0xFFE48B8F)
    val Error500 = Color(0xFFD25258)
    val Error700 = Color(0xFFA93A40)

    // Neutrals — calm, slightly cool
    val Neutral0 = Color(0xFFFFFFFF)
    val Neutral50 = Color(0xFFFAFAFA)
    val Neutral100 = Color(0xFFF4F4F5)
    val Neutral200 = Color(0xFFEAEAEC)
    val Neutral300 = Color(0xFFDCDDE1)
    val Neutral400 = Color(0xFFB8BBC2)
    val Neutral500 = Color(0xFF8F95A1)
    val Neutral600 = Color(0xFF6B7280)
    val Neutral700 = Color(0xFF525559)
    val Neutral800 = Color(0xFF343A40)
    val Neutral900 = Color(0xFF1F2329)
    val Neutral950 = Color(0xFF101214)

    // Focus states — steady, not neon
    val FocusActive = Color(0xFF22C55E) // success/“go”
    val FocusPaused = Color(0xFFF4B400) // amber/“hold”
    val FocusComplete = Color(0xFF16A34A) // deeper success

    // Mood — de-saturated to avoid overstimulation
    val MoodVeryBad = Color(0xFFB42318)
    val MoodBad = Color(0xFFC2410C)
    val MoodNeutral = Color(0xFF71717A)
    val MoodGood = Color(0xFF2E7D32)
    val MoodVeryGood = Color(0xFF1B5E20)
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
    primary = AdhdColors.Primary300,          // lighter teal in dark mode
    onPrimary = AdhdColors.Neutral900,        // dark text on light chip/tonal areas
    primaryContainer = AdhdColors.Primary800, // for selected states
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