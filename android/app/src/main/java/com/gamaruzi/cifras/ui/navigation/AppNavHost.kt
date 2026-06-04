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
import com.gamaruzi.cifras.ui.repertoires.AddSongsScreen
import com.gamaruzi.cifras.ui.repertoires.RepertoireEditorScreen
import com.gamaruzi.cifras.ui.repertoires.RepertoiresScreen
import com.gamaruzi.cifras.ui.search.SearchScreen
import com.gamaruzi.cifras.ui.search.juntarNomeEArtista
import com.gamaruzi.cifras.ui.settings.SettingsScreen
import com.gamaruzi.cifras.ui.stage.StageDefaults
import com.gamaruzi.cifras.ui.stage.StageIntroScreen
import com.gamaruzi.cifras.ui.stage.StageScreen

object Rotas {
    const val EMPTY = "empty"
    const val SEARCH = "search"
    const val DETAIL = "detail/{songId}"
    const val REPERTOIRES = "repertoires"
    const val REPERTOIRE_EDITOR = "repertoire/{repId}"
    const val ADD_SONGS = "repertoire/{repId}/add"
    // Stage aceita repId OU songId — params opcionais via query string.
    // Com repId: toca o repertório inteiro. Com songId: modo single-song.
    const val STAGE = "stage?rep={repId}&song={songId}"
    const val STAGE_INTRO = "stage_intro?rep={repId}&song={songId}"
    const val SETTINGS = "settings"

