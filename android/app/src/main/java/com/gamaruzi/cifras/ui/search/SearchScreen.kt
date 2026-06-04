package com.gamaruzi.cifras.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamaruzi.cifras.data.Folder
import com.gamaruzi.cifras.data.Song
import com.gamaruzi.cifras.data.SongFormat
import com.gamaruzi.cifras.ui.common.DEFAULT_ENTITY_COLOR_KEY
import com.gamaruzi.cifras.ui.common.EntityColorPalette
import com.gamaruzi.cifras.ui.common.entityColorByKey
import kotlinx.coroutines.launch

enum class SearchTab { TODAS, FAVORITAS, RECENTES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    bibliotecaSize: Int,
    songs: List<Song>,
    loading: Boolean,
    favorites: Set<String>,
    recents: List<String>,
    folders: List<Folder>,
    sortModes: Map<SearchTab, SortMode>,
    snackbarHostState: SnackbarHostState,
    onOpenSong: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onAddImages: () -> Unit,
    onAddDocs: () -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onCreateFolder: (String, String) -> Unit,
    onRenameFolder: (String, String, String?) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onMoveToFolder: (String, String?) -> Unit,
    onSortModeChange: (SearchTab, SortMode) -> Unit,
    onStartStage: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(SearchTab.TODAS) }
    var pastaAtualId by remember { mutableStateOf<String?>(null) }
    var sheetAberta by remember { mutableStateOf(false) }
    var dialogNovaPasta by remember { mutableStateOf(false) }
    var pastaParaRenomear by remember { mutableStateOf<Folder?>(null) }
    var pastaParaExcluir by remember { mutableStateOf<Folder?>(null) }
    var songParaRenomear by remember { mutableStateOf<Song?>(null) }
    var songParaExcluir by remember { mutableStateOf<Song?>(null) }
    var songParaMover by remember { mutableStateOf<Song?>(null) }

    // Quando a pasta atual deixa de existir (ex: foi removida), volta pra raiz.
    val pastaAtual = folders.firstOrNull { it.id == pastaAtualId }
    if (pastaAtualId != null && pastaAtual == null) {
        pastaAtualId = null
    }

    val buscando = query.trim().isNotEmpty()
    val sortAtual = SortModeCodec.forTab(sortModes, tab)

    val resultados = remember(songs, favorites, recents, query, tab, pastaAtualId, buscando, sortAtual) {
        // Escopo da listagem por aba:
        // - TODAS: dentro da pasta atual (ou raiz = sem pasta).
        // - FAVORITAS/RECENTES: na raiz, globais (inclusive cifras dentro
        //   de pastas) — caso contrário um favorito dentro de pasta sumiria
        //   sem o usuário entender por quê. Dentro de uma pasta, restringe
        //   ao escopo da pasta.
        // Busca textual ignora pasta — é global.
        val noEscopo = when {
            buscando -> songs
            tab == SearchTab.TODAS -> songs.filter { it.folderId == pastaAtualId }
            pastaAtualId != null -> songs.filter { it.folderId == pastaAtualId }
            else -> songs
        }
        val base = when (tab) {
            SearchTab.TODAS -> noEscopo
            SearchTab.FAVORITAS -> noEscopo.filter { it.id in favorites }
            SearchTab.RECENTES -> recents.mapNotNull { id -> noEscopo.find { it.id == id } }
        }
        val q = query.trim().lowercase()
        val filtrados = if (q.isEmpty()) base
        else base.filter { (it.file + " " + it.genre).lowercase().contains(q) }

        // RECENTES preserva ordem de recência; demais aplicam o sort.
        if (tab == SearchTab.RECENTES) {
            filtrados
        } else {
            sortSongs(filtrados, sortAtual)
        }
    }

    // Subpastas a mostrar no topo da lista quando estamos na raiz, sem busca e tab=TODAS.
    val cifrasPorPasta = remember(songs) { songs.groupBy { it.folderId }.mapValues { it.value.size } }
    val pastasMap = remember(folders) { folders.associateBy { it.id } }
    val mostrarPastas = !buscando && pastaAtualId == null && tab == SearchTab.TODAS
    val pastasOrdenadas = remember(folders, cifrasPorPasta, sortAtual, mostrarPastas) {
        if (!mostrarPastas) emptyList()
        else sortFolders(folders, cifrasPorPasta, sortAtual)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartStage,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "Ir ao palco",
                    modifier = Modifier.size(26.dp),
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar M3 (docked, 56dp, surfaceContainerHigh)
            Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 4.dp)) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar nas suas cifras", fontSize = 16.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Limpar")
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { sheetAberta = true }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Adicionar cifras")
                                }
                                IconButton(onClick = onOpenSettings) {
                                    Icon(Icons.Filled.Settings, contentDescription = "Configurações")
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                )
            }

            // Cabeçalho de localização: "Biblioteca" na raiz, ou breadcrumb com
            // a pasta atual (clicar no Home volta; ⋮ permite renomear/excluir).
            val totalNoEscopo = if (buscando) bibliotecaSize else (cifrasPorPasta[pastaAtualId] ?: 0)
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pastaAtual != null) {
                    val corPasta = entityColorByKey(pastaAtual.color).container
                    IconButton(
                        onClick = { pastaAtualId = null },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar para a biblioteca",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.size(4.dp))
                    Icon(
                        Icons.Filled.Folder,
                        contentDescription = null,
                        tint = corPasta,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(7.dp))
                    Text(
                        text = pastaAtual.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "$totalNoEscopo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.size(4.dp))
                    PastaOverflow(
                        onRenomear = { pastaParaRenomear = pastaAtual },
                        onExcluir = { pastaParaExcluir = pastaAtual },
                    )
                } else {
                    Spacer(Modifier.size(8.dp))
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Biblioteca · $bibliotecaSize ${if (bibliotecaSize == 1) "cifra" else "cifras"}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Filter chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Chip("Todas", tab == SearchTab.TODAS) { tab = SearchTab.TODAS }
                Chip("Favoritas", tab == SearchTab.FAVORITAS, leading = Icons.Filled.Star) {
                    tab = SearchTab.FAVORITAS
                }
                Chip("Recentes", tab == SearchTab.RECENTES, leading = Icons.Filled.History) {
                    tab = SearchTab.RECENTES
                }
            }

            // Controle de ordenação (some na aba RECENTES: ordem é por recência).
            if (tab != SearchTab.RECENTES) {
                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                    SortDropdown(
                        atual = sortAtual,
                        // Em FAVORITAS lista plana de cifras: esconde a opção
                        // "Mais cifras" que só faz sentido pra pastas.
                        permitirQuantidade = tab == SearchTab.TODAS && pastaAtualId == null,
                        onSelect = { onSortModeChange(tab, it) },
                    )
                }
            }

            val mostrarEmpty = resultados.isEmpty() && pastasOrdenadas.isEmpty()

            if (loading && songs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (mostrarEmpty) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Column(
                        modifier = Modifier.padding(top = 56.dp, start = 40.dp, end = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(44.dp),
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = when {
                                query.isNotEmpty() -> "Nada encontrado para \"$query\"."
                                tab == SearchTab.FAVORITAS -> "Nenhuma favorita ainda. Toque na ★ de uma cifra."
                                tab == SearchTab.RECENTES -> "Nenhuma cifra recente ainda."
                                songs.isEmpty() -> "Biblioteca vazia. Use o menu para adicionar cifras."
                                else -> "Nada por aqui ainda."
                            },
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    if (pastasOrdenadas.isNotEmpty()) {
                        items(pastasOrdenadas, key = { "folder-" + it.id }) { folder ->
                            PastaItem(
                                folder = folder,
                                contagem = cifrasPorPasta[folder.id] ?: 0,
                                onClick = { pastaAtualId = folder.id },
                                onRenomear = { pastaParaRenomear = folder },
                                onExcluir = { pastaParaExcluir = folder },
                            )
                        }
                    }
                    items(resultados, key = { it.id }) { song ->
                        val pastaDaSong = song.folderId?.let { pastasMap[it] }
                        // Mostra chip de pasta quando o escopo não é "dentro
                        // dela": busca global, ou Favoritas/Recentes na raiz
                        // mostrando cifras de várias pastas.
                        val mostrarChipPasta = pastaDaSong != null && when {
                            buscando -> true
                            pastaAtualId == null && tab != SearchTab.TODAS -> true
                            else -> false
                        }
                        SongItem(
                            song = song,
                            isFavorite = song.id in favorites,
                            pastaInfo = if (mostrarChipPasta) pastaDaSong else null,
                            onClick = { onOpenSong(song.id) },
                            onToggleFav = { onToggleFavorite(song.id) },
                            onRename = { songParaRenomear = song },
                            onMove = { songParaMover = song },
                            onDelete = { songParaExcluir = song },
                        )
                    }
                }
            }
        }
    }

    if (sheetAberta) {
        AdicionarBottomSheet(
            onAddImages = { sheetAberta = false; onAddImages() },
            onAddDocs = { sheetAberta = false; onAddDocs() },
            onNovaPasta = { sheetAberta = false; dialogNovaPasta = true },
            onDismiss = { sheetAberta = false },
        )
    }

    if (dialogNovaPasta) {
        PastaDialog(
            titulo = "Nova pasta",
            nomeInicial = "",
            corInicial = DEFAULT_ENTITY_COLOR_KEY,
            confirmLabel = "Criar",
            onDismiss = { dialogNovaPasta = false },
            onConfirm = { nome, cor ->
                onCreateFolder(nome, cor)
                dialogNovaPasta = false
            },
        )
    }

    pastaParaRenomear?.let { folder ->
        PastaDialog(
            titulo = "Renomear pasta",
            nomeInicial = folder.name,
            corInicial = folder.color,
            confirmLabel = "Salvar",
            onDismiss = { pastaParaRenomear = null },
            onConfirm = { novoNome, novaCor ->
                onRenameFolder(folder.id, novoNome, novaCor)
                pastaParaRenomear = null
            },
        )
    }

    pastaParaExcluir?.let { folder ->
        ExcluirPastaDialog(
            folder = folder,
            cifrasNaPasta = cifrasPorPasta[folder.id] ?: 0,
            onDismiss = { pastaParaExcluir = null },
            onConfirm = {
                if (pastaAtualId == folder.id) pastaAtualId = null
                onDeleteFolder(folder.id)
                pastaParaExcluir = null
            },
        )
    }

    songParaMover?.let { song ->
        MoverParaPastaDialog(
            song = song,
            folders = folders,
            onDismiss = { songParaMover = null },
            onMover = { folderId ->
                onMoveToFolder(song.id, folderId)
                songParaMover = null
            },
            onCriarNova = {
                songParaMover = null
                dialogNovaPasta = true
            },
        )
    }

    songParaRenomear?.let { song ->
        // Reconstrói "Título" e "Artista" pra editar separadamente.
        val nomeInicial = song.title
        val artistaInicial = if (song.artist == "—") "" else song.artist
        RenomearDialog(
            nomeInicial = nomeInicial,
            artistaInicial = artistaInicial,
            extensao = song.ext,
            onDismiss = { songParaRenomear = null },
            onConfirm = { nome, artista ->
                onRename(song.id, juntarNomeEArtista(nome, artista))
                songParaRenomear = null
            },
        )
    }

    songParaExcluir?.let { song ->
        ExcluirDialog(
            song = song,
            onDismiss = { songParaExcluir = null },
            onConfirm = {
                onDelete(song.id)
                songParaExcluir = null
            },
        )
    }
}

