package com.gamaruzi.cifras.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gamaruzi.cifras.ui.AppState
import com.gamaruzi.cifras.ui.detail.DetailScreen
import com.gamaruzi.cifras.ui.empty.EmptyScreen
import com.gamaruzi.cifras.ui.search.SearchScreen
import com.gamaruzi.cifras.ui.setlist.SetlistScreen
import com.gamaruzi.cifras.ui.settings.SettingsScreen
import com.gamaruzi.cifras.ui.stage.StageScreen

object Rotas {
    const val EMPTY = "empty"
    const val SEARCH = "search"
    const val DETAIL = "detail/{songId}"
    const val SETLIST = "setlist"
    const val STAGE = "stage"
    const val SETTINGS = "settings"

    fun detail(songId: String) = "detail/$songId"
}

@Composable
fun AppNavHost(appState: AppState = viewModel()) {
    val navController = rememberNavController()

    val folder by appState.folderName.collectAsStateWithLifecycle()
    val songs by appState.songs.collectAsStateWithLifecycle()
    val favorites by appState.favorites.collectAsStateWithLifecycle()
    val recents by appState.recents.collectAsStateWithLifecycle()

    val startDestination = if (folder == null) Rotas.EMPTY else Rotas.SEARCH

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Rotas.EMPTY) {
            EmptyScreen(onPickFolder = {
                appState.pickFolderMock()
                navController.navigate(Rotas.SEARCH) {
                    popUpTo(Rotas.EMPTY) { inclusive = true }
                }
            })
        }

        composable(Rotas.SEARCH) {
            SearchScreen(
                folderName = folder ?: "—",
                songs = songs,
                favorites = favorites,
                recents = recents,
                onOpenSong = { id ->
                    appState.markRecent(id)
                    navController.navigate(Rotas.detail(id))
                },
                onToggleFavorite = appState::toggleFavorite,
                onOpenSettings = { navController.navigate(Rotas.SETTINGS) },
                onChangeFolder = {
                    navController.navigate(Rotas.EMPTY) {
                        popUpTo(Rotas.SEARCH) { inclusive = true }
                    }
                },
                onStartStage = { navController.navigate(Rotas.SETLIST) },
            )
        }

        composable(
            route = Rotas.DETAIL,
            arguments = listOf(navArgument("songId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId")
            val song = songId?.let { appState.song(it) }
            if (song == null) {
                navController.popBackStack()
            } else {
                DetailScreen(
                    song = song,
                    isFavorite = song.id in favorites,
                    onBack = { navController.popBackStack() },
                    onToggleFavorite = { appState.toggleFavorite(song.id) },
                    onPlayStage = { navController.navigate(Rotas.STAGE) },
                )
            }
        }

        composable(Rotas.SETLIST) {
            SetlistScreen(onBack = { navController.popBackStack() })
        }

        composable(Rotas.STAGE) {
            StageScreen(onBack = { navController.popBackStack() })
        }

        composable(Rotas.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
