package com.gamaruzi.cifras.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

    fun detail(songId: String) = "detail/" + java.net.URLEncoder.encode(songId, "UTF-8")
}

@Composable
fun AppNavHost(appState: AppState = viewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val library by appState.library.collectAsStateWithLifecycle()
    val songs by appState.songs.collectAsStateWithLifecycle()
    val loading by appState.loading.collectAsStateWithLifecycle()
    val favorites by appState.favorites.collectAsStateWithLifecycle()
    val recents by appState.recents.collectAsStateWithLifecycle()
    val lastAddResult by appState.lastAddResult.collectAsStateWithLifecycle()

    // Photo Picker do sistema (API agnóstica a versão do Android; Google fornece
    // backport via Play Services no Android 11-12 e usa o picker nativo em 13+).
    // Não exige permissão READ_MEDIA_IMAGES — funciona em sandbox.
    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        if (uris.isNotEmpty()) {
            appState.addUris(uris)
            navegarPraBiblioteca(navController)
        }
    }

    // OpenDocument para PDF e TXT — janela do sistema permite ao usuário
    // navegar em Downloads, Documents, Drive, etc.
    val pickDocsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            appState.addUris(uris)
            navegarPraBiblioteca(navController)
        }
    }

    // Feedback de adição em lote (qts entraram, qts foram ignoradas).
    LaunchedEffect(lastAddResult) {
        val r = lastAddResult ?: return@LaunchedEffect
        val partes = buildList {
            if (r.adicionadas > 0) add("${r.adicionadas} adicionada(s)")
            if (r.ignoradasPorTamanho > 0) add("${r.ignoradasPorTamanho} grande(s) demais")
            if (r.ignoradasPorFormato > 0) add("${r.ignoradasPorFormato} sem suporte")
        }
        if (partes.isNotEmpty()) snackbarHostState.showSnackbar(partes.joinToString(" · "))
        appState.consumeAddResult()
    }

    val startDestination = if (library.isEmpty()) Rotas.EMPTY else Rotas.SEARCH

    LaunchedEffect(library.isEmpty()) {
        // Quando a biblioteca esvazia (usuário removeu tudo), volta pra Empty.
        if (library.isEmpty() && navController.currentDestination?.route != Rotas.EMPTY) {
            navController.navigate(Rotas.EMPTY) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Rotas.EMPTY) {
            EmptyScreen(
                onPickImages = {
                    pickImagesLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onPickDocs = {
                    pickDocsLauncher.launch(arrayOf("application/pdf", "text/plain"))
                },
            )
        }

        composable(Rotas.SEARCH) {
            SearchScreen(
                bibliotecaSize = library.size,
                songs = songs,
                loading = loading,
                favorites = favorites,
                recents = recents,
                snackbarHostState = snackbarHostState,
                onOpenSong = { id ->
                    appState.markRecent(id)
                    navController.navigate(Rotas.detail(id))
                },
                onToggleFavorite = appState::toggleFavorite,
                onOpenSettings = { navController.navigate(Rotas.SETTINGS) },
                onAddImages = {
                    pickImagesLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onAddDocs = {
                    pickDocsLauncher.launch(arrayOf("application/pdf", "text/plain"))
                },
                onRename = appState::renameEntry,
                onDelete = appState::removeFromLibrary,
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

private fun navegarPraBiblioteca(navController: androidx.navigation.NavController) {
    if (navController.currentDestination?.route != Rotas.SEARCH) {
        navController.navigate(Rotas.SEARCH) {
            popUpTo(Rotas.EMPTY) { inclusive = true }
            launchSingleTop = true
        }
    }
}
