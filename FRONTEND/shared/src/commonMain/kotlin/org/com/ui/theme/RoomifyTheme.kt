package org.com.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object RoomifyColors {
    val Primary = Color(0xFF1A237E)
    val PrimaryLight = Color(0xFF3949AB)
    val Secondary = Color(0xFFFF9800)
    
    // Borders and Outlines
    val Border = Color(0xFFBDBDBD)       // Visible neutral
    val BorderStrong = Color(0xFF9E9E9E) // Stronger neutral
    val BorderLight = Color(0xFFE0E0E0)  // Very light (use sparingly)
    
    val InputBorder = Color(0xFF9E9E9E)  // Clearly visible input border
    val InputBorderFocused = Color(0xFF1A237E)
    
    val Error = Color(0xFFD32F2F)
    val Success = Color(0xFF2E7D32)
    
    val Surface = Color.White
    val Background = Color(0xFFF5F5F5)
    
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)

    // Shared extended palette (use these instead of per-screen raw colours)
    val TextStrong = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val Divider = Color(0xFFE2E8F0)
    val Warning = Color(0xFFF59E0B)
    val Accent = Color(0xFF03DAC5)
    val Skeleton = Color(0xFFE5E7EB)
    val SkeletonHighlight = Color(0xFFF3F4F6)
}

/** Shared spacing & corner radii so screens stay visually consistent. */
object RoomifyDimens {
    val ScreenPadding = androidx.compose.ui.unit.Dp(16f)
    val CardRadius = androidx.compose.ui.unit.Dp(16f)
    val ChipRadius = androidx.compose.ui.unit.Dp(12f)
}

@Composable
fun RoomifyTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = RoomifyColors.Primary,
        onPrimary = Color.White,
        primaryContainer = RoomifyColors.PrimaryLight,
        onPrimaryContainer = Color.White,
        secondary = RoomifyColors.Secondary,
        onSecondary = Color.White,
        error = RoomifyColors.Error,
        onError = Color.White,
        outline = RoomifyColors.BorderStrong,        // Global outline color
        outlineVariant = RoomifyColors.Border,       // Secondary outline color
        surface = RoomifyColors.Surface,
        onSurface = RoomifyColors.TextPrimary,
        background = RoomifyColors.Background,
        onBackground = RoomifyColors.TextPrimary
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
