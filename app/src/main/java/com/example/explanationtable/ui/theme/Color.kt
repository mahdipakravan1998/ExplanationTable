package com.example.explanationtable.ui.theme

import androidx.compose.ui.graphics.Color

// ------------------------------
// Core Brand Colors
// Primary brand colors that form the foundation of the design language.
// ------------------------------
val FeatherGreen = Color(0xFF58CC02) // Used for success indications
val MaskGreen = Color(0xFF89E219)    // Main brand accent color
val Eel = Color(0xFF4B4B4B)          // Dark neutral, often used for text or contrast

// ------------------------------
// Secondary Palette
// Secondary colors to complement the core brand colors for accents and highlights.
// ------------------------------
val Macaw = Color(0xFF1CB0F6)
val Cardinal = Color(0xFFFF4B4B)
val Bee = Color(0xFFFFC800)
val Fox = Color(0xFFFF9600)
val Beetle = Color(0xFFCE82FF)
val Humpback = Color(0xFF2B70C9)

// ------------------------------
// Neutrals
// Neutral colors used for backgrounds, borders, and subtle UI elements.
// ------------------------------
val Wolf = Color(0xFF777777)
val Hare = Color(0xFFAFAFAF)
val Swan = Color(0xFFE5E5E5)
val Polar = Color(0xFFF7F7F7)

// ------------------------------
// Duo's Palette
// Additional accent colors for specialized UI elements and details.
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
// Legacy colors kept for backward compatibility or optional mappings.
// ------------------------------
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val Teal700 = Color(0xFF018786)

// ------------------------------
// Common Colors
// Universal colors used throughout the UI.
// ------------------------------
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

// ------------------------------
// Additional Colors
// Color used for cell text in gameplay components.
// ------------------------------
val DarkBrown = Color(0xFF5E3700)

// ------------------------------
// Light Theme Colors
// Define the color scheme for light mode.
// ------------------------------
val PrimaryLight = MaskGreen           // Core brand primary color
val PrimaryContainerLight = LimeGreen    // Container color for primary components
val OnPrimaryLight = White               // Content color displayed on primary-colored backgrounds

val SecondaryLight = BeakUpper           // Secondary color from Duo's Palette
val SecondaryContainerLight = Fox          // Secondary container color from Secondary Palette
val OnSecondaryLight = White               // Content color displayed on secondary-colored backgrounds

val TertiaryLight = Macaw                // Tertiary accent color from Secondary Palette
val TertiaryContainerLight = Humpback      // Container for tertiary elements
val OnTertiaryLight = White               // Content color for tertiary components

val BackgroundLight = Color(0xFFFFFDFD)    // Light background (nearly white)
val SurfaceLight = BackgroundLight         // Surface uses the same color as the background
val OnBackgroundLight = Eel                // Content color on the background
val OnSurfaceLight = Wolf                  // Content color on surfaces

val AccentLight = PrimaryLight             // Accent mapping example (reuses primary)
val HighlightLight = SecondaryLight        // Highlight mapping example (reuses secondary)
val OverlayLight = Polar                   // Overlay color from neutrals
val ErrorLight = Color(0xFFB22222)          // Error state color
val OnErrorLight = White                   // Content color on error backgrounds
val SuccessLight = FeatherGreen            // Success state color from core brand

// ------------------------------
// Dark Theme Colors
// Define the color scheme for dark mode.
// ------------------------------
val PrimaryDark = MaskGreen              // Primary color (same as light mode)
val PrimaryContainerDark = LimeGreen       // Container color for primary elements
val OnPrimaryDark = White                  // Content color on primary dark backgrounds

val SecondaryDark = BeakUpper              // Secondary color (same as light mode)
val SecondaryContainerDark = Fox             // Container color for secondary elements
val OnSecondaryDark = White                // Content color on secondary dark backgrounds

val TertiaryDark = Macaw                 // Tertiary accent color (same as light mode)
val TertiaryContainerDark = Humpback       // Container color for tertiary elements
val OnTertiaryDark = White                 // Content color on tertiary dark backgrounds

val BackgroundDark = Color(0xFF141F23)     // Dark background for dark mode
val SurfaceDark = BackgroundDark           // Surface color mirrors the background
val OnBackgroundDark = Polar               // Content color on dark backgrounds
val OnSurfaceDark = Hare                   // Content color on dark surfaces

