package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FolderCodecTest {

    private val SEP = ""

    @Test
    fun `roundtrip v2 preserva id, nome e cor`() {
        val folder = Folder(id = "abc-123-uuid", name = "Show de domingo", color = "blue")
        assertEquals(folder, FolderCodec.decode(FolderCodec.encode(folder)))
    }

    @Test
    fun `nome com acentos e simbolos sobrevive`() {
        val folder = Folder(id = "x", name = "É: Música · Coração (versão 2)", color = "purple")
        assertEquals(folder, FolderCodec.decode(FolderCodec.encode(folder)))
    }

    @Test
    fun `decode v1 sem cor cai em verde default (retrocompat)`() {
        // Pastas criadas antes da v1.2.0 não tinham cor — devem entrar como "green".
        val rawV1 = "abc" + SEP + "Show"
        val folder = FolderCodec.decode(rawV1)
        assertEquals(Folder(id = "abc", name = "Show", color = "green"), folder)
    }

    @Test
    fun `decode com formato invalido devolve null`() {
        assertNull(FolderCodec.decode(""))
        assertNull(FolderCodec.decode("sem_separador"))
    }

    @Test
    fun `decode com id ou nome em branco devolve null`() {
        assertNull(FolderCodec.decode("id" + SEP))
        assertNull(FolderCodec.decode(SEP + "Nome"))
    }

    @Test
    fun `decode com cor em branco cai no default`() {
        val folder = FolderCodec.decode("abc" + SEP + "Nome" + SEP + "")
        assertEquals(Folder(id = "abc", name = "Nome", color = "green"), folder)
    }

    @Test
    fun `encode escreve v2 com tres campos`() {
        val raw = FolderCodec.encode(Folder("aaa", "bbb", "red"))
        assertEquals("aaa" + SEP + "bbb" + SEP + "red", raw)
    }
}
