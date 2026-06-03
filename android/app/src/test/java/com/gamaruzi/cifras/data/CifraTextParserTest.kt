package com.gamaruzi.cifras.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CifraTextParserTest {

    @Test
    fun `arquivo vazio retorna sections vazia e tom default`() {
        val parsed = CifraTextParser.parse("")
        assertTrue(parsed.sections.isEmpty())
        assertEquals("—", parsed.key)
        assertEquals(0, parsed.capo)
    }

    @Test
    fun `metadados Tom e Capo sao detectados`() {
        val parsed = CifraTextParser.parse(
            """
            Tom: D
            Capotraste: 2

            [Verso]
            D       G       A
            letra qualquer aqui
            """.trimIndent()
        )
        assertEquals("D", parsed.key)
        assertEquals(2, parsed.capo)
    }

    @Test
    fun `linha de acordes seguida de letra forma um Line com ambos`() {
        val parsed = CifraTextParser.parse(
            """
            [Verso]
            G             D
            A manhã chega devagar
            """.trimIndent()
        )
        assertEquals(1, parsed.sections.size)
        val secao = parsed.sections.first()
        assertEquals("Verso", secao.tag)
        assertEquals(1, secao.lines.size)
        assertEquals("G             D", secao.lines.first().chords)
        assertEquals("A manhã chega devagar", secao.lines.first().lyrics)
    }

    @Test
    fun `linha de acordes sem letra abaixo vira Line so com acordes`() {
        val parsed = CifraTextParser.parse(
            """
            [Intro]
            G   D   Em  C
            """.trimIndent()
        )
        val linha = parsed.sections.first().lines.first()
        assertEquals("G   D   Em  C", linha.chords)
        assertEquals("", linha.lyrics)
    }

    @Test
    fun `tom inicial e inferido da primeira linha de acordes quando nao declarado`() {
        val parsed = CifraTextParser.parse(
            """
            [Intro]
            Em7   G   Cmaj7   D
            """.trimIndent()
        )
        // Removendo extensões: "Em7" -> "Em"
        assertEquals("Em", parsed.key)
    }

    @Test
    fun `secoes em maiusculo sem colchetes sao reconhecidas`() {
        val parsed = CifraTextParser.parse(
            """
            INTRO
            C  G

            REFRAO
            F  G  C
            la la la
            """.trimIndent()
        )
        assertEquals(2, parsed.sections.size)
        assertEquals("Intro", parsed.sections[0].tag)
        assertEquals("Refrão", parsed.sections[1].tag)
    }

    @Test
    fun `letra solta sem acordes acima vira Line so com letra`() {
        val parsed = CifraTextParser.parse(
            """
            [Falado]
            uma fala sem acordes
            """.trimIndent()
        )
        val linha = parsed.sections.first().lines.first()
        assertEquals("", linha.chords)
        assertEquals("uma fala sem acordes", linha.lyrics)
    }

    @Test
    fun `linha que parece chord mas tem palavra comum nao e tratada como chord`() {
        val parsed = CifraTextParser.parse(
            """
            A noite chegou e eu fiquei
            """.trimIndent()
        )
        // "A" sozinho casa o regex, mas "noite", "chegou" etc não.
        // Logo a linha inteira é letra.
        val linha = parsed.sections.first().lines.first()
        assertEquals("", linha.chords)
        assertEquals("A noite chegou e eu fiquei", linha.lyrics)
    }
}

class SplitTitleArtistTest {

    @Test
    fun `arquivo com hifen no nome separa titulo e artista`() {
        val (titulo, artista) = splitTitleArtist("Asa Branca - Luiz Gonzaga.txt")
        assertEquals("Asa Branca", titulo)
        assertEquals("Luiz Gonzaga", artista)
    }

    @Test
    fun `arquivo sem hifen mantem nome como titulo e artista vazio`() {
        val (titulo, artista) = splitTitleArtist("Wonderwall.txt")
        assertEquals("Wonderwall", titulo)
        assertEquals("—", artista)
    }

    @Test
    fun `multiplos hifens usam o ultimo como separador`() {
        val (titulo, artista) = splitTitleArtist("Eleanor Rigby - Sgt Peppers - The Beatles.txt")
        assertEquals("Eleanor Rigby - Sgt Peppers", titulo)
        assertEquals("The Beatles", artista)
    }
}
