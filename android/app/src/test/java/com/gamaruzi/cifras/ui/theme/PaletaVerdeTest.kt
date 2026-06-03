package com.gamaruzi.cifras.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

// "Trava" os valores da paleta verde Spotify para evitar drift acidental.
// Spec: docs/design/v2-verde-icone-paleta/README.md
class PaletaVerdeTest {

    @Test
    fun `primary light e verde escuro 107A39`() {
        assertEquals(Color(0xFF107A39), PrimaryLight)
    }

    @Test
    fun `primary container light e verde Spotify claro 1ED760`() {
        assertEquals(Color(0xFF1ED760), PrimaryContainerLight)
    }

    @Test
    fun `primary dark e verde Spotify claro 1ED760`() {
        assertEquals(Color(0xFF1ED760), PrimaryDark)
    }

    @Test
    fun `background light e quase branco esverdeado FBFDF7`() {
        assertEquals(Color(0xFFFBFDF7), BackgroundLight)
    }

    @Test
    fun `modo palco usa verde Spotify para acordes`() {
        assertEquals(Color(0xFF1ED760), StageChord)
    }

    @Test
    fun `modo palco usa fundo preto puro para OLED`() {
        assertEquals(Color(0xFF000000), StageBackground)
    }
}
