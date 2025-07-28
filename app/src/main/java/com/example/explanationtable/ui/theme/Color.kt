package com.example.explanationtable.ui.theme

import androidx.compose.ui.graphics.Color

// -----------------------------------------------------------------------------------------
// Core Brand Colors
// -----------------------------------------------------------------------------------------

/** Primary brand colors used throughout the app. */
val FeatherGreen = Color(0xFF58CC02)    // Success state indicator
val MaskGreen    = Color(0xFF89E219)    // Main brand accent
val Eel          = Color(0xFF4B4B4B)    // Dark neutral for high-contrast text

// -----------------------------------------------------------------------------------------
// Secondary Palette
// -----------------------------------------------------------------------------------------

/** Complementary accent colors for highlights and emphasis. */
val Macaw    = Color(0xFF1CB0F6)
val Cardinal = Color(0xFFFF4B4B)
val Bee      = Color(0xFFFFC800)
val Fox      = Color(0xFFFF9600)
val Beetle   = Color(0xFFCE82FF)
val Humpback = Color(0xFF2B70C9)

// -----------------------------------------------------------------------------------------
// Neutral Colors
// -----------------------------------------------------------------------------------------

/** Neutrals for backgrounds, borders, and subtle UI elements. */
val Wolf  = Color(0xFF777777)
val Hare  = Color(0xFFAFAFAF)
val Swan  = Color(0xFFE5E5E5)
val Polar = Color(0xFFF7F7F7)

// -----------------------------------------------------------------------------------------
// Duo’s Palette
// -----------------------------------------------------------------------------------------

/** Specialized accents for detailed UI elements. */
val WingOverlay      = Color(0xFF43C000)
val BeakInner        = Color(0xFFB66E28)
val BeakLowerFeet    = Color(0xFFF49000)
val BeakUpper        = Color(0xFFFFC200)
val BeakHighlight    = Color(0xFFFFDE00)
val TonguePink       = Color(0xFFFFCAFF)
val LimeGreen        = Color(0xFF68A62F)

// -----------------------------------------------------------------------------------------
// Legacy Colors (Optional)
// -----------------------------------------------------------------------------------------

/** Kept for backward compatibility or optional mappings. */
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200   = Color(0xFF03DAC5)
val Teal700   = Color(0xFF018786)

// -----------------------------------------------------------------------------------------
// Common Colors
// -----------------------------------------------------------------------------------------

/** Universally-used colors. */
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

// -----------------------------------------------------------------------------------------
// Gameplay Component Colors
// -----------------------------------------------------------------------------------------

/** Text color for gameplay cells. */
val DarkBrown = Color(0xFF5E3700)

// -----------------------------------------------------------------------------------------
// Light Theme Color Scheme
// -----------------------------------------------------------------------------------------

/** Primary and secondary roles in light mode. */
val PrimaryLight            = MaskGreen
val PrimaryContainerLight   = LimeGreen
val OnPrimaryLight          = White

val SecondaryLight          = BeakUpper
val SecondaryContainerLight = Fox
val OnSecondaryLight        = White

val TertiaryLight           = Macaw
val TertiaryContainerLight  = Humpback
val OnTertiaryLight         = White

/** Surface roles in light mode. */
val BackgroundLight   = Color(0xFFFFFDFD)  // Near-white background
val SurfaceLight      = BackgroundLight
val OnBackgroundLight = Eel
val OnSurfaceLight    = Wolf

/** State & accent roles in light mode. */
val AccentLight    = PrimaryLight
val HighlightLight = SecondaryLight
val OverlayLight   = Polar

val ErrorLight     = Color(0xFFB22222)
val OnErrorLight   = White
val SuccessLight   = FeatherGreen

// -----------------------------------------------------------------------------------------
// Dark Theme Color Scheme
// -----------------------------------------------------------------------------------------

/** Primary and secondary roles in dark mode. */
val PrimaryDark            = MaskGreen
val PrimaryContainerDark   = LimeGreen
val OnPrimaryDark          = White

