package com.gamaruzi.cifras.data

// Codec + manipulações do setlist (lista ordenada de URIs de Song).
// Diferente do recents (MRU), aqui a ordem é definida pelo usuário e o
// setlist normalmente é pequeno (3-20 músicas de um show).
internal object SetlistCodec {
    private const val SEP = ""

    fun encode(list: List<String>): String = list.joinToString(SEP)

    fun decode(raw: String): List<String> =
        if (raw.isEmpty()) emptyList() else raw.split(SEP)

    // Move o item da posição index uma para cima (troca com o anterior).
    // No-op nos extremos ou se index inválido.
    fun moveUp(list: List<String>, index: Int): List<String> {
        if (index <= 0 || index >= list.size) return list
        val out = list.toMutableList()
        val tmp = out[index]
        out[index] = out[index - 1]
        out[index - 1] = tmp
        return out
    }

    fun moveDown(list: List<String>, index: Int): List<String> {
        if (index < 0 || index >= list.size - 1) return list
        val out = list.toMutableList()
        val tmp = out[index]
        out[index] = out[index + 1]
        out[index + 1] = tmp
        return out
    }

    // Adiciona no fim, sem duplicar.
    fun add(list: List<String>, uri: String): List<String> =
        if (uri in list) list else list + uri

    fun remove(list: List<String>, uri: String): List<String> = list.filter { it != uri }

    fun pruneOrphans(list: List<String>, urisValidas: Set<String>): List<String> =
        list.filter { it in urisValidas }
}
