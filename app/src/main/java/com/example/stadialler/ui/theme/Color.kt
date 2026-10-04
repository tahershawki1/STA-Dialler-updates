package com.example.stadialler.ui.theme

import androidx.compose.ui.graphics.Color

// Samsung One UI Dark Theme Palette
val SamsungDarkBg = Color(0xFF000000)          // Pure AMOLED Black
val SamsungSurface = Color(0xFF141519)         // One UI Card Background
val SamsungSurfaceVariant = Color(0xFF1E1F24)  // One UI Elevated Surface
val SamsungSurfaceElevated = Color(0xFF282930) // One UI Highlight Surface

val SamsungGreen = Color(0xFF12B262)          // Signature Samsung Call Green
val SamsungGreenLight = Color(0xFF22C55E)     // Vibrant Green
val SamsungGreenDark = Color(0xFF0E8F4E)      // Pressed Green

val SamsungBlue = Color(0xFF0381FE)           // One UI Primary Blue
val SamsungBlueLight = Color(0xFF45A5FF)
val SamsungBlueVariant = Color(0xFF1976D2)

val SamsungRed = Color(0xFFE53935)            // Missed Call Red
val SamsungRedLight = Color(0xFFF85149)
val SamsungAmber = Color(0xFFF59E0B)          // Warning / Notice Amber

val SamsungTextPrimary = Color(0xFFFFFFFF)    // Crisp White Text
val SamsungTextSecondary = Color(0xFF8E8E93)  // One UI Subtitle Gray
val SamsungTextMuted = Color(0xFF5A5B62)      // Subtle Hint Gray
val SamsungDivider = Color(0xFF23242B)        // Subtle Border / Divider

// Compatibility Aliases for existing screen imports
val DarkBackground = SamsungDarkBg
val DarkSurface = SamsungSurface
val DarkSurfaceVariant = SamsungSurfaceVariant
val DarkSurfaceElevated = SamsungSurfaceElevated

val CyanPrimary = SamsungGreen
val CyanPrimaryVariant = SamsungGreenDark
val CyanAccent = SamsungBlue
val CyanLight = SamsungBlueLight

val GreenConnect = SamsungGreen
val GreenConnectLight = SamsungGreenLight
val RedDisconnect = SamsungRed
val RedDisconnectLight = SamsungRedLight
val AmberUpdate = SamsungAmber
val AmberUpdateLight = Color(0xFFFBBF24)

val TextPrimary = SamsungTextPrimary
val TextSecondary = SamsungTextSecondary
val TextMuted = SamsungTextMuted

val KeypadButtonBg = Color(0xFF16171B)
val KeypadButtonBorder = SamsungDivider
val KeypadButtonPressed = Color(0xFF23242A)
