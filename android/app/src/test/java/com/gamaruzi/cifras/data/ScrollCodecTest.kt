package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrollCodecTest {

    @Test
    fun `mapa vazio roundtrip`() {
        assertTrue(RecentsCodec.decode(RecentsCodec.encode(emptyList())).isEmpty())
        assertEquals(emptyMap<String, Int>(), ScrollCodec.decode(ScrollCodec.encode(emptyMap())))
    }

    @Test
    fun `decode string vazia devolve mapa vazio`() {
        assertEquals(emptyMap<String, Int>(), ScrollCodec.decode(""))
    }

    @Test
    fun `roundtrip preserva uri e offset`() {
        val mapa = mapOf(
            "content://a" to 1200,
            "content://b" to 0,
            "content://c" to 99999,
        )
        assertEquals(mapa, ScrollCodec.decode(ScrollCodec.encode(mapa)))
    }

    @Test
    fun `offset zero sobrevive roundtrip`() {
        // saveScrollOffset trata 0 como "remover", mas o codec em si aceita 0.
        val mapa = mapOf("content://x" to 0)
        assertEquals(mapa, ScrollCodec.decode(ScrollCodec.encode(mapa)))
    }

    @Test
    fun `decode com numero de partes impar devolve mapa vazio`() {
        // String corrompida ou de versão futura — degradar graciosamente.
        val raw = listOf("content://a", "100", "content://b").joinToString("\u0001")
        assertEquals(emptyMap<String, Int>(), ScrollCodec.decode(raw))
    }

    @Test
    fun `decode pula entrada com offset nao numerico`() {
        val raw = listOf(
            "content://a", "100",
            "content://b", "nao_eh_numero",
            "content://c", "200",
        ).joinToString("\u0001")
        val decoded = ScrollCodec.decode(raw)
        assertEquals(2, decoded.size)
        assertEquals(100, decoded["content://a"])
        assertEquals(200, decoded["content://c"])
    }

    @Test
    fun `pruneOrphans remove offsets de URIs ausentes`() {
        val mapa = mapOf("a" to 100, "b" to 200, "c" to 300)
        val resultado = ScrollCodec.pruneOrphans(mapa, setOf("a", "c"))
        assertEquals(mapOf("a" to 100, "c" to 300), resultado)
    }

    @Test
    fun `pruneOrphans com nenhuma valida devolve mapa vazio`() {
        assertEquals(
            emptyMap<String, Int>(),
            ScrollCodec.pruneOrphans(mapOf("a" to 1, "b" to 2), emptySet()),
        )
    }

    @Test
    fun `pruneOrphans preserva mapa quando todas validas`() {
        val mapa = mapOf("a" to 1, "b" to 2)
        assertEquals(mapa, ScrollCodec.pruneOrphans(mapa, setOf("a", "b", "c")))
    }
}