// Aplica o SortMode em uma lista de pastas considerando a contagem de cifras.
internal fun sortFolders(
    folders: List<Folder>,
    cifrasPorPasta: Map<String?, Int>,
    sort: SortMode,
): List<Folder> = when (sort) {
    SortMode.ALFABETICA_ASC -> folders.sortedBy { it.name.lowercase() }
    SortMode.ALFABETICA_DESC -> folders.sortedByDescending { it.name.lowercase() }
    SortMode.QUANTIDADE_DESC -> folders.sortedWith(
        compareByDescending<Folder> { cifrasPorPasta[it.id] ?: 0 }.thenBy { it.name.lowercase() }
    )
    SortMode.QUANTIDADE_ASC -> folders.sortedWith(
        compareBy<Folder> { cifrasPorPasta[it.id] ?: 0 }.thenBy { it.name.lowercase() }
    )
}

// Aplica o SortMode em uma lista de cifras. Modos de quantidade caem em
// alfabética, já que "tamanho" só faz sentido em pastas.
internal fun sortSongs(songs: List<Song>, sort: SortMode): List<Song> = when (sort) {
    SortMode.ALFABETICA_ASC,
    SortMode.QUANTIDADE_ASC -> songs.sortedBy { it.file.lowercase() }
    SortMode.ALFABETICA_DESC -> songs.sortedByDescending { it.file.lowercase() }
    SortMode.QUANTIDADE_DESC -> songs.sortedBy { it.file.lowercase() }
}

