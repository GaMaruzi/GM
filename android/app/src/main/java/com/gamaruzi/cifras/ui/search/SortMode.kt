package com.gamaruzi.cifras.ui.search

// Ordenação aplicada à lista de pastas/cifras na home, escolhida pelo
// usuário por aba (Todas/Favoritas/Recentes) e persistida em DataStore.
//
// - ALFABETICA_ASC / ALFABETICA_DESC: aplica em pastas (pelo nome) e
//   em cifras (pelo nome de exibição).
// - QUANTIDADE_DESC: aplica em pastas (pelo número de cifras). Para
//   cifras, cai em ALFABETICA_ASC porque "quantidade" não faz sentido
//   numa lista plana de cifras.
enum class SortMode(val label: String) {
    ALFABETICA_ASC("A → Z"),
    ALFABETICA_DESC("Z → A"),
    QUANTIDADE_DESC("Mais cifras");
}

// Codec compacto: "TAB=MODE;TAB=MODE". Tab ausente cai no default.
internal object SortModeCodec {
    private const val PAIR_SEP = ";"
    private const val KV_SEP = "="
    private val DEFAULT = SortMode.ALFABETICA_ASC

    fun decode(raw: String): Map<SearchTab, SortMode> {
        if (raw.isBlank()) return emptyMap()
        return raw.split(PAIR_SEP).mapNotNull { pair ->
            val parts = pair.split(KV_SEP)
            if (parts.size != 2) return@mapNotNull null
            val tab = runCatching { SearchTab.valueOf(parts[0]) }.getOrNull() ?: return@mapNotNull null
            val mode = runCatching { SortMode.valueOf(parts[1]) }.getOrNull() ?: return@mapNotNull null
            tab to mode
        }.toMap()
    }

    fun encode(map: Map<SearchTab, SortMode>): String =
        map.entries.joinToString(PAIR_SEP) { (tab, mode) -> "${tab.name}$KV_SEP${mode.name}" }

    fun forTab(map: Map<SearchTab, SortMode>, tab: SearchTab): SortMode =
        map[tab] ?: DEFAULT
}
