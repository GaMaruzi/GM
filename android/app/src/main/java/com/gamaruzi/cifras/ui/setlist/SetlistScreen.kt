package com.gamaruzi.cifras.ui.setlist

import androidx.compose.runtime.Composable
import com.gamaruzi.cifras.ui.common.PlaceholderScreen

@Composable
fun SetlistScreen(onBack: () -> Unit) {
    PlaceholderScreen(
        titulo = "Setlist",
        descricao = "Aqui você vai montar a sequência de cifras do Modo Palco e definir a velocidade de rolagem de cada uma.",
        onBack = onBack,
    )
}
