package com.gamaruzi.cifras.ui.theme

// Modo de tema escolhido pelo usuário. Sem "Sistema" — o app é offline e
// quer identidade visual estável. Default = CLARO (verde Spotify).
enum class ThemeMode {
    CLARO, ESCURO;

    companion object {
        // Decode resiliente: valores desconhecidos (downgrade, bug, ou valor
        // antigo "SISTEMA" removido nesta versão) caem em CLARO.
        fun fromKey(raw: String?): ThemeMode =
            raw?.uppercase()?.let { key -> entries.firstOrNull { it.name == key } } ?: CLARO
    }
}