    // Uri.encode codifica `/` `:` `%` e demais reservados. O Navigation Compose
    // decodifica via Uri.decode no caminho oposto — só uma camada de encode,
    // sem URLEncoder/URLDecoder em cima.
    fun detail(songId: String) = "detail/" + android.net.Uri.encode(songId)
    fun repertoireEditor(repId: String) = "repertoire/" + android.net.Uri.encode(repId)
    fun addSongs(repId: String) = "repertoire/" + android.net.Uri.encode(repId) + "/add"
    fun stageRep(repId: String) = "stage?rep=" + android.net.Uri.encode(repId) + "&song="
    fun stageSong(songId: String) = "stage?rep=&song=" + android.net.Uri.encode(songId)
    fun stageIntroRep(repId: String) = "stage_intro?rep=" + android.net.Uri.encode(repId) + "&song="
    fun stageIntroSong(songId: String) = "stage_intro?rep=&song=" + android.net.Uri.encode(songId)
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
    val folders by appState.folders.collectAsStateWithLifecycle()
    val sortModes by appState.sortModes.collectAsStateWithLifecycle()
    val scrollOffsets by appState.scrollOffsets.collectAsStateWithLifecycle()
    val cifraSemis by appState.cifraSemis.collectAsStateWithLifecycle()
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
                folders = folders,
                sortModes = sortModes,
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
                onCreateFolder = { nome, cor -> appState.createFolder(nome, cor) },
                onRenameFolder = { id, nome, cor -> appState.renameFolder(id, nome, cor) },
                onDeleteFolder = appState::deleteFolder,
                onMoveToFolder = appState::moveToFolder,
                onSortModeChange = appState::setSortMode,
                onStartStage = { navController.navigate(Rotas.REPERTOIRES) },
            )
        }

        composable(
            route = Rotas.STAGE_INTRO,
            arguments = listOf(
                navArgument("repId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("songId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val repId = entry.arguments?.getString("repId")?.takeIf { it.isNotEmpty() }
            val songIdArg = entry.arguments?.getString("songId")?.takeIf { it.isNotEmpty() }
            StageIntroScreen(
                onBack = { navController.popBackStack() },
                onStart = {
                    val rota = when {
                        songIdArg != null -> Rotas.stageSong(songIdArg)
                        repId != null -> Rotas.stageRep(repId)
                        else -> return@StageIntroScreen
                    }
                    navController.navigate(rota) {
                        // popUpTo intro inclusive: back do palco volta ao
                        // editor/detail, não passa de novo pela intro.
                        popUpTo(Rotas.STAGE_INTRO) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Rotas.DETAIL,
            arguments = listOf(navArgument("songId") { type = NavType.StringType }),
        ) { backStackEntry ->
            // Navigation Compose já decodifica o argumento via Uri.decode antes
            // de entregar — não aplicar URLDecoder em cima (perde %XX preservados).
            val songId = backStackEntry.arguments?.getString("songId")
            val song = songId?.let { appState.song(it) }
            if (song == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                DetailScreen(
                    song = song,
                    isFavorite = song.id in favorites,
                    folders = folders,
                    initialScrollOffset = scrollOffsets[song.id] ?: 0,
                    initialSemis = cifraSemis[song.id] ?: 0,
                    onScrollPersist = { offset -> appState.saveScrollOffset(song.id, offset) },
                    onSemisChange = { s -> appState.setCifraSemis(song.id, s) },
                    onBack = { navController.popBackStack() },
                    onToggleFavorite = { appState.toggleFavorite(song.id) },
                    onRename = { nome, artista ->
                        appState.renameEntry(song.id, juntarNomeEArtista(nome, artista))
                    },
                    onMove = { folderId -> appState.moveToFolder(song.id, folderId) },
                    onDelete = {
                        appState.removeFromLibrary(song.id)
                        navController.popBackStack()
                    },
                    onPlayStage = {
                        navController.navigate(Rotas.stageIntroSong(song.id))
                    },
                )
            }
        }

        composable(Rotas.REPERTOIRES) {
            RepertoiresScreen(
                appState = appState,
                onBack = { navController.popBackStack() },
                onOpenRepertoire = { id ->
                    navController.navigate(Rotas.repertoireEditor(id))
                },
            )
        }

        composable(
            route = Rotas.REPERTOIRE_EDITOR,
            arguments = listOf(navArgument("repId") { type = NavType.StringType }),
        ) { entry ->
            val repId = entry.arguments?.getString("repId") ?: return@composable
            RepertoireEditorScreen(
                appState = appState,
                repertoireId = repId,
                onBack = { navController.popBackStack() },
                onAddSongs = { navController.navigate(Rotas.addSongs(repId)) },
                onStartStage = {
                    navController.navigate(Rotas.stageIntroRep(repId))
                },
            )
        }

        composable(
            route = Rotas.ADD_SONGS,
            arguments = listOf(navArgument("repId") { type = NavType.StringType }),
        ) { entry ->
            val repId = entry.arguments?.getString("repId") ?: return@composable
            AddSongsScreen(
                appState = appState,
                repertoireId = repId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Rotas.STAGE,
            arguments = listOf(
                navArgument("repId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("songId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val repId = entry.arguments?.getString("repId")?.takeIf { it.isNotEmpty() }
            val songIdArg = entry.arguments?.getString("songId")?.takeIf { it.isNotEmpty() }
            val musicas = remember(repId, songIdArg, songs) {
                when {
                    songIdArg != null -> songs.firstOrNull { it.id == songIdArg }?.let { listOf(it) }.orEmpty()
                    repId != null -> appState.repertoire(repId)?.songIds
                        ?.mapNotNull { id -> songs.firstOrNull { it.id == id } }.orEmpty()
                    else -> emptyList()
                }
            }
            val rep = repId?.let { appState.repertoire(it) }
            val defaults = rep?.let {
                StageDefaults(
                    repId = it.id,
                    textZoom = it.defaultTextZoom,
                    imageZoom = it.defaultImageZoom,
                    scrollSpeed = it.defaultScrollSpeed,
                )
            }
            val speeds by appState.speeds.collectAsStateWithLifecycle()
            val semisAll by appState.cifraSemis.collectAsStateWithLifecycle()
            val zoomAll by appState.cifraZoom.collectAsStateWithLifecycle()
            StageScreen(
                musicas = musicas,
                speeds = speeds,
                cifraSemis = semisAll,
                cifraZoom = zoomAll,
                repertoireDefaults = defaults,
                onBack = { navController.popBackStack() },
                onPersistRepertoireDefaults = { rid, tx, img, sp ->
                    appState.setRepertoireDefaults(rid, tx, img, sp)
                },
                onSpeedChange = { songId, px -> appState.setSpeed(songId, px) },
                onSemisChange = { songId, s -> appState.setCifraSemis(songId, s) },
                onZoomChange = { songId, v -> appState.setCifraZoom(songId, v) },
                onShowEnded = {
                    // Fim de repertório: volta direto pra lista de repertórios
                    // (Editor já foi popado quando o intro saiu do back stack).
                    navController.popBackStack(Rotas.REPERTOIRES, inclusive = false)
                },
            )
        }

        composable(Rotas.SETTINGS) {
            SettingsScreen(appState = appState, onBack = { navController.popBackStack() })
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
