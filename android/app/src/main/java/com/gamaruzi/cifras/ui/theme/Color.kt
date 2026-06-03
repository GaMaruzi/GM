package com.gamaruzi.cifras.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta "Verde Spotify" (semente verde Material 3) do design v2.
// Spec: docs/design/v2-verde-icone-paleta/README.md
// Tema padrão = LIGHT. Dark é fallback automático quando o sistema está em dark.

// === LIGHT ===
val PrimaryLight = Color(0xFF107A39)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF1ED760)
val OnPrimaryContainerLight = Color(0xFF00210E)

val SecondaryLight = Color(0xFF3E6B4B)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFC2F0CD)
val OnSecondaryContainerLight = Color(0xFF00210E)

val TertiaryLight = Color(0xFF1C6B4A)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFA6F2C6)
val OnTertiaryContainerLight = Color(0xFF002112)

val BackgroundLight = Color(0xFFFBFDF7)
val OnBackgroundLight = Color(0xFF191C19)
val SurfaceLight = Color(0xFFFBFDF7)
val OnSurfaceLight = Color(0xFF191C19)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF2F6EF)
val SurfaceContainerLightToken = Color(0xFFECF1E9)
val SurfaceContainerHighLight = Color(0xFFE6EBE3)
val SurfaceContainerHighestLight = Color(0xFFE0E6DD)
val OnSurfaceVariantLight = Color(0xFF414941)
val OutlineLight = Color(0xFF717971)
val OutlineVariantLight = Color(0xFFC0C9BF)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// === DARK ===
val PrimaryDark = Color(0xFF1ED760)
val OnPrimaryDark = Color(0xFF00390F)
val PrimaryContainerDark = Color(0xFF0A8A3F)
val OnPrimaryContainerDark = Color(0xFFC7F7D2)

val SecondaryDark = Color(0xFFA6D3B2)
val OnSecondaryDark = Color(0xFF0E3320)
val SecondaryContainerDark = Color(0xFF234A33)
val OnSecondaryContainerDark = Color(0xFFC2F0CD)

val TertiaryDark = Color(0xFF86D6AC)
val OnTertiaryDark = Color(0xFF00391F)
val TertiaryContainerDark = Color(0xFF004F30)
val OnTertiaryContainerDark = Color(0xFFA6F2C6)

val BackgroundDark = Color(0xFF101510)
val OnBackgroundDark = Color(0xFFE0E6DD)
val SurfaceDark = Color(0xFF101510)
val OnSurfaceDark = Color(0xFFE0E6DD)
val SurfaceContainerLowestDark = Color(0xFF0A0F0A)
val SurfaceContainerLowDark = Color(0xFF181D18)
val SurfaceContainerDarkToken = Color(0xFF1A211B)
val SurfaceContainerHighDark = Color(0xFF252B25)
val SurfaceContainerHighestDark = Color(0xFF303630)
val OnSurfaceVariantDark = Color(0xFFC0C9BF)
val OutlineDark = Color(0xFF8A938A)
val OutlineVariantDark = Color(0xFF414941)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// === Modo Palco === (paleta fixa, independe do tema — para PR 8)
val StageBackground = Color(0xFF000000)
val StageText = Color(0xFFF2F2F2)
val StageTextDim = Color(0xFF8A8A8A)
val StageChord = Color(0xFF1ED760)