// Junta "Nome" + "Artista" no formato canônico que o Song.parseTitleArtist
// já entende. Vazio em artista mantém só o nome (sem traço).
internal fun juntarNomeEArtista(nome: String, artista: String): String {
    val n = nome.trim()
    val a = artista.trim()
    return if (a.isEmpty()) n else "$n - $a"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Chip(
    label: String,
    selected: Boolean,
    leading: ImageVector? = null,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = leading?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp)) } },
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(),
    )
}

@Composable
private fun SortDropdown(
    atual: SortMode,
    permitirQuantidade: Boolean,
    onSelect: (SortMode) -> Unit,
) {
    var aberto by remember { mutableStateOf(false) }
    val opcoes = SortMode.entries.filter { mode ->
        when (mode) {
            SortMode.QUANTIDADE_DESC, SortMode.QUANTIDADE_ASC -> permitirQuantidade
            else -> true
        }
    }
    Surface(
        onClick = { aberto = true },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                "Ordenar: ${atual.label}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
    DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
        opcoes.forEach { mode ->
            DropdownMenuItem(
                text = { Text(mode.label) },
                trailingIcon = if (mode == atual) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null,
                onClick = { aberto = false; onSelect(mode) },
            )
        }
    }
}

@Composable
private fun SongItem(
    song: Song,
    isFavorite: Boolean,
    pastaInfo: Folder?,
    onClick: () -> Unit,
    onToggleFav: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
) {
    var overflowExpanded by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    iconeDoFormato(song.format),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subtitulo(song),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (pastaInfo != null) {
                        Spacer(Modifier.size(8.dp))
                        ChipDePasta(pastaInfo)
                    }
                }
            }
            IconButton(
                onClick = onToggleFav,
                modifier = Modifier.size(44.dp).clip(CircleShape),
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "Desfavoritar" else "Favoritar",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
            Box {
                IconButton(
                    onClick = { overflowExpanded = true },
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Mais ações",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Renomear") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { overflowExpanded = false; onRename() },
                    )
                    DropdownMenuItem(
                        text = { Text("Mover para pasta") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null) },
                        onClick = { overflowExpanded = false; onMove() },
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = { overflowExpanded = false; onDelete() },
                    )
                }
            }
        }
    }
}

