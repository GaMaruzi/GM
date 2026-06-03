package com.gamaruzi.cifras.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TheoryTest {

    // === keyPrefersFlat ===

    @Test
    fun `tons em bemol preferem flat`() {
        listOf("F", "Bb", "Eb", "Ab", "Db", "Gb").forEach { tom ->
            assertTrue("$tom devia preferir flat", Theory.keyPrefersFlat(tom))
        }
    }

    @Test
    fun `tons menores em bemol preferem flat`() {
        listOf("Dm", "Gm", "Cm", "Fm", "Bbm").forEach { tom ->
            assertTrue("$tom devia preferir flat", Theory.keyPrefersFlat(tom))
        }
    }

    @Test
    fun `tons em sustenido preferem sharp`() {
        listOf("G", "D", "A", "E", "B", "F#", "C#", "Am", "Em", "Bm").forEach { tom ->
            assertFalse("$tom NÃO devia preferir flat", Theory.keyPrefersFlat(tom))
        }
    }

    // === noteName ===

    @Test
    fun `noteName preferindo sharp escolhe da escala SHARP`() {
        assertEquals("C", Theory.noteName(0, preferFlat = false))
        assertEquals("C#", Theory.noteName(1, preferFlat = false))
        assertEquals("F#", Theory.noteName(6, preferFlat = false))
    }

    @Test
    fun `noteName preferindo flat escolhe da escala FLAT`() {
        assertEquals("Db", Theory.noteName(1, preferFlat = true))
        assertEquals("Gb", Theory.noteName(6, preferFlat = true))
        assertEquals("Bb", Theory.noteName(10, preferFlat = true))
    }

    @Test
    fun `noteName normaliza indice negativo`() {
        assertEquals("B", Theory.noteName(-1, preferFlat = false))
        assertEquals("B", Theory.noteName(-13, preferFlat = false))
    }

    @Test
    fun `noteName normaliza indice acima de 12`() {
        assertEquals("C", Theory.noteName(12, preferFlat = false))
        assertEquals("D", Theory.noteName(14, preferFlat = false))
        assertEquals("C", Theory.noteName(24, preferFlat = false))
    }

    // === transposeChord (acorde único) ===

    @Test
    fun `transposeChord sobe C em 2 vira D`() {
        assertEquals("D", Theory.transposeChord("C", semis = 2, preferFlat = false))
    }

    @Test
    fun `transposeChord preserva sufixo`() {
        assertEquals("Dm7", Theory.transposeChord("Cm7", semis = 2, preferFlat = false))
        assertEquals("Asus4", Theory.transposeChord("Gsus4", semis = 2, preferFlat = false))
    }

    @Test
    fun `transposeChord wrap circular C-1 vira B`() {
        assertEquals("B", Theory.transposeChord("C", semis = -1, preferFlat = false))
    }

    @Test
    fun `transposeChord respeita preferFlat`() {
        assertEquals("Db", Theory.transposeChord("C", semis = 1, preferFlat = true))
        assertEquals("C#", Theory.transposeChord("C", semis = 1, preferFlat = false))
    }

    @Test
    fun `transposeChord acorde com baixo separado por slash`() {
        // G/B + 2 = A/C#
        assertEquals("A/C#", Theory.transposeChord("G/B", semis = 2, preferFlat = false))
    }

    @Test
    fun `transposeChord acorde estranho preserva sufixo complexo`() {
        assertEquals("D#maj7add9", Theory.transposeChord("Cmaj7add9", semis = 3, preferFlat = false))
    }

    @Test
    fun `transposeChord aceita acidente bemol no input`() {
        assertEquals("C", Theory.transposeChord("Bb", semis = 2, preferFlat = false))
    }

    @Test
    fun `transposeChord token irreconhecivel volta intacto`() {
        assertEquals("xyz", Theory.transposeChord("xyz", semis = 5, preferFlat = false))
    }

    @Test
    fun `transposeChord semis zero e identidade`() {
        listOf("C", "Cm7", "G/B", "F#maj9").forEach { acorde ->
            assertEquals(acorde, Theory.transposeChord(acorde, semis = 0, preferFlat = false))
        }
    }

    // === transposeKey ===

    @Test
    fun `transposeKey preserva sufixo m`() {
        assertEquals("Em", Theory.transposeKey("Dm", semis = 2, preferFlat = false))
    }

    @Test
    fun `transposeKey traço continua traço`() {
        assertEquals("—", Theory.transposeKey("—", semis = 3, preferFlat = false))
    }

    @Test
    fun `transposeKey vazio continua vazio`() {
        assertEquals("", Theory.transposeKey("", semis = 3, preferFlat = false))
    }

    // === transposeChordLine ===

    @Test
    fun `transposeChordLine semis zero e identidade`() {
        val linha = "C       G       Am      F"
        assertEquals(linha, Theory.transposeChordLine(linha, semis = 0, preferFlat = false))
    }

    @Test
    fun `transposeChordLine preserva alinhamento por colunas`() {
        // C na coluna 0, G na coluna 8, Am na coluna 16, F na coluna 24
        val original = "C       G       Am      F"
        val transposto = Theory.transposeChordLine(original, semis = 2, preferFlat = false)
        // Cada acorde transposto deve estar ancorado na mesma coluna original
        // (D, A, Bm, G — todos têm o mesmo comprimento ou menor do que os
        // originais, então não há colisão).
        assertEquals(0, transposto.indexOf("D"))
        assertEquals(8, transposto.indexOf("A"))
        assertEquals(16, transposto.indexOf("Bm"))
        assertEquals(24, transposto.indexOf("G"))
    }

    @Test
    fun `transposeChordLine quando acorde maior que original empurra com espaco`() {
        // C na col 0, D na col 1 — D+1=D# (2 chars), entra colisão.
        // O algoritmo coloca pelo menos 1 espaço entre eles.
        val original = "C D"
        val transposto = Theory.transposeChordLine(original, semis = 1, preferFlat = false)
        // C# e D# separados por pelo menos um espaço.
        assertTrue("Esperava espaço entre acordes: $transposto", "C# D#" in transposto)
    }

    @Test
    fun `transposeChordLine linha vazia volta vazia`() {
        assertEquals("", Theory.transposeChordLine("", semis = 3, preferFlat = false))
    }

    @Test
    fun `transposeChordLine multiplos acordes com slash`() {
        val original = "G/B  D/F#  Em"
        val transposto = Theory.transposeChordLine(original, semis = 2, preferFlat = false)
        // G/B+2 = A/C#, D/F#+2 = E/G#, Em+2 = F#m
        assertTrue("A/C# faltando em '$transposto'", "A/C#" in transposto)
        assertTrue("E/G# faltando em '$transposto'", "E/G#" in transposto)
        assertTrue("F#m faltando em '$transposto'", "F#m" in transposto)
    }

    // === Ciclo completo: transpor por 12 semitons volta ao original ===

    @Test
    fun `transposeChord ciclo de 12 volta ao original`() {
        val original = "C#m7"
        val ida = Theory.transposeChord(original, semis = 12, preferFlat = false)
        assertEquals(original, ida)
    }

    @Test
    fun `transposeChord ida e volta com semis opostos cancela`() {
        val original = "G"
        val ida = Theory.transposeChord(original, semis = 5, preferFlat = false)
        val volta = Theory.transposeChord(ida, semis = -5, preferFlat = false)
        assertEquals(original, volta)
    }
}
