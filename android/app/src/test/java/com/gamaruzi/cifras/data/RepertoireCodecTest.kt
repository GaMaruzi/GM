package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RepertoireCodecTest {

    private val SEP = ""

    @Test
    fun `roundtrip v2 vazio preserva id, nome e defaults`() {
        val rep = Repertoire(
            id = "abc-uuid",
            name = "Show de domingo",
            songIds = emptyList(),
            color = "blue",
            defaultTextZoom = 22,
            defaultImageZoom = 1.5f,
            defaultScrollSpeed = 30,
        )
        assertEquals(rep, RepertoireCodec.decode(RepertoireCodec.encode(rep)))
    }

    @Test
    fun `roundtrip com musicas preserva ordem e metadata`() {
        val rep = Repertoire(
            id = "abc",
            name = "Acústico",
            songIds = listOf("content://a", "content://b", "content://c"),
            color = "purple",
        )
        assertEquals(rep, RepertoireCodec.decode(RepertoireCodec.encode(rep)))
    }

    @Test
    fun `nome com acentos e pipes sobrevive`() {
        val rep = Repertoire("x", "Show: João | É · Tchau", listOf("content://x"))
        assertEquals(rep, RepertoireCodec.decode(RepertoireCodec.encode(rep)))
    }

    @Test
    fun `decode v1 legado sem metadata assume defaults`() {
        // Repertórios criados antes da v1.2.0 não tinham bloco de metadata
        // (parts[2..n] eram todos songIds). O decoder reconhece pela ausência
        // do prefixo '$' e preenche os defaults.
        val rawV1 = "abc" + SEP + "Show" + SEP + "content://a" + SEP + "content://b"
        val rep = RepertoireCodec.decode(rawV1)
        val esperado = Repertoire(
            id = "abc",
            name = "Show",
            songIds = listOf("content://a", "content://b"),
            color = "green",
            defaultTextZoom = 18,
            defaultImageZoom = 1.0f,
            defaultScrollSpeed = 0,
        )
        assertEquals(esperado, rep)
    }

    @Test
    fun `decode v1 vazio sem songs`() {
        val rawV1 = "abc" + SEP + "Show"
        val rep = RepertoireCodec.decode(rawV1)
        assertEquals(
            Repertoire(id = "abc", name = "Show", songIds = emptyList()),
            rep,
        )
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
    fun `decode metadata com chave desconhecida ignora e mantem defaults`() {
        val raw = "id" + SEP + "Show" + SEP + "$" + "color:red,inventado:42" +
            SEP + "content://x"
        val rep = RepertoireCodec.decode(raw)
        assertEquals("red", rep?.color)
        assertEquals(18, rep?.defaultTextZoom)
        assertEquals(listOf("content://x"), rep?.songIds)
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
