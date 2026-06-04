package com.gamaruzi.cifras.ui.repertoires

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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamaruzi.cifras.data.Song
import com.gamaruzi.cifras.data.SongFormat
import com.gamaruzi.cifras.ui.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsScreen(
    appState: AppState,
    repertoireId: String,
    onBack: () -> Unit,
) {
    val songs by appState.songs.collectAsStateWithLifecycle()
    val repertoires by appState.repertoires.collectAsStateWithLifecycle()
    val folders by appState.folders.collectAsStateWithLifecycle()

    val rep = repertoires.firstOrNull { it.id == repertoireId }
    if (rep == null) {
        onBack()
        return
    }

    var query by remember { mutableStateOf("") }
    // null = "Todas as pastas", "raiz" = filtra só raiz
    var filtroPasta by remember { mutableStateOf<String?>(null) }
    val PASTA_RAIZ = "__raiz__"
    var selecionadas by remember { mutableStateOf(emptySet<String>()) }

    val candidatas = remember(songs, rep.songIds) {
        songs.filter { it.id !in rep.songIds }
    }

    val visiveis = remember(candidatas, query, filtroPasta) {
        val porPasta = when (filtroPasta) {
            null -> candidatas
            PASTA_RAIZ -> candidatas.filter { it.folderId == null }
            else -> candidatas.filter { it.folderId == filtroPasta }
        }
        val q = query.trim().lowercase()
        if (q.isEmpty()) porPasta
        else porPasta.filter { (it.file + " " + it.title + " " + it.artist).lowercase().contains(q) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Adicionar a \"${rep.name}\"",
                            fontSize = 18.sp, fontWeight = FontWeight.Medium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            if (selecionadas.isEmpty()) "Toque para selecionar"
                            else "${selecionadas.size} selecionada${if (selecionadas.size > 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        if (selecionadas.isNotEmpty()) {
                            appState.addSongsToRepertoire(rep.id, selecionadas.toList())
                            onBack()
                        }
                    },
                    enabled = selecionadas.isNotEmpty(),
                    modifier = Modifier.padding(end = 16.dp),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        if (selecionadas.isEmpty()) "Adicionar"
                        else "Adicionar (${selecionadas.size})",
                    )
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar nas suas cifras", fontSize = 16.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Limpar")
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                )
            }

            // Filtro por pasta — "Todas" + "Raiz" + lista de pastas existentes
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filtroPasta == null,
                    onClick = { filtroPasta = null },
                    label = { Text("Todas") },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(),
                )
                FilterChip(
                    selected = filtroPasta == PASTA_RAIZ,
                    onClick = { filtroPasta = if (filtroPasta == PASTA_RAIZ) null else PASTA_RAIZ },
                    label = { Text("Raiz") },
                    shape = RoundedCornerShape(8.dp),
                )
                folders.forEach { folder ->
                    FilterChip(
                        selected = filtroPasta == folder.id,
                        onClick = {
                            filtroPasta = if (filtroPasta == folder.id) null else folder.id
                        },
                        label = { Text(folder.name, maxLines = 1) },
                        leadingIcon = {
                            Icon(Icons.Filled.Folder, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                        },
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            }

            if (visiveis.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        when {
                            candidatas.isEmpty() -> "Toda a biblioteca já está neste repertório."
                            query.isNotEmpty() -> "Nada encontrado para \"$query\"."
                            else -> "Nenhuma cifra nesta pasta."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
                    items(visiveis, key = { it.id }) { song ->
                        SongCheckItem(
                            song = song,
                            selecionada = song.id in selecionadas,
                            onToggle = {
                                selecionadas = if (song.id in selecionadas)
                                    selecionadas - song.id
                                else selecionadas + song.id
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongCheckItem(
    song: Song,
    selecionada: Boolean,
    onToggle: () -> Unit,
) {
    // Row inteiro clicável via toggleable — não precisa mirar o checkbox.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = selecionada,
                onValueChange = { onToggle() },
                role = Role.Checkbox,
            )
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = selecionada, onCheckedChange = null)
        Spacer(Modifier.size(8.dp))
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
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title, fontSize = 15.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                song.artist, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun iconeDoFormato(format: SongFormat): ImageVector = when (format) {
    SongFormat.TEXT -> Icons.Filled.Description
    SongFormat.IMAGE -> Icons.Filled.Image
    SongFormat.PDF -> Icons.Filled.PictureAsPdf
}
