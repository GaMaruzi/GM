package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongFormatDetectorTest {

    @Test
    fun `text plain mime vira TEXT`() {
        assertEquals(SongFormat.TEXT, SongFormatDetector.fromMimeType("text/plain"))
    }

    @Test
    fun `application pdf mime vira PDF`() {
        assertEquals(SongFormat.PDF, SongFormatDetector.fromMimeType("application/pdf"))
    }

    @Test
    fun `mimes de imagem suportados viram IMAGE`() {
        listOf("image/jpeg", "image/jpg", "image/png", "image/webp").forEach { mime ->
            assertEquals("falhou para $mime", SongFormat.IMAGE, SongFormatDetector.fromMimeType(mime))
        }
    }

    @Test
    fun `mime null devolve null`() {
        assertNull(SongFormatDetector.fromMimeType(null))
    }

    @Test
    fun `mime desconhecido devolve null`() {
        assertNull(SongFormatDetector.fromMimeType("application/zip"))
        assertNull(SongFormatDetector.fromMimeType("video/mp4"))
    }

    @Test
    fun `mime e case-insensitive`() {
        assertEquals(SongFormat.PDF, SongFormatDetector.fromMimeType("APPLICATION/PDF"))
        assertEquals(SongFormat.IMAGE, SongFormatDetector.fromMimeType("Image/JPEG"))
    }

    @Test
    fun `extensao txt vira TEXT`() {
        assertEquals(SongFormat.TEXT, SongFormatDetector.fromFileName("cifra.txt"))
        assertEquals(SongFormat.TEXT, SongFormatDetector.fromFileName("CIFRA.TXT"))
    }

    @Test
    fun `extensao pdf vira PDF`() {
        assertEquals(SongFormat.PDF, SongFormatDetector.fromFileName("partitura.pdf"))
    }

    @Test
    fun `extensoes de imagem suportadas viram IMAGE`() {
        listOf("foto.jpg", "foto.JPEG", "foto.png", "foto.webp").forEach { name ->
            assertEquals("falhou para $name", SongFormat.IMAGE, SongFormatDetector.fromFileName(name))
        }
    }

    @Test
    fun `arquivo sem extensao devolve null`() {
        assertNull(SongFormatDetector.fromFileName("arquivo_sem_ext"))
    }

    @Test
    fun `nome null devolve null`() {
        assertNull(SongFormatDetector.fromFileName(null))
    }

    @Test
    fun `detect prioriza mime quando ambos disponiveis`() {
        // Mime diz PDF mesmo com extensão errada — confiamos no mime.
        assertEquals(
            SongFormat.PDF,
            SongFormatDetector.detect("application/pdf", "arquivo_sem_extensao_correta"),
        )
    }

    @Test
    fun `detect cai pra extensao quando mime ausente`() {
        assertEquals(SongFormat.TEXT, SongFormatDetector.detect(null, "musica.txt"))
        assertEquals(SongFormat.IMAGE, SongFormatDetector.detect(null, "scan.jpg"))
    }

    @Test
    fun `detect devolve null quando nenhum dos dois identifica`() {
        assertNull(SongFormatDetector.detect("application/zip", "arquivo.zip"))
        assertNull(SongFormatDetector.detect(null, null))
    }
}
