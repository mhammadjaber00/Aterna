package io.yavero.pocketadhd.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ADHD-friendly typography system
 * 
 * Design principles:
 * - Larger base sizes for better readability
 * - Clear font weight distinctions
 * - Generous line heights for reduced visual stress
 * - Support for dynamic text scaling
 * - High contrast ratios
 */
object AdhdTypography {
    
    // Base font sizes - larger than typical for ADHD-friendly design
    private val DisplayLarge = 57.sp
    private val DisplayMedium = 45.sp
    private val DisplaySmall = 36.sp
    
    private val HeadlineLarge = 32.sp
    private val HeadlineMedium = 28.sp
    private val HeadlineSmall = 24.sp
    
    private val TitleLarge = 22.sp
    private val TitleMedium = 18.sp  // Increased from 16sp
    private val TitleSmall = 16.sp   // Increased from 14sp
    
    private val BodyLarge = 18.sp    // Increased from 16sp
    private val BodyMedium = 16.sp   // Increased from 14sp
    private val BodySmall = 14.sp    // Increased from 12sp
    
    private val LabelLarge = 16.sp   // Increased from 14sp
    private val LabelMedium = 14.sp  // Increased from 12sp
    private val LabelSmall = 12.sp   // Increased from 11sp
    
    // Line heights - generous for reduced visual stress
    private val DisplayLargeLineHeight = 64.sp
    private val DisplayMediumLineHeight = 52.sp
    private val DisplaySmallLineHeight = 44.sp
    
    private val HeadlineLargeLineHeight = 40.sp
    private val HeadlineMediumLineHeight = 36.sp
    private val HeadlineSmallLineHeight = 32.sp
    
    private val TitleLargeLineHeight = 28.sp
    private val TitleMediumLineHeight = 24.sp
    private val TitleSmallLineHeight = 20.sp
    
    private val BodyLargeLineHeight = 24.sp
    private val BodyMediumLineHeight = 20.sp
    private val BodySmallLineHeight = 16.sp
    
    private val LabelLargeLineHeight = 20.sp
    private val LabelMediumLineHeight = 16.sp
    private val LabelSmallLineHeight = 16.sp
    
    val Default = Typography(
        // Display styles - for large, prominent text
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = DisplayLarge,
            lineHeight = DisplayLargeLineHeight,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = DisplayMedium,
            lineHeight = DisplayMediumLineHeight,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = DisplaySmall,
            lineHeight = DisplaySmallLineHeight,
            letterSpacing = 0.sp
        ),
        
        // Headline styles - for section headers
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold, // Stronger weight for better hierarchy
            fontSize = HeadlineLarge,
            lineHeight = HeadlineLargeLineHeight,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = HeadlineMedium,
            lineHeight = HeadlineMediumLineHeight,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = HeadlineSmall,
            lineHeight = HeadlineSmallLineHeight,
            letterSpacing = 0.sp
        ),
        
        // Title styles - for card titles, dialog titles
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = TitleLarge,
            lineHeight = TitleLargeLineHeight,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = TitleMedium,
            lineHeight = TitleMediumLineHeight,
            letterSpacing = 0.1.sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = TitleSmall,
            lineHeight = TitleSmallLineHeight,
            letterSpacing = 0.1.sp
        ),
        
        // Body styles - for main content
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = BodyLarge,
            lineHeight = BodyLargeLineHeight,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = BodyMedium,
            lineHeight = BodyMediumLineHeight,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = BodySmall,
            lineHeight = BodySmallLineHeight,
            letterSpacing = 0.4.sp
        ),
        
        // Label styles - for buttons, tabs, form labels
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = LabelLarge,
            lineHeight = LabelLargeLineHeight,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = LabelMedium,
            lineHeight = LabelMediumLineHeight,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = LabelSmall,
            lineHeight = LabelSmallLineHeight,
            letterSpacing = 0.5.sp
        )
    )
    
    // Custom styles for specific ADHD-friendly use cases
    val FocusTimer = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp
    )
    
    val BigButton = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    )
    
    val StatusText = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.25.sp
    )
    
    val EmptyState = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    )
}

/**
 * Scales typography based on user's text scale preference
 * This supports accessibility requirements for dynamic text sizing
 */
fun Typography.scaled(textScale: Float): Typography {
    return Typography(
        displayLarge = displayLarge.copy(fontSize = displayLarge.fontSize * textScale),
        displayMedium = displayMedium.copy(fontSize = displayMedium.fontSize * textScale),
        displaySmall = displaySmall.copy(fontSize = displaySmall.fontSize * textScale),
        headlineLarge = headlineLarge.copy(fontSize = headlineLarge.fontSize * textScale),
        headlineMedium = headlineMedium.copy(fontSize = headlineMedium.fontSize * textScale),
        headlineSmall = headlineSmall.copy(fontSize = headlineSmall.fontSize * textScale),
        titleLarge = titleLarge.copy(fontSize = titleLarge.fontSize * textScale),
        titleMedium = titleMedium.copy(fontSize = titleMedium.fontSize * textScale),
        titleSmall = titleSmall.copy(fontSize = titleSmall.fontSize * textScale),
        bodyLarge = bodyLarge.copy(fontSize = bodyLarge.fontSize * textScale),
        bodyMedium = bodyMedium.copy(fontSize = bodyMedium.fontSize * textScale),
        bodySmall = bodySmall.copy(fontSize = bodySmall.fontSize * textScale),
        labelLarge = labelLarge.copy(fontSize = labelLarge.fontSize * textScale),
        labelMedium = labelMedium.copy(fontSize = labelMedium.fontSize * textScale),
        labelSmall = labelSmall.copy(fontSize = labelSmall.fontSize * textScale)
    )
}