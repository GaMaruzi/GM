package com.gamaruzi.cifras.ui.theme

// Modo de tema selecionado pelo usuário. SISTEMA é o default — segue o
// modo atual do Android. CLARO/ESCURO sobrepõem a escolha do sistema.
enum class ThemeMode {
    SISTEMA, CLARO, ESCURO;

    companion object {
        // Decode resiliente: valores desconhecidos (downgrade ou bug)
        // caem em SISTEMA. Strings em case insensitive.
        fun fromKey(raw: String?): ThemeMode =
            raw?.uppercase()?.let { key -> entries.firstOrNull { it.name == key } } ?: SISTEMA
    }
}
