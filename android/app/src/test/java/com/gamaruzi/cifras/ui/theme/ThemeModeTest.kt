package com.gamaruzi.cifras.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `fromKey nulo cai em CLARO`() {
        assertEquals(ThemeMode.CLARO, ThemeMode.fromKey(null))
    }

    @Test
    fun `fromKey string vazia cai em CLARO`() {
        assertEquals(ThemeMode.CLARO, ThemeMode.fromKey(""))
    }

    @Test
    fun `fromKey valor desconhecido cai em CLARO`() {
        // Cenário de downgrade: prefs gravadas em versão futura com novo valor.
        assertEquals(ThemeMode.CLARO, ThemeMode.fromKey("ALTO_CONTRASTE"))
    }

    @Test
    fun `fromKey migra SISTEMA legado para CLARO`() {
        // Versão 1.2.0 removeu ThemeMode.SISTEMA. Preferências antigas
        // gravadas como "SISTEMA" caem no default CLARO.
        assertEquals(ThemeMode.CLARO, ThemeMode.fromKey("SISTEMA"))
    }

    @Test
    fun `fromKey case insensitive`() {
        assertEquals(ThemeMode.CLARO, ThemeMode.fromKey("claro"))
        assertEquals(ThemeMode.ESCURO, ThemeMode.fromKey("Escuro"))
    }

    @Test
    fun `roundtrip name to fromKey funciona para todos os valores`() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromKey(mode.name))
        }
    }
}
