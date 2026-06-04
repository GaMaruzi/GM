package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryEntryCodecTest {

    private val SEP = ""

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

    @Test
    fun `customName e preservado no roundtrip v2`() {
        val entry = LibraryEntry(
            uri = "content://x",
            displayName = "IMG_2390.jpg",
            format = SongFormat.IMAGE,
            sizeBytes = 500_000,
            customName = "Wonderwall - Oasis",
        )
        val decoded = LibraryEntryCodec.decode(LibraryEntryCodec.encode(entry))
        assertEquals(entry, decoded)
        assertEquals("Wonderwall - Oasis", decoded?.customName)
        assertEquals("Wonderwall - Oasis", decoded?.nomeExibicao)
    }

    @Test
    fun `customName null vira string vazia no encode mas volta como null no decode`() {
        // Garante que entries sem renomear não viram customName = ""
        val entry = LibraryEntry("content://x", "nome.txt", SongFormat.TEXT, 100, customName = null)
        val decoded = LibraryEntryCodec.decode(LibraryEntryCodec.encode(entry))
        assertNull(decoded?.customName)
        assertEquals("nome.txt", decoded?.nomeExibicao)
    }

    @Test
    fun `decoder aceita esquema v1 de 4 campos preservando back-compat`() {
        // Formato antigo, gravado por versões anteriores ao PR-D. Construo a
        // string com listOf().joinToString para não depender de literais com
        // caractere de controle U+0001 (que ferramentas podem normalizar).
        val v1raw = listOf("content://x", "nome.txt", "TEXT", "100").joinToString("")
        val decoded = LibraryEntryCodec.decode(v1raw)
        assertEquals(
            LibraryEntry("content://x", "nome.txt", SongFormat.TEXT, 100, customName = null),
            decoded,
        )
    }

    @Test
    fun `nomeExibicao usa customName quando presente, senao displayName`() {
        val sem = LibraryEntry("x", "original.pdf", SongFormat.PDF, 1)
        val com = sem.copy(customName = "Apelido")
        assertEquals("original.pdf", sem.nomeExibicao)
        assertEquals("Apelido", com.nomeExibicao)
    }

    @Test
    fun `customName com caracteres especiais sobrevive`() {
        val entry = LibraryEntry(
            uri = "x",
            displayName = "scan.jpg",
            format = SongFormat.IMAGE,
            sizeBytes = 100,
            customName = "Música: Çoração (versão acústica)",
        )
        assertEquals(entry, LibraryEntryCodec.decode(LibraryEntryCodec.encode(entry)))
    }

    @Test
    fun `folderId e preservado no roundtrip v3`() {
        val entry = LibraryEntry(
            uri = "content://x",
            displayName = "Asa.txt",
            format = SongFormat.TEXT,
            sizeBytes = 80,
            folderId = "folder-uuid-1",
        )
        val decoded = LibraryEntryCodec.decode(LibraryEntryCodec.encode(entry))
        assertEquals(entry, decoded)
        assertEquals("folder-uuid-1", decoded?.folderId)
    }

    @Test
    fun `folderId null sobrevive ao roundtrip v3`() {
        val entry = LibraryEntry("content://x", "n.txt", SongFormat.TEXT, 10)
        val decoded = LibraryEntryCodec.decode(LibraryEntryCodec.encode(entry))
        assertNull(decoded?.folderId)
    }

    @Test
    fun `decoder aceita esquema v2 de 5 campos sem folderId`() {
        // v2 = bibliotecas criadas entre PR-D e PR 11. Não tem folderId.
        val v2raw = listOf(
            "content://x", "nome.txt", "TEXT", "200", "Custom"
        ).joinToString(SEP)
        val decoded = LibraryEntryCodec.decode(v2raw)
        assertEquals(
            LibraryEntry("content://x", "nome.txt", SongFormat.TEXT, 200, customName = "Custom"),
            decoded,
        )
        assertNull(decoded?.folderId)
    }

    @Test
    fun `roundtrip combinando customName e folderId`() {
        val entry = LibraryEntry(
            uri = "content://y",
            displayName = "IMG.jpg",
            format = SongFormat.IMAGE,
            sizeBytes = 2000,
            customName = "Sweet Child",
            folderId = "f-1",
        )
        assertEquals(entry, LibraryEntryCodec.decode(LibraryEntryCodec.encode(entry)))
    }
}
