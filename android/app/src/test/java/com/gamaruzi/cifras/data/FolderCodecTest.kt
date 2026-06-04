package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FolderCodecTest {

    private val SEP = ""

    @Test
    fun `roundtrip preserva id e nome`() {
        val folder = Folder(id = "abc-123-uuid", name = "Show de domingo")
        assertEquals(folder, FolderCodec.decode(FolderCodec.encode(folder)))
    }

    @Test
    fun `nome com acentos e simbolos sobrevive`() {
        val folder = Folder(id = "x", name = "É: Música · Coração (versão 2)")
        assertEquals(folder, FolderCodec.decode(FolderCodec.encode(folder)))
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
    fun `encode usa o separador U+0001`() {
        val raw = FolderCodec.encode(Folder("aaa", "bbb"))
        assertEquals("aaa" + SEP + "bbb", raw)
    }
}
