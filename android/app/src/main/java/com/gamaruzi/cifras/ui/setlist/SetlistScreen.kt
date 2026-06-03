package com.gamaruzi.cifras.ui.setlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamaruzi.cifras.data.Song
import com.gamaruzi.cifras.data.SongFormat
import com.gamaruzi.cifras.ui.AppState

// Passos de velocidade em px/segundo. 0 = sem auto-scroll. Range pensado pra
// cifras de letra média/grande em fonte normal.
private const val SPEED_STEP = 10
private const val SPEED_MAX = 120

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistScreen(
    appState: AppState,
    onBack: () -> Unit,
    onStartStage: () -> Unit,
) {
    val songs by appState.songs.collectAsStateWithLifecycle()
    val setlist by appState.setlist.collectAsStateWithLifecycle()
    val speeds by appState.speeds.collectAsStateWithLifecycle()
    var mostrarCandidatas by remember { mutableStateOf(false) }
    var confirmarLimpar by remember { mutableStateOf(false) }

    // Resolvemos as Songs em ordem do setlist. Itens sem match (URI excluída
    // antes do prune passar) caem fora silenciosamente.
    val itensSetlist = remember(setlist, songs) {
        setlist.mapNotNull { uri -> songs.firstOrNull { it.id == uri } }
    }
    val candidatas = remember(songs, setlist) {
        songs.filter { it.id !in setlist }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Setlist",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            if (setlist.isEmpty()) "Vazio" else "${setlist.size} música(s)",
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
                actions = {
                    if (setlist.isNotEmpty()) {
                        IconButton(onClick = { confirmarLimpar = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Limpar setlist")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartStage,
                icon = { Icon(Icons.Filled.Fullscreen, contentDescription = null) },
                text = { Text("Iniciar palco · ${setlist.size}") },
                expanded = setlist.isNotEmpty(),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (itensSetlist.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Fullscreen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Monte o setlist do seu show.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Adicione cifras da sua biblioteca e ajuste a velocidade de cada uma.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 40.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp),
                ) {
                    itemsIndexed(itensSetlist, key = { _, s -> s.id }) { index, song ->
                        SetlistItem(
                            posicao = index + 1,
                            song = song,
                            velocidade = speeds[song.id] ?: 0,
                            podeSubir = index > 0,
                            podeDescer = index < itensSetlist.size - 1,
                            onSubir = { appState.moveSetlistUp(index) },
                            onDescer = { appState.moveSetlistDown(index) },
                            onRemover = { appState.removeFromSetlist(song.id) },
                            onDiminuirVel = {
                                val nova = ((speeds[song.id] ?: 0) - SPEED_STEP).coerceAtLeast(0)
                                appState.setSpeed(song.id, nova)
                            },
                            onAumentarVel = {
                                val nova = ((speeds[song.id] ?: 0) + SPEED_STEP).coerceAtMost(SPEED_MAX)
                                appState.setSpeed(song.id, nova)
                            },
                        )
                    }
                }
            }

            Surface(
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = { mostrarCandidatas = true },
                    enabled = candidatas.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        if (candidatas.isEmpty()) "Toda a biblioteca já está no setlist"
                        else "Adicionar cifras (${candidatas.size} disponíveis)",
                    )
                }
            }
        }
    }

    if (mostrarCandidatas) {
        DialogCandidatas(
            candidatas = candidatas,
            onDismiss = { mostrarCandidatas = false },
            onAdicionar = { id -> appState.addToSetlist(id) },
        )
    }

    if (confirmarLimpar) {
        AlertDialog(
            onDismissRequest = { confirmarLimpar = false },
            title = { Text("Limpar setlist?") },
            text = {
                Text(
                    "Todas as músicas e velocidades do setlist serão removidas. " +
                        "A biblioteca não é afetada.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    appState.clearSetlist()
                    confirmarLimpar = false
                }) { Text("Limpar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpar = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun SetlistItem(
    posicao: Int,
    song: Song,
    velocidade: Int,
    podeSubir: Boolean,
    podeDescer: Boolean,
    onSubir: () -> Unit,
    onDescer: () -> Unit,
    onRemover: () -> Unit,
    onDiminuirVel: () -> Unit,
    onAumentarVel: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$posicao",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.size(10.dp))
                Icon(
                    iconeDoFormato(song.format),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        song.title,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        song.artist,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(
                    onClick = onSubir,
                    enabled = podeSubir,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Filled.ArrowDropUp,
                        contentDescription = "Mover para cima",
                        modifier = Modifier.size(28.dp),
                    )
                }
                IconButton(
                    onClick = onDescer,
                    enabled = podeDescer,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Mover para baixo",
                        modifier = Modifier.size(28.dp),
                    )
                }
                IconButton(onClick = onRemover, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Remover do setlist",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Velocidade",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onDiminuirVel,
                    enabled = velocidade > 0,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Diminuir velocidade",
                        modifier = Modifier.size(18.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .heightIn(min = 28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (velocidade == 0) "off" else "$velocidade",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (velocidade == 0) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
                IconButton(
                    onClick = onAumentarVel,
                    enabled = velocidade < SPEED_MAX,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Aumentar velocidade",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogCandidatas(
    candidatas: List<Song>,
    onDismiss: () -> Unit,
    onAdicionar: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar ao setlist") },
        text = {
            if (candidatas.isEmpty()) {
                Text("Nada disponível.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    itemsIndexed(candidatas, key = { _, s -> s.id }) { _, song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                iconeDoFormato(song.format),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.size(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    song.title,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    song.artist,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(onClick = { onAdicionar(song.id) }) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Adicionar ${song.title}",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Pronto") }
        },
    )
}

private fun iconeDoFormato(format: SongFormat): ImageVector = when (format) {
    SongFormat.TEXT -> Icons.Filled.Description
    SongFormat.IMAGE -> Icons.Filled.Image
    SongFormat.PDF -> Icons.Filled.PictureAsPdf
}
