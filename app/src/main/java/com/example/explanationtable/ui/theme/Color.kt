package com.example.explanationtable.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.explanationtable.ui.theme.BeakLowerFeet

// ------------------------------
// Core Brand Colors
// ------------------------------
val FeatherGreen = Color(0xFF58CC02)
val MaskGreen = Color(0xFF89E219)
val Eel = Color(0xFF4B4B4B)

// ------------------------------
// Secondary Palette
// ------------------------------
val Macaw = Color(0xFF1CB0F6)
val Cardinal = Color(0xFFFF4B4B)
val Bee = Color(0xFFFFC800)
val Fox = Color(0xFFFF9600)
val Beetle = Color(0xFFCE82FF)
val Humpback = Color(0xFF2B70C9)

// ------------------------------
// Neutrals
// ------------------------------
val Wolf = Color(0xFF777777)
val Hare = Color(0xFFAFAFAF)
val Swan = Color(0xFFE5E5E5)
val Polar = Color(0xFFF7F7F7)

// ------------------------------
// Duo's Palette
// ------------------------------
val WingOverlay = Color(0xFF43C000)
val BeakInner = Color(0xFFB66E28)
val BeakLowerFeet = Color(0xFFF49000)
val BeakUpper = Color(0xFFFFC200)
val BeakHighlight = Color(0xFFFFDE00)
val TonguePink = Color(0xFFFFCAFF)
val LimeGreen = Color(0xFF68A62F)

// ------------------------------
// Optional Legacy Colors
// ------------------------------
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val Teal700 = Color(0xFF018786)

// ------------------------------
// Common Colors
// ------------------------------
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

// ------------------------------
// Light Theme Colors
// ------------------------------
val PrimaryLight = MaskGreen // Core Brand Colors
val PrimaryContainerLight = LimeGreen
val OnPrimaryLight = White

val SecondaryLight = BeakUpper // Secondary Palette
val SecondaryContainerLight = Fox // Secondary Palette
val OnSecondaryLight = White

val TertiaryLight = Macaw // Duo's Palette
val TertiaryContainerLight = Humpback // Duo's Palette
val OnTertiaryLight = White

val BackgroundLight = Color(0xFFFFFDFD) // White
val SurfaceLight = BackgroundLight // Same as BackgroundLight
val OnBackgroundLight = Eel
val OnSurfaceLight = Wolf

val AccentLight = PrimaryLight // Example mapping
val HighlightLight = SecondaryLight // Example mapping
val OverlayLight = Polar // Neutrals
val ErrorLight = Color(0xFFB22222)
val OnErrorLight = White
val SuccessLight = FeatherGreen // Already defined

// ------------------------------
// Dark Theme Colors
// ------------------------------
val PrimaryDark = MaskGreen // Core Brand Colors
val PrimaryContainerDark = LimeGreen // Core Brand Colors
val OnPrimaryDark = White

val SecondaryDark = BeakUpper // Secondary Palette
val SecondaryContainerDark = Fox // Secondary Palette
val OnSecondaryDark = White

val TertiaryDark = Macaw // Duo's Palette
val TertiaryContainerDark = Humpback // Duo's Palette
val OnTertiaryDark = White

val BackgroundDark = Color(0xFF141F23) // Night Mode Color
val SurfaceDark = BackgroundDark // Same as BackgroundDark
val OnBackgroundDark = Polar
val OnSurfaceDark = Hare

val AccentDark = Teal200 // Optional Legacy Colors
val HighlightDark = Humpback // Secondary Palette
val OverlayDark = Color(0xB3000000) // Already defined
val ErrorDark = Color(0xFFCD5C5C)
val OnErrorDark = Color(0xFF000000)
val SuccessDark = Color(0xFF2E8B57) // Already defined

// ------------------------------
// UI Element Colors
// ------------------------------
val ButtonBackgroundLight = Color(0xDFFFFFFF)
val ButtonBackgroundDark = BackgroundDark

val ButtonIconLight = White
val ButtonIconDark = Eel

// Dark Mode Colors
val DialogBackgroundDark = Color(0xFF141F23)
val BorderDark = Color(0xFF38464F)

// Light Mode Colors
val DialogBackgroundLight = Color(0xFFFF_FFFD)
val BorderLight = Color(0xFFE5E5E5)