val SecondaryDark          = BeakUpper
val SecondaryContainerDark = Fox
val OnSecondaryDark        = White

val TertiaryDark           = Macaw
val TertiaryContainerDark  = Humpback
val OnTertiaryDark         = White

/** Surface roles in dark mode. */
val BackgroundDark   = Color(0xFF141F23)  // Deep background
val SurfaceDark      = BackgroundDark
val OnBackgroundDark = Polar
val OnSurfaceDark    = Hare

/** State & accent roles in dark mode. */
val AccentDark    = Teal200
val HighlightDark = Humpback
val OverlayDark   = Color(0xB3000000)     // Semi-transparent overlay

val ErrorDark     = Color(0xFFCD5C5C)
val OnErrorDark   = Color(0xFF000000)
val SuccessDark   = Color(0xFF2E8B57)

// -----------------------------------------------------------------------------------------
// UI Element Colors
// -----------------------------------------------------------------------------------------

/** Button backgrounds and icons. */
val ButtonBackgroundLight = Color(0xDFFFFFFF) // Light mode with transparency
val ButtonBackgroundDark  = BackgroundDark

val ButtonIconLight = White
val ButtonIconDark  = Eel

/** Dialogs and borders. */
val DialogBackgroundLight = Color(0xFFFFFFFD)
val BorderLight           = Swan

val DialogBackgroundDark = BackgroundDark
val BorderDark           = Color(0xFF38464F)

/** Text color for dark mode. */
val TextDarkMode = Color(0xFFF1F7FB)

// -----------------------------------------------------------------------------------------
// Selection Mode Colors
// -----------------------------------------------------------------------------------------

/** Light mode, green-themed selection. */
val Turtle          = Color(0xFFA5ED6E)  // Border
val SeaSponge       = Color(0xFFD7FFB8)  // Background
val TreeFrog        = Color(0xFF58A700)  // Text

/** Dark mode, green-themed selection. */
val DarkGreenBorder     = Color(0xFF5F8428)
val DarkGreenBackground = Color(0xFF202F36)
val DarkGreenText       = Color(0xFF79B933)

/** Light mode, blue-themed selection. */
val Iguana     = Color(0xFFDDF4FF)  // Background
val BlueJay    = Color(0xFF84D8FF)  // Border
val Whale      = Color(0xFF1899D6)  // Text

/** Dark mode, blue-themed selection. */
val DarkBlueBackground = Color(0xFF202F36)
val DarkBlueBorder     = Color(0xFF84D8FF)
val DarkBlueText       = Color(0xFF1899D6)

// -----------------------------------------------------------------------------------------
// PrizeBox Component Colors
// -----------------------------------------------------------------------------------------

/** PrizeBox-specific theming. */
val IconCircleDark          = Color(0xFF7AB934)
val CheckColorDark          = Color(0xFF152024)
val PrizeButtonBackgroundDark  = Color(0xFF93D334)
val PrizeButtonBackgroundLight = Color(0xFF57CC02)

// -----------------------------------------------------------------------------------------
// StageReviewTable Component Colors
// -----------------------------------------------------------------------------------------

val HeaderBackgroundLight = Color(0xFFBAE7FC)
val HeaderBackgroundDark  = Color(0xFF235063)

val BorderColorLight = Color(0xFF84D7FF)
val BorderColorDark  = Color(0xFF3F85A7)

val TextColorLight = Eel
val TextColorDark  = TextDarkMode

val NeutralColorDark     = Color(0xFF9BA3A6)
val StrokeColor         = Color(0xFFFFC801)
val SpecialFillColor    = Color(0xFFD79534)
val SpecialFillGlowColor = Color(0xFFFFD334)
val TitleTextColorDark  = Color(0xFFF2F7FB)

val Seal  = Color(0xFFB7B7B7)
val Raven = Color(0xFF2C383F)
val Pigeon = Color(0xFF37464F)
val Heron  = Color(0xFF52656D)
