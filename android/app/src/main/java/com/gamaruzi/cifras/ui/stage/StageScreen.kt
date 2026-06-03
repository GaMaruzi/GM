package com.gamaruzi.cifras.ui.stage

import androidx.compose.runtime.Composable
import com.gamaruzi.cifras.ui.common.PlaceholderScreen

@Composable
fun StageScreen(onBack: () -> Unit) {
    PlaceholderScreen(
        titulo = "Modo Palco",
        descricao = "Fundo preto, fonte gigante, tela sempre acesa, auto-scroll e tap-to-advance: 1× toque vai pra próxima música, 2× vai pra anterior.",
        onBack = onBack,
    )
}
