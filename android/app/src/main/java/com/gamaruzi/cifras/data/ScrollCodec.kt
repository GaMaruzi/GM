package com.gamaruzi.cifras.data

// Codec para Map<songId, scrollOffsetPx>. Persistida como string única em
// DataStore, plana: uri1|offset1|uri2|offset2... — separador U+0001, mesmo
// padrão do LibraryEntryCodec e RecentsCodec.
//
// scrollOffset é em pixels (mesma unidade do ScrollState do Compose). Ao
// trocar de aparelho a densidade pode mudar e o offset ficar levemente
// errado — aceitável: o usuário re-rola um pouco e o novo valor sobrescreve.
internal object ScrollCodec {
    // U+0001. Escrito como "" para sobreviver a normalização de
    // ferramentas (idêntico em runtime ao byte cru).
    private val SEP = "\u0001"

    fun encode(mapa: Map<String, Int>): String =
        mapa.entries
            .flatMap { (uri, offset) -> listOf(uri, offset.toString()) }
            .joinToString(SEP)

    fun decode(raw: String): Map<String, Int> {
        if (raw.isEmpty()) return emptyMap()
        val partes = raw.split(SEP)
        if (partes.size % 2 != 0) return emptyMap()
        val mapa = mutableMapOf<String, Int>()
        for (i in partes.indices step 2) {
            val offset = partes[i + 1].toIntOrNull() ?: continue
            mapa[partes[i]] = offset
        }
        return mapa
    }

    fun pruneOrphans(mapa: Map<String, Int>, urisValidas: Set<String>): Map<String, Int> =
        mapa.filterKeys { it in urisValidas }
}
