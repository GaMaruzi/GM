package com.gamaruzi.cifras.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

object Rotas {
    const val EMPTY = "empty"
    const val SEARCH = "search"
    const val DETAIL = "detail/{songId}"
    const val SETLIST = "setlist"
    const val STAGE = "stage"
    const val SETTINGS = "settings"

    fun detail(songId: String) = "detail/" + java.net.URLEncoder.encode(songId, "UTF-8")
}

@Composable
fun AppNavHost(appState: AppState = viewModel()) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val folder by appState.folderName.collectAsStateWithLifecycle()
    val songs by appState.songs.collectAsStateWithLifecycle()
    val favorites by appState.favorites.collectAsStateWithLifecycle()
    val recents by appState.recents.collectAsStateWithLifecycle()

    // SAF folder picker. Resultado pode vir null se o usuário cancelar.
    val pickFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            appState.setFolder(uri)
            // Garante que estamos na Search após escolher (vindos do Empty).
            navController.navigate(Rotas.SEARCH) {
                popUpTo(Rotas.EMPTY) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Se a pasta sumiu (URI revogada, pasta deletada etc.) volta pro Empty.
    LaunchedEffect(folder) {
        if (folder == null && navController.currentDestination?.route != Rotas.EMPTY) {
            navController.navigate(Rotas.EMPTY) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val startDestination = if (folder == null) Rotas.EMPTY else Rotas.SEARCH

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Rotas.EMPTY) {
            EmptyScreen(onPickFolder = { pickFolderLauncher.launch(null) })
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
                onChangeFolder = { pickFolderLauncher.launch(null) },
                onStartStage = { navController.navigate(Rotas.SETLIST) },
            )
        }

        composable(
            route = Rotas.DETAIL,
            arguments = listOf(navArgument("songId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("songId")
            val songId = encoded?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            val song = songId?.let { appState.song(it) }
            if (song == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                // Carrega o conteúdo bruto do arquivo (sem parser por enquanto).
                var rawContent by remember(song.id) { mutableStateOf<String?>(null) }
                LaunchedEffect(song.id) {
                    if (song.sections.isEmpty()) {
                        rawContent = appState.readSongContent(song.id)
                    }
                }
                DetailScreen(
                    song = song,
                    rawContent = rawContent,
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
