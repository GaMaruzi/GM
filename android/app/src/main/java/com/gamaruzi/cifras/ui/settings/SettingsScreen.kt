package com.gamaruzi.cifras.ui.settings

import androidx.compose.runtime.Composable
import com.gamaruzi.cifras.ui.common.PlaceholderScreen

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    PlaceholderScreen(
        titulo = "Configurações",
        descricao = "Trocar pasta de cifras · tema claro/escuro/sistema · cores dinâmicas · card de privacidade · sobre.",
        onBack = onBack,
    )
}
