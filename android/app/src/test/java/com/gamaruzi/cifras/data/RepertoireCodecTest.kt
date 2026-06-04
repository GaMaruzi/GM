package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RepertoireCodecTest {

    private val SEP = ""

    @Test
    fun `roundtrip vazio preserva id e nome`() {
        val rep = Repertoire("abc-uuid", "Show de domingo", emptyList())
        assertEquals(rep, RepertoireCodec.decode(RepertoireCodec.encode(rep)))
    }

    @Test
    fun `roundtrip com musicas preserva ordem`() {
        val rep = Repertoire(
            id = "abc",
            name = "Acústico",
            songIds = listOf("content://a", "content://b", "content://c"),
        )
        assertEquals(rep, RepertoireCodec.decode(RepertoireCodec.encode(rep)))
    }

    @Test
    fun `nome com acentos e pipes sobrevive`() {
        val rep = Repertoire("x", "Show: João | É · Tchau", listOf("content://x"))
        assertEquals(rep, RepertoireCodec.decode(RepertoireCodec.encode(rep)))
    }

    @Test
    fun `decode formato invalido devolve null`() {
        assertNull(RepertoireCodec.decode(""))
        assertNull(RepertoireCodec.decode("apenas_id_sem_nome"))
    }

    @Test
    fun `decode id ou nome em branco devolve null`() {
        assertNull(RepertoireCodec.decode("idVazio" + SEP))
        assertNull(RepertoireCodec.decode(SEP + "Nome"))
    }

    @Test
    fun `moveUp troca com o anterior`() {
        val rep = Repertoire("x", "Y", listOf("a", "b", "c"))
        val movido = RepertoireCodec.moveUp(rep, 2)
        assertEquals(listOf("a", "c", "b"), movido.songIds)
    }

    @Test
    fun `moveUp no extremo zero e no-op`() {
        val rep = Repertoire("x", "Y", listOf("a", "b"))
        assertEquals(rep, RepertoireCodec.moveUp(rep, 0))
    }

    @Test
    fun `moveDown troca com o seguinte`() {
        val rep = Repertoire("x", "Y", listOf("a", "b", "c"))
        val movido = RepertoireCodec.moveDown(rep, 0)
        assertEquals(listOf("b", "a", "c"), movido.songIds)
    }

    @Test
    fun `addSong deduplica`() {
        val rep = Repertoire("x", "Y", listOf("a", "b"))
        val mesmoRep = RepertoireCodec.addSong(rep, "a")
        assertEquals(rep, mesmoRep)
        val novoRep = RepertoireCodec.addSong(rep, "c")
        assertEquals(listOf("a", "b", "c"), novoRep.songIds)
    }

    @Test
    fun `addSongs deduplica e preserva ordem dos novos`() {
        val rep = Repertoire("x", "Y", listOf("a", "b"))
        val novo = RepertoireCodec.addSongs(rep, listOf("c", "a", "d"))
        // a já está, então c e d entram nessa ordem
        assertEquals(listOf("a", "b", "c", "d"), novo.songIds)
    }

    @Test
    fun `removeSong tira a uri`() {
        val rep = Repertoire("x", "Y", listOf("a", "b", "c"))
        val novo = RepertoireCodec.removeSong(rep, "b")
        assertEquals(listOf("a", "c"), novo.songIds)
    }

    @Test
    fun `pruneOrphans tira songs sem entry`() {
        val rep = Repertoire("x", "Y", listOf("a", "b", "c", "d"))
        val novo = RepertoireCodec.pruneOrphans(rep, setOf("a", "c"))
        assertEquals(listOf("a", "c"), novo.songIds)
    }
}
