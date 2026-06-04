package com.gamaruzi.cifras.ui.search

import com.gamaruzi.cifras.data.Folder
import com.gamaruzi.cifras.data.Section
import com.gamaruzi.cifras.data.Song
import com.gamaruzi.cifras.data.SongFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class SortModeCodecTest {

    @Test
    fun `decode vazio devolve mapa vazio`() {
        assertEquals(emptyMap<SearchTab, SortMode>(), SortModeCodec.decode(""))
    }

    @Test
    fun `roundtrip preserva todas as entradas`() {
        val mapa = mapOf(
            SearchTab.TODAS to SortMode.ALFABETICA_DESC,
            SearchTab.FAVORITAS to SortMode.QUANTIDADE_DESC,
            SearchTab.RECENTES to SortMode.ALFABETICA_ASC,
        )
        val raw = SortModeCodec.encode(mapa)
        assertEquals(mapa, SortModeCodec.decode(raw))
    }

    @Test
    fun `decode ignora pares invalidos`() {
        // Tab desconhecida ou modo desconhecido caem fora; entradas válidas mantêm.
        val raw = "TODAS=ALFABETICA_ASC;DESCONHECIDA=QUALQUER;FAVORITAS=NAO_EXISTE"
        assertEquals(
            mapOf(SearchTab.TODAS to SortMode.ALFABETICA_ASC),
            SortModeCodec.decode(raw),
        )
    }

    @Test
    fun `forTab devolve default quando ausente`() {
        val vazio = emptyMap<SearchTab, SortMode>()
        assertEquals(SortMode.ALFABETICA_ASC, SortModeCodec.forTab(vazio, SearchTab.TODAS))
        assertEquals(SortMode.ALFABETICA_ASC, SortModeCodec.forTab(vazio, SearchTab.FAVORITAS))
    }
}

class SortFoldersTest {

    private fun folder(name: String) = Folder(id = name, name = name)

    private val pastas = listOf(folder("Zumbi"), folder("alfa"), folder("Bravo"))

    private val contagens = mapOf<String?, Int>(
        "Zumbi" to 1,
        "alfa" to 7,
        "Bravo" to 3,
    )

    @Test
    fun `alfabetica asc ignora caixa`() {
        val ordenadas = sortFolders(pastas, contagens, SortMode.ALFABETICA_ASC)
        assertEquals(listOf("alfa", "Bravo", "Zumbi"), ordenadas.map { it.name })
    }

    @Test
    fun `alfabetica desc inverte`() {
        val ordenadas = sortFolders(pastas, contagens, SortMode.ALFABETICA_DESC)
        assertEquals(listOf("Zumbi", "Bravo", "alfa"), ordenadas.map { it.name })
    }

    @Test
    fun `quantidade desc prioriza pasta com mais cifras`() {
        val ordenadas = sortFolders(pastas, contagens, SortMode.QUANTIDADE_DESC)
        assertEquals(listOf("alfa", "Bravo", "Zumbi"), ordenadas.map { it.name })
    }
}

class SortSongsTest {

    private fun song(file: String) = Song(
        id = file,
        file = file,
        title = file,
        artist = "—",
        key = "C",
        capo = 0,
        genre = "",
        ext = "txt",
        format = SongFormat.TEXT,
        sizeBytes = 1,
        sections = emptyList<Section>(),
        folderId = null,
    )

    private val cifras = listOf(song("Zumbi.txt"), song("alfa.txt"), song("Bravo.txt"))

    @Test
    fun `alfabetica asc ignora caixa`() {
        val ordenadas = sortSongs(cifras, SortMode.ALFABETICA_ASC)
        assertEquals(listOf("alfa.txt", "Bravo.txt", "Zumbi.txt"), ordenadas.map { it.file })
    }

    @Test
    fun `alfabetica desc inverte`() {
        val ordenadas = sortSongs(cifras, SortMode.ALFABETICA_DESC)
        assertEquals(listOf("Zumbi.txt", "Bravo.txt", "alfa.txt"), ordenadas.map { it.file })
    }

    @Test
    fun `quantidade desc em cifras cai em alfabetica asc`() {
        val ordenadas = sortSongs(cifras, SortMode.QUANTIDADE_DESC)
        assertEquals(listOf("alfa.txt", "Bravo.txt", "Zumbi.txt"), ordenadas.map { it.file })
    }
}

class JuntarNomeEArtistaTest {

    @Test
    fun `sem artista mantem so o nome`() {
        assertEquals("Asa Branca", juntarNomeEArtista("Asa Branca", ""))
        assertEquals("Asa Branca", juntarNomeEArtista("Asa Branca", "   "))
    }

    @Test
    fun `com artista junta com traco`() {
        assertEquals(
            "Asa Branca - Luiz Gonzaga",
            juntarNomeEArtista("Asa Branca", "Luiz Gonzaga"),
        )
    }

    @Test
    fun `trim aplica em ambos os campos`() {
        assertEquals(
            "Wave - Tom Jobim",
            juntarNomeEArtista("  Wave  ", "  Tom Jobim  "),
        )
    }
}
