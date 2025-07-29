package io.yavero.pocketadhd.core.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ADHD-friendly spacing system
 * 
 * Design principles:
 * - Generous spacing to reduce visual clutter
 * - Large touch targets (minimum 48dp)
 * - Clear visual hierarchy through consistent spacing
 * - Breathing room between elements
 * - Predictable spacing scale
 */
object AdhdSpacing {
    
    // Base spacing scale - generous for ADHD-friendly design
    val None: Dp = 0.dp
    val ExtraSmall: Dp = 4.dp
    val Small: Dp = 8.dp
    val Medium: Dp = 16.dp
    val Large: Dp = 24.dp
    val ExtraLarge: Dp = 32.dp
    val ExtraExtraLarge: Dp = 48.dp
    val Huge: Dp = 64.dp
    
    // Semantic spacing - named by purpose
    val SpaceXS: Dp = ExtraSmall    // 4dp - tight spacing within components
    val SpaceS: Dp = Small          // 8dp - small gaps, icon padding
    val SpaceM: Dp = Medium         // 16dp - standard spacing between elements
    val SpaceL: Dp = Large          // 24dp - section spacing
    val SpaceXL: Dp = ExtraLarge    // 32dp - large section breaks
    val SpaceXXL: Dp = ExtraExtraLarge // 48dp - major section breaks
    val SpaceHuge: Dp = Huge        // 64dp - screen-level spacing
    
    // Component-specific spacing
    object Button {
        val PaddingHorizontal: Dp = 24.dp  // Generous horizontal padding
        val PaddingVertical: Dp = 16.dp    // Generous vertical padding
        val MinHeight: Dp = 48.dp          // Minimum touch target
        val SpacingBetween: Dp = Medium    // Space between buttons
        val IconSpacing: Dp = Small        // Space between icon and text
    }
    
    object Card {
        val Padding: Dp = Medium           // Internal card padding
        val Margin: Dp = Medium            // Space between cards
        val CornerRadius: Dp = 12.dp       // Rounded corners for friendliness
        val Elevation: Dp = 4.dp           // Subtle elevation
    }
    
    object List {
        val ItemPadding: Dp = Medium       // Padding within list items
        val ItemSpacing: Dp = Small        // Space between list items
        val SectionSpacing: Dp = Large     // Space between list sections
        val MinItemHeight: Dp = 56.dp      // Minimum list item height
    }
    
    object Screen {
        val HorizontalPadding: Dp = Medium // Standard screen horizontal padding
        val VerticalPadding: Dp = Medium   // Standard screen vertical padding
        val TopPadding: Dp = Large         // Top padding under app bar
        val BottomPadding: Dp = Large      // Bottom padding above nav bar
    }
    
    object Dialog {
        val Padding: Dp = Large            // Internal dialog padding
        val ButtonSpacing: Dp = Small      // Space between dialog buttons
        val ContentSpacing: Dp = Medium    // Space between dialog content elements
        val CornerRadius: Dp = 16.dp       // Rounded dialog corners
    }
    
    object Input {
        val PaddingHorizontal: Dp = Medium // Text field horizontal padding
        val PaddingVertical: Dp = 12.dp    // Text field vertical padding
        val MinHeight: Dp = 48.dp          // Minimum input height
        val SpacingBetween: Dp = Medium    // Space between form fields
        val LabelSpacing: Dp = ExtraSmall  // Space between label and input
    }
    
    object Timer {
        val CircularPadding: Dp = ExtraLarge    // Padding around circular timer
        val ButtonSpacing: Dp = Large           // Space between timer buttons
        val StatusSpacing: Dp = Medium          // Space between timer and status
        val ControlsSpacing: Dp = ExtraLarge    // Space between timer and controls
    }
    
    object Mood {
        val ScalePadding: Dp = Medium      // Padding around mood scale
        val ScaleItemSpacing: Dp = Large   // Space between mood scale items
        val ChartPadding: Dp = Medium      // Padding around mood charts
        val LegendSpacing: Dp = Small      // Space in chart legends
    }
    
    object Focus {
        val SessionSpacing: Dp = Large     // Space between focus session elements
        val StatsSpacing: Dp = Medium      // Space between focus stats
        val BreakSpacing: Dp = ExtraLarge  // Space during break screens
    }
    
    object Routine {
        val StepPadding: Dp = Medium       // Padding within routine steps
        val StepSpacing: Dp = Small        // Space between routine steps
        val TimerSpacing: Dp = Large       // Space around step timers
        val CompletionSpacing: Dp = ExtraLarge // Space for completion celebration
    }
    
    object Navigation {
        val TabHeight: Dp = 56.dp          // Bottom navigation tab height
        val TabPadding: Dp = Small         // Padding within tabs
        val AppBarHeight: Dp = 64.dp       // Top app bar height
        val AppBarPadding: Dp = Medium     // App bar content padding
    }
    
    // Touch target specifications
    object TouchTarget {
        val Minimum: Dp = 48.dp            // Minimum touch target size
        val Comfortable: Dp = 56.dp        // Comfortable touch target size
        val Large: Dp = 64.dp              // Large touch target for primary actions
    }
    
    // Animation and motion spacing
    object Motion {
        val SlideDistance: Dp = 32.dp      // Distance for slide animations
        val FadeOffset: Dp = 16.dp         // Offset for fade animations
        val ScaleOrigin: Dp = 24.dp        // Origin point for scale animations
    }
}

/**
 * Extension functions for common spacing patterns
 */

// Horizontal spacing helpers
val Dp.horizontal: androidx.compose.foundation.layout.PaddingValues
    get() = androidx.compose.foundation.layout.PaddingValues(horizontal = this)

// Vertical spacing helpers  
val Dp.vertical: androidx.compose.foundation.layout.PaddingValues
    get() = androidx.compose.foundation.layout.PaddingValues(vertical = this)

// All sides spacing helper
val Dp.all: androidx.compose.foundation.layout.PaddingValues
    get() = androidx.compose.foundation.layout.PaddingValues(all = this)

/**
 * Responsive spacing that adapts to screen size
 * Useful for tablets and different screen densities
 */
object ResponsiveSpacing {
    fun small(compact: Dp = AdhdSpacing.Small, expanded: Dp = AdhdSpacing.Medium): Dp {
        // In a real implementation, this would check screen size
        // For now, return the compact size
        return compact
    }
    
    fun medium(compact: Dp = AdhdSpacing.Medium, expanded: Dp = AdhdSpacing.Large): Dp {
        return compact
    }
    
    fun large(compact: Dp = AdhdSpacing.Large, expanded: Dp = AdhdSpacing.ExtraLarge): Dp {
        return compact
    }
}