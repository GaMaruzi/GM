package com.gamaruzi.cifras.data

// Repertório (antigo "Setlist", agora plural). Cada repertório é uma lista
// ordenada de URIs de Song, com nome próprio. O usuário pode ter vários:
// "Show de domingo", "Acústico", "Casamento da Mari".
//
// Defaults (a partir de v1.2.0):
// - color: chave da paleta visual (ver ui/common/EntityColors.kt)
// - defaultTextZoom: tamanho da fonte (sp) usado no Stage para TXT
// - defaultImageZoom: zoom inicial para IMG/PDF no Stage
// - defaultScrollSpeed: velocidade (px/s) do auto-scroll no Stage
//   quando a cifra não tem velocidade própria gravada em speeds_v1.
//
// Esses defaults são herdados ao entrar no palco e atualizados sempre
// que o usuário muda zoom/velocidade durante a apresentação.
data class Repertoire(
    val id: String,
    val name: String,
    val songIds: List<String>,
    val color: String = "green",
    val defaultTextZoom: Int = 18,
    val defaultImageZoom: Float = 1.0f,
    val defaultScrollSpeed: Int = 0,
)

// Codec linha-única. Separador U+0001 ("SEP").
//
// v1 (legado): `id<SEP>name<SEP>songId1<SEP>songId2<SEP>...`
// v2 (atual):  `id<SEP>name<SEP>$color:green,txt:18,img:1.0,speed:0<SEP>songId1<SEP>...`
//
// O bloco de metadata em v2 sempre começa com `$` — caractere ausente
// em URIs SAF, que servem de songIds — o que torna seguro distinguir as
// duas versões observando parts[2]. Encoder sempre escreve v2.
internal object RepertoireCodec {
    private const val SEP = ""
    private const val META_PREFIX = "$"
    private const val META_KV_SEP = ","
    private const val META_KV_EQ = ":"

    fun encode(rep: Repertoire): String {
        val meta = META_PREFIX + listOf(
            "color${META_KV_EQ}${rep.color}",
            "txt${META_KV_EQ}${rep.defaultTextZoom}",
            "img${META_KV_EQ}${rep.defaultImageZoom}",
            "speed${META_KV_EQ}${rep.defaultScrollSpeed}",
        ).joinToString(META_KV_SEP)
        return (listOf(rep.id, rep.name, meta) + rep.songIds).joinToString(SEP)
    }

    fun decode(raw: String): Repertoire? {
        if (raw.isEmpty()) return null
        val parts = raw.split(SEP)
        if (parts.size < 2) return null
        if (parts[0].isBlank() || parts[1].isBlank()) return null

        val rest = if (parts.size > 2) parts.subList(2, parts.size) else emptyList()
        val (meta, songIds) = splitMetaAndSongs(rest)
        return Repertoire(
            id = parts[0],
            name = parts[1],
            songIds = songIds,
            color = meta["color"] ?: "green",
            defaultTextZoom = meta["txt"]?.toIntOrNull() ?: 18,
            defaultImageZoom = meta["img"]?.toFloatOrNull() ?: 1.0f,
            defaultScrollSpeed = meta["speed"]?.toIntOrNull() ?: 0,
        )
    }

    private fun splitMetaAndSongs(rest: List<String>): Pair<Map<String, String>, List<String>> {
        if (rest.isEmpty()) return emptyMap<String, String>() to emptyList()
        val first = rest[0]
        if (!first.startsWith(META_PREFIX)) {
            // v1: a partir do índice 2 são todos songIds.
            return emptyMap<String, String>() to rest
        }
        val metaMap = first.removePrefix(META_PREFIX).split(META_KV_SEP)
            .mapNotNull {
                val kv = it.split(META_KV_EQ, limit = 2)
                if (kv.size == 2 && kv[0].isNotBlank()) kv[0] to kv[1] else null
            }
            .toMap()
        return metaMap to rest.drop(1)
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
