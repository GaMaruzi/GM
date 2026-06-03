package com.gamaruzi.cifras.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SizeLimitsTest {

    @Test
    fun `TEXT nao tem limite de tamanho`() {
        assertTrue(SizeLimits.withinLimit(SongFormat.TEXT, 0))
        assertTrue(SizeLimits.withinLimit(SongFormat.TEXT, 1))
        assertTrue(SizeLimits.withinLimit(SongFormat.TEXT, 100L * 1024 * 1024))
    }

    @Test
    fun `IMAGE aceita ate 3MB exato`() {
        assertTrue(SizeLimits.withinLimit(SongFormat.IMAGE, 1))
        assertTrue(SizeLimits.withinLimit(SongFormat.IMAGE, 1024))
        assertTrue(SizeLimits.withinLimit(SongFormat.IMAGE, SizeLimits.IMAGE_BYTES))
    }

    @Test
    fun `IMAGE rejeita acima de 3MB`() {
        assertFalse(SizeLimits.withinLimit(SongFormat.IMAGE, SizeLimits.IMAGE_BYTES + 1))
        assertFalse(SizeLimits.withinLimit(SongFormat.IMAGE, 10L * 1024 * 1024))
    }

    @Test
    fun `IMAGE rejeita tamanho zero`() {
        // ContentResolver às vezes devolve 0 — não vale persistir.
        assertFalse(SizeLimits.withinLimit(SongFormat.IMAGE, 0))
    }

    @Test
    fun `PDF aceita ate 8MB exato`() {
        assertTrue(SizeLimits.withinLimit(SongFormat.PDF, 1))
        assertTrue(SizeLimits.withinLimit(SongFormat.PDF, SizeLimits.PDF_BYTES))
    }

    @Test
    fun `PDF rejeita acima de 8MB`() {
        assertFalse(SizeLimits.withinLimit(SongFormat.PDF, SizeLimits.PDF_BYTES + 1))
        assertFalse(SizeLimits.withinLimit(SongFormat.PDF, 100L * 1024 * 1024))
    }

    @Test
    fun `constantes batem com a especificacao`() {
        // Estes números são parte do contrato com o usuário; trava qualquer
        // alteração silenciosa.
        assertTrue(SizeLimits.IMAGE_BYTES == 3L * 1024 * 1024)
        assertTrue(SizeLimits.PDF_BYTES == 8L * 1024 * 1024)
        assertTrue(SizeLimits.PDF_MAX_PAGES == 20)
    }
}
