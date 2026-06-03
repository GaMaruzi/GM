package com.gamaruzi.cifras.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `fromKey nulo cai em SISTEMA`() {
        assertEquals(ThemeMode.SISTEMA, ThemeMode.fromKey(null))
    }

    @Test
    fun `fromKey string vazia cai em SISTEMA`() {
        assertEquals(ThemeMode.SISTEMA, ThemeMode.fromKey(""))
    }

    @Test
    fun `fromKey valor desconhecido cai em SISTEMA`() {
        // Cenário de downgrade: prefs gravadas em versão futura com novo valor.
        assertEquals(ThemeMode.SISTEMA, ThemeMode.fromKey("ALTO_CONTRASTE"))
    }

    @Test
    fun `fromKey case insensitive`() {
        assertEquals(ThemeMode.CLARO, ThemeMode.fromKey("claro"))
        assertEquals(ThemeMode.ESCURO, ThemeMode.fromKey("Escuro"))
        assertEquals(ThemeMode.SISTEMA, ThemeMode.fromKey("SISTEMA"))
    }

    @Test
    fun `roundtrip name to fromKey funciona para todos os valores`() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromKey(mode.name))
        }
    }
}
