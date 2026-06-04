package com.gamaruzi.cifras.data

// Repertório (antigo "Setlist", agora plural). Cada repertório é uma lista
// ordenada de URIs de Song, com nome próprio. O usuário pode ter vários:
// "Show de domingo", "Acústico", "Casamento da Mari".
data class Repertoire(
    val id: String,
    val name: String,
    val songIds: List<String>,
)

// Codec linha-única: id  name  songId1  songId2  ...
// Separador U+0001. Repertório vazio = só "id  name".
internal object RepertoireCodec {
    private const val SEP = ""

    fun encode(rep: Repertoire): String =
        (listOf(rep.id, rep.name) + rep.songIds).joinToString(SEP)

    fun decode(raw: String): Repertoire? {
        if (raw.isEmpty()) return null
        val parts = raw.split(SEP)
        if (parts.size < 2) return null
        if (parts[0].isBlank() || parts[1].isBlank()) return null
        return Repertoire(
            id = parts[0],
            name = parts[1],
            songIds = if (parts.size > 2) parts.subList(2, parts.size) else emptyList(),
        )
    }

    fun moveUp(rep: Repertoire, index: Int): Repertoire {
        if (index <= 0 || index >= rep.songIds.size) return rep
        val out = rep.songIds.toMutableList()
        val tmp = out[index]
        out[index] = out[index - 1]
        out[index - 1] = tmp
        return rep.copy(songIds = out)
    }

    fun moveDown(rep: Repertoire, index: Int): Repertoire {
        if (index < 0 || index >= rep.songIds.size - 1) return rep
        val out = rep.songIds.toMutableList()
        val tmp = out[index]
        out[index] = out[index + 1]
        out[index + 1] = tmp
        return rep.copy(songIds = out)
    }

    // Adiciona no fim, deduplica.
    fun addSong(rep: Repertoire, songId: String): Repertoire =
        if (songId in rep.songIds) rep else rep.copy(songIds = rep.songIds + songId)

    fun addSongs(rep: Repertoire, songIds: List<String>): Repertoire {
        val novosUnicos = songIds.filter { it !in rep.songIds }
        return if (novosUnicos.isEmpty()) rep else rep.copy(songIds = rep.songIds + novosUnicos)
    }

    fun removeSong(rep: Repertoire, songId: String): Repertoire =
        rep.copy(songIds = rep.songIds.filter { it != songId })

    fun pruneOrphans(rep: Repertoire, urisValidas: Set<String>): Repertoire =
        rep.copy(songIds = rep.songIds.filter { it in urisValidas })
}
