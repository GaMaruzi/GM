package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryEntryCodecTest {

    @Test
    fun `roundtrip preserva todos os campos`() {
        val original = LibraryEntry(
            uri = "content://com.android.providers.media.documents/document/image%3A123",
            displayName = "Asa Branca - Luiz Gonzaga.jpg",
            format = SongFormat.IMAGE,
            sizeBytes = 1_234_567,
        )
        val encoded = LibraryEntryCodec.encode(original)
        val decoded = LibraryEntryCodec.decode(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `roundtrip funciona para os 3 formatos`() {
        listOf(SongFormat.TEXT, SongFormat.IMAGE, SongFormat.PDF).forEach { format ->
            val entry = LibraryEntry("content://x", "nome.ext", format, 42)
            assertEquals(entry, LibraryEntryCodec.decode(LibraryEntryCodec.encode(entry)))
        }
    }

    @Test
    fun `nome com espacos e caracteres especiais sobrevive`() {
        // Pipes, dois-pontos, acentos — caracteres comuns em nomes reais.
        val entry = LibraryEntry(
            uri = "content://foo/bar",
            displayName = "É | Música: Ré, Mi (versão 2).pdf",
            format = SongFormat.PDF,
            sizeBytes = 999,
        )
        assertEquals(entry, LibraryEntryCodec.decode(LibraryEntryCodec.encode(entry)))
    }

    @Test
    fun `string mal formada devolve null`() {
        assertNull(LibraryEntryCodec.decode(""))
        assertNull(LibraryEntryCodec.decode("apenas_uri_sem_separador"))
        assertNull(LibraryEntryCodec.decode("urinomeapenas_2_partes"))
    }

    @Test
    fun `formato invalido devolve null`() {
        val raw = "content://xnome.txtAUDIO100"
        assertNull(LibraryEntryCodec.decode(raw))
    }

    @Test
    fun `tamanho nao numerico devolve null`() {
        val raw = "content://xnome.txtTEXTnao_eh_numero"
        assertNull(LibraryEntryCodec.decode(raw))
    }

    @Test
    fun `tamanho zero ou negativo e aceito no codec`() {
        // Validação de limite é em SizeLimits, não no codec.
        val zero = LibraryEntry("content://x", "n", SongFormat.TEXT, 0)
        assertEquals(zero, LibraryEntryCodec.decode(LibraryEntryCodec.encode(zero)))
    }
}