val AccentDark = Teal200                   // Accent color from legacy palette for dark mode
val HighlightDark = Humpback               // Highlight color from secondary palette
val OverlayDark = Color(0xB3000000)        // Semi-transparent overlay for dark mode
val ErrorDark = Color(0xFFCD5C5C)           // Error color for dark mode
val OnErrorDark = Color(0xFF000000)         // Content color on error backgrounds in dark mode
val SuccessDark = Color(0xFF2E8B57)         // Success state color for dark mode

// ------------------------------
// UI Element Colors
// Specific colors for UI elements such as buttons and dialogs.
// ------------------------------
val ButtonBackgroundLight = Color(0xDFFFFFFF)  // Light mode button background with transparency
val ButtonBackgroundDark = BackgroundDark      // Dark mode button background

val ButtonIconLight = White                   // Button icon color for light mode
val ButtonIconDark = Eel                      // Button icon color for dark mode

// Dark mode specific UI elements
val DialogBackgroundDark = Color(0xFF141F23)    // Dialog background in dark mode
val BorderDark = Color(0xFF38464F)             // Border color for dark mode elements

// Light mode specific UI elements
val DialogBackgroundLight = Color(0xFFFFFFFD)   // Dialog background in light mode (nearly white)
val BorderLight = Swan            // Border color for light mode elements

// Text color optimized for dark mode
val TextDarkMode = Color(0xFFF1F7FB)            // Used for text in dark mode

// ------------------------------
// Selection Mode Colors
// Colors used to indicate selection states, with variants for light and dark modes.
// ------------------------------

// Light mode selection colors (green-themed)
val Turtle = Color(0xFFA5ED6E)      // Border color for selected items
val SeaSponge = Color(0xFFD7FFB8)   // Background color for selected items
val TreeFrog = Color(0xFF58A700)    // Text color for selected items

// Dark mode selection colors (green-themed)
val DarkGreenBorder = Color(0xFF5F8428)     // Border color for selected items in dark mode
val DarkGreenBackground = Color(0xFF202F36) // Background color for selected items in dark mode
val DarkGreenText = Color(0xFF79B933)       // Text color for selected items in dark mode

// Light mode selection colors (blue-themed)
val Iguana = Color(0xFFDDF4FF)   // Background color for blue-themed selection in light mode
val BlueJay = Color(0xFF84D8FF)  // Border color for blue-themed selection in light mode
val Whale = Color(0xFF1899D6)    // Text color for blue-themed selection in light mode

// Dark mode selection colors (blue-themed)
val DarkBlueBackground = Color(0xFF202F36)  // Background color for blue-themed selection in dark mode
val DarkBlueBorder = Color(0xFF84D8FF)      // Border color for blue-themed selection in dark mode
val DarkBlueText = Color(0xFF1899D6)        // Text color for blue-themed selection in dark mode

// ------------------------------
// Prize Box Colors
// Colors used specifically for the PrizeBox composable.
// ------------------------------
val IconCircleDark = Color(0xFF7AB934)            // Icon circle color for dark theme in PrizeBox
val CheckColorDark = Color(0xFF152024)              // Check icon color for dark theme in PrizeBox
val PrizeButtonBackgroundDark = Color(0xFF93D334)   // Prize button background color for dark theme
val PrizeButtonBackgroundLight = Color(0xFF57CC02)  // Prize button background color for light theme

// New colors for StageReviewTable components
val HeaderBackgroundLight = Color(0xFFBAE7FC)
val HeaderBackgroundDark = Color(0xFF235063)

val BorderColorLight = Color(0xFF84D7FF)
val BorderColorDark = Color(0xFF3F85A7)

val TextColorLight = Eel
val TextColorDark = TextDarkMode

val NeutralColorDark = Color(0xFF9BA3A6)
val StrokeColor = Color(0xFFFFC801)
val SpecialFillColor = Color(0xFFD79534)
val SpecialFillGlowColor = Color(0xFFFFD334)
val TitleTextColorDark = Color(0xFFF2F7FB)

val Seal = Color(0xFFB7B7B7)