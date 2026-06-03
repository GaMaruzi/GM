package com.gamaruzi.cifras.data

// Codec para a lista MRU (Most Recently Used) de cifras abertas. Persistida
// como string única no DataStore, separada por U+0001 — mesma convenção do
// LibraryEntryCodec. Lista vazia vira string vazia.
internal object RecentsCodec {
    // U+0001 (Start of Heading) — caractere de controle ausente em URIs reais,
    // dispensa escape. Escrito como  para sobreviver a qualquer
    // normalização de ferramentas (igual em runtime ao byte 0x01 cru).
    private const val SEP = ""
    const val MAX_SIZE = 12

    fun encode(uris: List<String>): String = uris.joinToString(SEP)

    fun decode(raw: String): List<String> =
        if (raw.isEmpty()) emptyList() else raw.split(SEP)

    // Move (ou insere) a URI no topo, descarta o resto que extrapola MAX_SIZE.
    // - Se a URI já existia, é removida da posição antiga antes de ser inserida.
    // - Garante deduplicação por URI.
    fun applyMRU(atual: List<String>, recente: String, max: Int = MAX_SIZE): List<String> =
        (listOf(recente) + atual.filter { it != recente }).take(max)

    // Filtra a lista mantendo só URIs ainda presentes na biblioteca.
    // Usado quando o usuário exclui cifras ou troca a biblioteca inteira.
    fun pruneOrphans(atual: List<String>, urisValidas: Set<String>): List<String> =
        atual.filter { it in urisValidas }
}
