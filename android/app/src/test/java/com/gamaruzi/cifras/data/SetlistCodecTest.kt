package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetlistCodecTest {

    @Test
    fun `lista vazia roundtrip`() {
        assertEquals(emptyList<String>(), SetlistCodec.decode(SetlistCodec.encode(emptyList())))
    }

    @Test
    fun `decode string vazia devolve lista vazia`() {
        assertTrue(SetlistCodec.decode("").isEmpty())
    }

    @Test
    fun `roundtrip preserva ordem`() {
        val list = listOf("c", "a", "b", "d")
        assertEquals(list, SetlistCodec.decode(SetlistCodec.encode(list)))
    }

    @Test
    fun `add em lista vazia coloca o item`() {
        assertEquals(listOf("a"), SetlistCodec.add(emptyList(), "a"))
    }

    @Test
    fun `add no fim sem duplicar`() {
        assertEquals(listOf("a", "b", "c"), SetlistCodec.add(listOf("a", "b"), "c"))
    }

    @Test
    fun `add de URI ja existente e no-op`() {
        val list = listOf("a", "b", "c")
        assertEquals(list, SetlistCodec.add(list, "b"))
    }

    @Test
    fun `remove tira a URI mantendo ordem das outras`() {
        assertEquals(listOf("a", "c"), SetlistCodec.remove(listOf("a", "b", "c"), "b"))
    }

    @Test
    fun `remove de URI ausente e no-op`() {
        val list = listOf("a", "b")
        assertEquals(list, SetlistCodec.remove(list, "x"))
    }

    @Test
    fun `moveUp troca item com o anterior`() {
        assertEquals(listOf("b", "a", "c"), SetlistCodec.moveUp(listOf("a", "b", "c"), 1))
    }

    @Test
    fun `moveUp no topo e no-op`() {
        val list = listOf("a", "b", "c")
        assertEquals(list, SetlistCodec.moveUp(list, 0))
    }

    @Test
    fun `moveUp com index invalido e no-op`() {
        val list = listOf("a", "b")
        assertEquals(list, SetlistCodec.moveUp(list, -1))
        assertEquals(list, SetlistCodec.moveUp(list, 99))
    }

    @Test
    fun `moveDown troca item com o proximo`() {
        assertEquals(listOf("b", "a", "c"), SetlistCodec.moveDown(listOf("a", "b", "c"), 0))
    }

    @Test
    fun `moveDown no fim e no-op`() {
        val list = listOf("a", "b", "c")
        assertEquals(list, SetlistCodec.moveDown(list, 2))
    }

    @Test
    fun `pruneOrphans remove URIs ausentes preservando ordem`() {
        val list = listOf("a", "b", "c", "d")
        assertEquals(listOf("a", "c"), SetlistCodec.pruneOrphans(list, setOf("a", "c")))
    }

    @Test
    fun `ciclo de moves cancela`() {
        val list = listOf("a", "b", "c")
        val depois = SetlistCodec.moveUp(SetlistCodec.moveDown(list, 0), 1)
        assertEquals(list, depois)
    }
}
