package com.gamaruzi.cifras.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gamaruzi.cifras.ui.pesquisar.PesquisarScreen

object Rotas {
    const val PESQUISAR = "pesquisar"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Rotas.PESQUISAR
    ) {
        composable(Rotas.PESQUISAR) { PesquisarScreen() }
        // Próximas telas (detalhe da cifra, favoritos) entram aqui.
    }
}
