package io.yavero.aterna.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AternaTypography {


    private val PixelFont = FontFamily.Monospace
    private val MedievalFont = FontFamily.Serif
    private val BodyFont = FontFamily.Default


    private val DisplayLarge = 57.sp
    private val DisplayMedium = 45.sp
    private val DisplaySmall = 36.sp
    
    private val HeadlineLarge = 32.sp
    private val HeadlineMedium = 28.sp
    private val HeadlineSmall = 24.sp
    
    private val TitleLarge = 22.sp
    private val TitleMedium = 18.sp
    private val TitleSmall = 16.sp

    private val BodyLarge = 18.sp
    private val BodyMedium = 16.sp
    private val BodySmall = 14.sp

    private val LabelLarge = 16.sp
    private val LabelMedium = 14.sp
    private val LabelSmall = 12.sp


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

        displayLarge = TextStyle(
            fontFamily = MedievalFont,
            fontWeight = FontWeight.Black, 
            fontSize = DisplayLarge,
            lineHeight = DisplayLargeLineHeight,
            letterSpacing = 0.5.sp 
        ),
        displayMedium = TextStyle(
            fontFamily = MedievalFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = DisplayMedium,
            lineHeight = DisplayMediumLineHeight,
            letterSpacing = 0.25.sp
        ),
        displaySmall = TextStyle(
            fontFamily = MedievalFont,
            fontWeight = FontWeight.Bold,
            fontSize = DisplaySmall,
            lineHeight = DisplaySmallLineHeight,
            letterSpacing = 0.25.sp
        ),


        headlineLarge = TextStyle(
            fontFamily = MedievalFont,
            fontWeight = FontWeight.ExtraBold, 
            fontSize = HeadlineLarge,
            lineHeight = HeadlineLargeLineHeight,
            letterSpacing = 0.25.sp 
        ),
        headlineMedium = TextStyle(
            fontFamily = MedievalFont,
            fontWeight = FontWeight.Bold,
            fontSize = HeadlineMedium,
            lineHeight = HeadlineMediumLineHeight,
            letterSpacing = 0.25.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = MedievalFont,
            fontWeight = FontWeight.Bold,
            fontSize = HeadlineSmall,
            lineHeight = HeadlineSmallLineHeight,
            letterSpacing = 0.15.sp
        ),


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


    val FocusTimer = TextStyle(
        fontFamily = PixelFont, 
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 2.sp 
    )
    
    val BigButton = TextStyle(
        fontFamily = MedievalFont,
        fontWeight = FontWeight.ExtraBold, 
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp 
    )
    
    val StatusText = TextStyle(
        fontFamily = PixelFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp 
    )
    
    val EmptyState = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    )


    val QuestTitle = TextStyle(
        fontFamily = MedievalFont,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.5.sp
    )

    val DungeonName = TextStyle(
        fontFamily = MedievalFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.25.sp
    )

    val LootDisplay = TextStyle(
        fontFamily = PixelFont,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.sp
    )

    val HeroStats = TextStyle(
        fontFamily = PixelFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )

    val QuestDescription = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp
    )
}

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