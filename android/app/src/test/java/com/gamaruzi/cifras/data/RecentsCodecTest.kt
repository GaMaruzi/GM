package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentsCodecTest {

    @Test
    fun `lista vazia roundtrip`() {
        assertEquals(emptyList<String>(), RecentsCodec.decode(RecentsCodec.encode(emptyList())))
    }

    @Test
    fun `decode de string vazia devolve lista vazia`() {
        // Importante: split de string vazia daria listOf("") sem o early-return.
        assertTrue(RecentsCodec.decode("").isEmpty())
    }

    @Test
    fun `roundtrip preserva ordem original`() {
        val lista = listOf("content://a", "content://b", "content://c")
        assertEquals(lista, RecentsCodec.decode(RecentsCodec.encode(lista)))
    }

    @Test
    fun `roundtrip com URIs com caracteres especiais e percent-encoding`() {
        val lista = listOf(
            "content://com.android.providers.media.documents/document/image%3A123",
            "content://x/y%20z",
        )
        assertEquals(lista, RecentsCodec.decode(RecentsCodec.encode(lista)))
    }

    @Test
    fun `applyMRU em lista vazia adiciona o item no topo`() {
        assertEquals(listOf("a"), RecentsCodec.applyMRU(emptyList(), "a"))
    }

    @Test
    fun `applyMRU move item ja existente para o topo sem duplicar`() {
        val atual = listOf("a", "b", "c")
        assertEquals(listOf("b", "a", "c"), RecentsCodec.applyMRU(atual, "b"))
    }

    @Test
    fun `applyMRU coloca item novo no topo preservando o resto`() {
        val atual = listOf("a", "b", "c")
        assertEquals(listOf("d", "a", "b", "c"), RecentsCodec.applyMRU(atual, "d"))
    }

    @Test
    fun `applyMRU respeita o cap descartando o final da lista`() {
        val atual = (1..12).map { "uri$it" }
        val resultado = RecentsCodec.applyMRU(atual, "novo")
        assertEquals(12, resultado.size)
        assertEquals("novo", resultado.first())
        // uri12 (que era o último) saiu pra dar lugar ao novo.
        assertTrue("uri12" !in resultado)
        assertTrue("uri11" in resultado)
    }

    @Test
    fun `applyMRU com cap customizado`() {
        assertEquals(
            listOf("c", "a"),
            RecentsCodec.applyMRU(listOf("a", "b"), "c", max = 2),
        )
    }

    @Test
    fun `MAX_SIZE e 12 conforme contrato`() {
        assertEquals(12, RecentsCodec.MAX_SIZE)
    }

    @Test
    fun `pruneOrphans remove URIs ausentes da biblioteca`() {
        val atual = listOf("a", "b", "c", "d")
        val validas = setOf("a", "c")
        assertEquals(listOf("a", "c"), RecentsCodec.pruneOrphans(atual, validas))
    }

    @Test
    fun `pruneOrphans preserva ordem das URIs validas`() {
        val atual = listOf("c", "a", "b")
        val validas = setOf("a", "b", "c")
        assertEquals(listOf("c", "a", "b"), RecentsCodec.pruneOrphans(atual, validas))
    }

    @Test
    fun `pruneOrphans com nenhuma valida devolve lista vazia`() {
        assertEquals(emptyList<String>(), RecentsCodec.pruneOrphans(listOf("a", "b"), emptySet()))
    }
}
