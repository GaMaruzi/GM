package com.gamaruzi.cifras.ui.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamaruzi.cifras.data.Song
import com.gamaruzi.cifras.data.SongFormat
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
    snackbarHostState: SnackbarHostState,
    onOpenSong: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onAddImages: () -> Unit,
    onAddDocs: () -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onStartStage: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(SearchTab.TODAS) }
    var sheetAberta by remember { mutableStateOf(false) }
    var songParaRenomear by remember { mutableStateOf<Song?>(null) }
    var songParaExcluir by remember { mutableStateOf<Song?>(null) }

    val resultados = remember(songs, favorites, recents, query, tab) {
        val base = when (tab) {
            SearchTab.TODAS -> songs
            SearchTab.FAVORITAS -> songs.filter { it.id in favorites }
            SearchTab.RECENTES -> recents.mapNotNull { id -> songs.find { it.id == id } }
        }
        val q = query.trim().lowercase()
        if (q.isEmpty()) base
        else base.filter { (it.file + " " + it.genre).lowercase().contains(q) }
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
                    Icons.Filled.PlayCircle,
                    contentDescription = "Tocar no palco",
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
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

            // Status da biblioteca
            Row(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 6.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(7.dp))
                Text(
                    text = "Sua biblioteca · $bibliotecaSize ${if (bibliotecaSize == 1) "cifra" else "cifras"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Filter chips
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
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

            if (loading && songs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (resultados.isEmpty()) {
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
                    items(resultados, key = { it.id }) { song ->
                        SongItem(
                            song = song,
                            isFavorite = song.id in favorites,
                            onClick = { onOpenSong(song.id) },
                            onToggleFav = { onToggleFavorite(song.id) },
                            onRename = { songParaRenomear = song },
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
            onDismiss = { sheetAberta = false },
        )
    }

    songParaRenomear?.let { song ->
        RenomearDialog(
            nomeAtual = song.file,
            titulo = song.title,
            onDismiss = { songParaRenomear = null },
            onConfirm = { novoNome ->
                onRename(song.id, novoNome)
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
private fun SongItem(
    song: Song,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFav: () -> Unit,
    onRename: () -> Unit,
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
                Text(
                    text = subtitulo(song),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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

@Composable
private fun RenomearDialog(
    nomeAtual: String,
    titulo: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var texto by remember(nomeAtual) { mutableStateOf(nomeAtual) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renomear cifra") },
        text = {
            Column {
                Text(
                    "Mude como esta cifra aparece no app. O arquivo original não é alterado.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Dica: use o formato \"Título - Artista.ext\" pra preencher os dois campos.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(texto) },
                enabled = texto.isNotBlank() && texto != nomeAtual,
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
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
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

private fun iconeDoFormato(format: SongFormat): ImageVector = when (format) {
    SongFormat.TEXT -> Icons.Filled.Description
    SongFormat.IMAGE -> Icons.Filled.Image
    SongFormat.PDF -> Icons.Filled.PictureAsPdf
}

private fun subtitulo(song: Song): String = when (song.format) {
    SongFormat.TEXT -> "${song.artist} · ${song.ext.uppercase()} · tom ${song.key}"
    SongFormat.IMAGE, SongFormat.PDF -> "${song.artist} · ${song.ext.uppercase()}"
}