// Diálogo de renomear com dois campos (Nome e Artista) e dica embaixo.
// Quando Artista vem vazio, o resultado é só o Nome — preserva o fluxo
// de quem não quer separar autor.
@Composable
private fun RenomearDialog(
    nomeInicial: String,
    artistaInicial: String,
    extensao: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var nome by remember(nomeInicial) {
        mutableStateOf(TextFieldValue(nomeInicial, TextRange(0, nomeInicial.length)))
    }
    var artista by remember(artistaInicial) {
        mutableStateOf(TextFieldValue(artistaInicial))
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val sufixoExt = if (extensao.isNotBlank()) "." + extensao.lowercase() else ""
    val nomeLimpo = nome.text.trim()
    val artistaLimpo = artista.text.trim()
    val mudou = nomeLimpo.isNotEmpty() &&
        (nomeLimpo != nomeInicial || artistaLimpo != artistaInicial)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renomear cifra") },
        text = {
            Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da música") },
                    singleLine = true,
                    trailingIcon = if (sufixoExt.isNotEmpty()) {
                        {
                            Text(
                                sufixoExt,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 12.dp),
                            )
                        }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = artista,
                    onValueChange = { artista = it },
                    label = { Text("Artista (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Dica: o nome aparece em destaque e o artista numa linha menor. " +
                        "O arquivo original não é alterado.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nomeLimpo, artistaLimpo) },
                enabled = mudou,
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdicionarBottomSheet(
    onAddImages: () -> Unit,
    onAddDocs: () -> Unit,
    onNovaPasta: () -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun fechar(acao: () -> Unit) {
        scope.launch { state.hide() }.invokeOnCompletion {
            if (!state.isVisible) acao()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                "Adicionar à biblioteca",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 12.dp),
            )
            Surface(
                onClick = { fechar(onAddImages) },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Filled.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    headlineContent = { Text("Imagem", fontSize = 16.sp) },
                    supportingContent = {
                        Text(
                            "JPG, PNG, WebP",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            Surface(
                onClick = { fechar(onAddDocs) },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    headlineContent = { Text("PDF ou texto", fontSize = 16.sp) },
                    supportingContent = {
                        Text(
                            "PDF, TXT",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            Surface(
                onClick = { fechar(onNovaPasta) },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Filled.CreateNewFolder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    headlineContent = { Text("Nova pasta", fontSize = 16.sp) },
                    supportingContent = {
                        Text(
                            "Para organizar suas cifras",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun ExcluirDialog(
    song: Song,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir da biblioteca?") },
        text = {
            Text(
                "\"${song.title}\" será removida da biblioteca do Tap Cifras. O arquivo original no celular não é apagado.",
                fontSize = 14.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Excluir", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun PastaItem(
    folder: Folder,
    contagem: Int,
    onClick: () -> Unit,
    onRenomear: () -> Unit,
    onExcluir: () -> Unit,
) {
    val cor = entityColorByKey(folder.color)
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cor.container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Folder,
                    contentDescription = null,
                    tint = cor.onContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$contagem ${if (contagem == 1) "cifra" else "cifras"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PastaOverflow(onRenomear = onRenomear, onExcluir = onExcluir)
        }
    }
}

@Composable
private fun PastaOverflow(
    onRenomear: () -> Unit,
    onExcluir: () -> Unit,
) {
    var aberto by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { aberto = true },
            modifier = Modifier.size(44.dp).clip(CircleShape),
        ) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "Opções da pasta",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            DropdownMenuItem(
                text = { Text("Renomear pasta") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = { aberto = false; onRenomear() },
            )
            DropdownMenuItem(
                text = { Text("Excluir pasta") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = { aberto = false; onExcluir() },
            )
        }
    }
}

@Composable
private fun ChipDePasta(folder: Folder) {
    val cor = entityColorByKey(folder.color)
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            // widthIn limita o crescimento do chip pra não empurrar o
            // nome/artista da cifra. Quando a pasta tem nome longo, o
            // overflow Ellipsis truncа.
            modifier = Modifier
                .widthIn(max = 96.dp)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                tint = cor.container,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = folder.name,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// Diálogo unificado pra criar/renomear pasta: nome + paleta de 8 cores.
// Nome é limitado a FOLDER_NAME_MAX_LEN caracteres pra não estourar visualmente
// na lista, no breadcrumb e no chip de pasta da cifra.
internal const val FOLDER_NAME_MAX_LEN = 20

@Composable
private fun PastaDialog(
    titulo: String,
    nomeInicial: String,
    corInicial: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var texto by remember(nomeInicial) {
        mutableStateOf(TextFieldValue(nomeInicial, TextRange(0, nomeInicial.length)))
    }
    var corSelecionada by remember(corInicial) { mutableStateOf(corInicial) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val textoLimpo = texto.text.trim()
    val excedeu = textoLimpo.length > FOLDER_NAME_MAX_LEN
    val mudouAlgo = textoLimpo.isNotEmpty() && !excedeu &&
        (textoLimpo != nomeInicial || corSelecionada != corInicial)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column {
                OutlinedTextField(
                    value = texto,
                    // Bloqueia entrada acima do limite — não é tradeoff:
                    // o usuário tenta digitar e simplesmente para. Combinado
                    // com supportingText, fica claro o limite.
                    onValueChange = { novo ->
                        if (novo.text.length <= FOLDER_NAME_MAX_LEN) texto = novo
                    },
                    label = { Text("Nome") },
                    singleLine = true,
                    isError = excedeu,
                    supportingText = {
                        Text(
                            "${textoLimpo.length}/$FOLDER_NAME_MAX_LEN caracteres",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Cor",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                PaletaCores(
                    selecionada = corSelecionada,
                    onSelect = { corSelecionada = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(textoLimpo, corSelecionada) },
                enabled = mudouAlgo,
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
internal fun PaletaCores(
    selecionada: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        EntityColorPalette.forEach { cor ->
            val isSelected = cor.key == selecionada
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(cor.container)
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape,
                    )
                    .clickable { onSelect(cor.key) },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = cor.label,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExcluirPastaDialog(
    folder: Folder,
    cifrasNaPasta: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir pasta?") },
        text = {
            Text(
                if (cifrasNaPasta == 0)
                    "\"${folder.name}\" será removida. Como está vazia, nenhuma cifra é afetada."
                else
                    "\"${folder.name}\" será removida. As $cifrasNaPasta cifras que estão " +
                        "dentro voltam para a raiz da biblioteca (nada é apagado).",
                fontSize = 14.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Excluir", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun MoverParaPastaDialog(
    song: Song,
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onMover: (String?) -> Unit,
    onCriarNova: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mover \"${song.title}\"") },
        text = {
            Column {
                // Opção raiz
                Surface(
                    onClick = { onMover(null) },
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.size(12.dp))
                        Text("Raiz (biblioteca)", fontSize = 15.sp, modifier = Modifier.weight(1f))
                        if (song.folderId == null) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Atual",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                folders.forEach { folder ->
                    val cor = entityColorByKey(folder.color)
                    Surface(
                        onClick = { onMover(folder.id) },
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Folder,
                                contentDescription = null,
                                tint = cor.container,
                            )
                            Spacer(Modifier.size(12.dp))
                            Text(
                                folder.name,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (song.folderId == folder.id) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Atual",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Surface(
                    onClick = onCriarNova,
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.CreateNewFolder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            "Criar nova pasta",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

private fun iconeDoFormato(format: SongFormat): ImageVector = when (format) {
    SongFormat.TEXT -> Icons.Filled.Description
    SongFormat.IMAGE -> Icons.Filled.Image
    SongFormat.PDF -> Icons.Filled.PictureAsPdf
}

private fun subtitulo(song: Song): String = when (song.format) {
    SongFormat.TEXT -> "${song.artist} · ${song.ext.uppercase()} · tom ${song.key}"
    SongFormat.IMAGE, SongFormat.PDF -> "${song.artist} · ${song.ext.uppercase()}"
}
