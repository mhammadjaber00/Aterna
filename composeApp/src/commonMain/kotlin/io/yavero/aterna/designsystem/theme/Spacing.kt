package io.yavero.aterna.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AternaSpacing {


    val None: Dp = 0.dp
    val ExtraSmall: Dp = 4.dp
    val Small: Dp = 8.dp
    val Medium: Dp = 16.dp
    val Large: Dp = 24.dp
    val ExtraLarge: Dp = 32.dp
    val ExtraExtraLarge: Dp = 48.dp
    val Huge: Dp = 64.dp


    val SpaceXS: Dp = ExtraSmall
    val SpaceS: Dp = Small
    val SpaceM: Dp = Medium
    val SpaceL: Dp = Large
    val SpaceXL: Dp = ExtraLarge
    val SpaceXXL: Dp = ExtraExtraLarge
    val SpaceHuge: Dp = Huge


    object Button {
        val PaddingHorizontal: Dp = 24.dp
        val PaddingVertical: Dp = 16.dp
        val MinHeight: Dp = 48.dp
        val SpacingBetween: Dp = Medium
        val IconSpacing: Dp = Small
        val CornerRadius: Dp = Card.CornerRadius
    }

    object BigButton {
        val CornerRadius: Dp = 28.dp
        val PaddingHorizontal: Dp = 32.dp
        val PaddingVertical: Dp = 20.dp
        val MinHeight: Dp = 56.dp
    }

    object Pill {
        val CornerRadius: Dp = 999.dp
        val PaddingHorizontal: Dp = 16.dp
        val PaddingVertical: Dp = 8.dp
        val MinHeight: Dp = 32.dp
    }

    object Card {
        val Padding: Dp = Medium
        val Margin: Dp = Medium
        val CornerRadius: Dp = 16.dp
        val Elevation: Dp = 4.dp
    }

    object List {
        val ItemPadding: Dp = Medium
        val ItemSpacing: Dp = Small
        val SectionSpacing: Dp = Large
        val MinItemHeight: Dp = 56.dp
    }

    object Screen {
        val HorizontalPadding: Dp = 20.dp
        val VerticalPadding: Dp = Medium
        val TopPadding: Dp = Large
        val BottomPadding: Dp = Large
    }

    object Dialog {
        val Padding: Dp = Large
        val ButtonSpacing: Dp = Small
        val ContentSpacing: Dp = Medium
        val CornerRadius: Dp = 16.dp
    }

    object Input {
        val PaddingHorizontal: Dp = Medium
        val PaddingVertical: Dp = 12.dp
        val MinHeight: Dp = 48.dp
        val SpacingBetween: Dp = Medium
        val LabelSpacing: Dp = ExtraSmall
    }

    object Timer {
        val CircularPadding: Dp = ExtraLarge
        val ButtonSpacing: Dp = Large
        val StatusSpacing: Dp = Medium
        val ControlsSpacing: Dp = ExtraLarge
    }

    object Mood {
        val ScalePadding: Dp = Medium
        val ScaleItemSpacing: Dp = Large
        val ChartPadding: Dp = Medium
        val LegendSpacing: Dp = Small
    }

    object Focus {
        val SessionSpacing: Dp = Large
        val StatsSpacing: Dp = Medium
        val BreakSpacing: Dp = ExtraLarge
    }

    object Routine {
        val StepPadding: Dp = Medium
        val StepSpacing: Dp = Small
        val TimerSpacing: Dp = Large
        val CompletionSpacing: Dp = ExtraLarge
    }

    object Navigation {
        val TabHeight: Dp = 56.dp
        val TabPadding: Dp = Small
        val AppBarHeight: Dp = 64.dp
        val AppBarPadding: Dp = Medium
    }


    object TouchTarget {
        val Minimum: Dp = 48.dp
        val Comfortable: Dp = 56.dp
        val Large: Dp = 64.dp
    }


    object Motion {
        val SlideDistance: Dp = 32.dp
        val FadeOffset: Dp = 16.dp
        val ScaleOrigin: Dp = 24.dp
    }
}


val Dp.horizontal: androidx.compose.foundation.layout.PaddingValues
    get() = androidx.compose.foundation.layout.PaddingValues(horizontal = this)


val Dp.vertical: androidx.compose.foundation.layout.PaddingValues
    get() = androidx.compose.foundation.layout.PaddingValues(vertical = this)


val Dp.all: androidx.compose.foundation.layout.PaddingValues
    get() = androidx.compose.foundation.layout.PaddingValues(all = this)

object ResponsiveSpacing {
    fun small(compact: Dp = AternaSpacing.Small, expanded: Dp = AternaSpacing.Medium): Dp {


        return compact
    }

    fun medium(compact: Dp = AternaSpacing.Medium, expanded: Dp = AternaSpacing.Large): Dp {
        return compact
    }

    fun large(compact: Dp = AternaSpacing.Large, expanded: Dp = AternaSpacing.ExtraLarge): Dp {
        return compact
    }
}