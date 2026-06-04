package com.gamaruzi.cifras.ui.repertoires

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
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
import com.gamaruzi.cifras.domain.Theory
import com.gamaruzi.cifras.ui.AppState
import androidx.compose.ui.text.font.FontFamily

private const val SPEED_STEP = 10
private const val SPEED_MAX = 120

// Faixas/passos por formato pra os steppers de pré-config:
private const val FONT_MIN = 16
private const val FONT_MAX = 44
private const val FONT_STEP = 2
private const val FONT_DEFAULT = 18

private const val SCALE_MIN = 100
private const val SCALE_MAX = 400
private const val SCALE_STEP = 50
private const val SCALE_DEFAULT = 100

private const val SEMIS_MIN = -11
private const val SEMIS_MAX = 11

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepertoireEditorScreen(
    appState: AppState,
    repertoireId: String,
    onBack: () -> Unit,
    onAddSongs: () -> Unit,
    onStartStage: () -> Unit,
) {
    val songs by appState.songs.collectAsStateWithLifecycle()
    val repertoires by appState.repertoires.collectAsStateWithLifecycle()
    val speeds by appState.speeds.collectAsStateWithLifecycle()
    val cifraSemis by appState.cifraSemis.collectAsStateWithLifecycle()
    val cifraZoom by appState.cifraZoom.collectAsStateWithLifecycle()

    val rep = repertoires.firstOrNull { it.id == repertoireId }

    // Se o repertório some (foi excluído enquanto a tela tava aberta), volta.
    LaunchedEffect(rep == null) {
        if (rep == null) onBack()
    }
    if (rep == null) return

    var confirmarLimpar by remember { mutableStateOf(false) }

    val itensRep = remember(rep.songIds, songs) {
        rep.songIds.mapNotNull { id -> songs.firstOrNull { it.id == id } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(rep.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text(
                            if (rep.songIds.isEmpty()) "Vazio" else "${rep.songIds.size} cifra(s)",
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
                    if (rep.songIds.isNotEmpty()) {
                        IconButton(onClick = { confirmarLimpar = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Esvaziar repertório")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (itensRep.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Repertório vazio",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Adicione cifras pra montar o show.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onAddSongs,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Adicionar cifras", fontSize = 14.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp),
                ) {
                    itemsIndexed(itensRep, key = { _, s -> s.id }) { index, song ->
                        val zoomDefault = if (song.format == SongFormat.TEXT) FONT_DEFAULT else SCALE_DEFAULT
                        val zoomAtual = cifraZoom[song.id] ?: zoomDefault
                        val semisAtual = cifraSemis[song.id] ?: 0
                        RepertoireItem(
                            posicao = index + 1,
                            song = song,
                            velocidade = speeds[song.id] ?: 0,
                            zoom = zoomAtual,
                            semis = semisAtual,
                            podeSubir = index > 0,
                            podeDescer = index < itensRep.size - 1,
                            onSubir = { appState.moveRepertoireSongUp(rep.id, index) },
                            onDescer = { appState.moveRepertoireSongDown(rep.id, index) },
                            onRemover = { appState.removeSongFromRepertoire(rep.id, song.id) },
                            onDiminuirVel = {
                                val nova = ((speeds[song.id] ?: 0) - SPEED_STEP).coerceAtLeast(0)
                                appState.setSpeed(song.id, nova)
                            },
                            onAumentarVel = {
                                val nova = ((speeds[song.id] ?: 0) + SPEED_STEP).coerceAtMost(SPEED_MAX)
                                appState.setSpeed(song.id, nova)
                            },
                            onDiminuirZoom = {
                                val passo = if (song.format == SongFormat.TEXT) FONT_STEP else SCALE_STEP
                                val min = if (song.format == SongFormat.TEXT) FONT_MIN else SCALE_MIN
                                val novo = (zoomAtual - passo).coerceAtLeast(min)
                                appState.setCifraZoom(song.id, novo)
                            },
                            onAumentarZoom = {
                                val passo = if (song.format == SongFormat.TEXT) FONT_STEP else SCALE_STEP
                                val max = if (song.format == SongFormat.TEXT) FONT_MAX else SCALE_MAX
                                val novo = (zoomAtual + passo).coerceAtMost(max)
                                appState.setCifraZoom(song.id, novo)
                            },
                            onDiminuirSemis = {
                                appState.setCifraSemis(song.id, (semisAtual - 1).coerceAtLeast(SEMIS_MIN))
                            },
                            onAumentarSemis = {
                                appState.setCifraSemis(song.id, (semisAtual + 1).coerceAtMost(SEMIS_MAX))
                            },
                            onResetSemis = { appState.setCifraSemis(song.id, 0) },
                        )
                    }
                }

                // Botões inline no fim do conteúdo, mais perto do dedo (já não
                // grudados na borda como FAB/bottomBar). Tamanho compacto,
                // cantos pouco arredondados (12dp) — visual coerente com a
                // tela "Repertórios" anterior.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onAddSongs,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Adicionar cifras", fontSize = 14.sp)
                    }
                    Button(
                        onClick = onStartStage,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            Icons.Filled.Festival,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("Ir ao palco", fontSize = 14.sp)
                    }
                }
            }
        }
    }

    if (confirmarLimpar) {
        AlertDialog(
            onDismissRequest = { confirmarLimpar = false },
            title = { Text("Esvaziar \"${rep.name}\"?") },
            text = {
                Text(
                    "Todas as cifras serão removidas deste repertório. A biblioteca não é afetada.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarLimpar = false
                    // remove uma a uma; é uma lista curta na prática
                    rep.songIds.forEach { id -> appState.removeSongFromRepertoire(rep.id, id) }
                }) { Text("Esvaziar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpar = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun RepertoireItem(
    posicao: Int,
    song: Song,
    velocidade: Int,
    zoom: Int,
    semis: Int,
    podeSubir: Boolean,
    podeDescer: Boolean,
    onSubir: () -> Unit,
    onDescer: () -> Unit,
    onRemover: () -> Unit,
    onDiminuirVel: () -> Unit,
    onAumentarVel: () -> Unit,
    onDiminuirZoom: () -> Unit,
    onAumentarZoom: () -> Unit,
    onDiminuirSemis: () -> Unit,
    onAumentarSemis: () -> Unit,
    onResetSemis: () -> Unit,
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
                        song.title, fontSize = 15.sp, maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        song.artist, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onSubir, enabled = podeSubir, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.ArrowDropUp, contentDescription = "Mover para cima",
                        modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = onDescer, enabled = podeDescer, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Mover para baixo",
                        modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = onRemover, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Close, contentDescription = "Remover do repertório",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            // Controles condicionais por formato:
            //  TEXT  → Fonte + Velocidade + Tom (3 linhas)
            //  PDF   → Zoom + Velocidade (2)
            //  IMAGE → Zoom (1)
            when (song.format) {
                SongFormat.TEXT -> {
                    StepperLinha(
                        label = "Fonte",
                        valor = "${zoom}sp",
                        valorAtivo = zoom != FONT_DEFAULT,
                        podeMenos = zoom > FONT_MIN,
                        podeMais = zoom < FONT_MAX,
                        onMenos = onDiminuirZoom,
                        onMais = onAumentarZoom,
                    )
                    Spacer(Modifier.height(4.dp))
                    StepperLinha(
                        label = "Velocidade",
                        valor = if (velocidade == 0) "off" else "$velocidade",
                        valorAtivo = velocidade > 0,
                        podeMenos = velocidade > 0,
                        podeMais = velocidade < SPEED_MAX,
                        onMenos = onDiminuirVel,
                        onMais = onAumentarVel,
                    )
                    Spacer(Modifier.height(4.dp))
                    TomLinha(
                        keyOriginal = song.key,
                        semis = semis,
                        podeMenos = semis > SEMIS_MIN,
                        podeMais = semis < SEMIS_MAX,
                        onMenos = onDiminuirSemis,
                        onMais = onAumentarSemis,
                        onReset = onResetSemis,
                    )
                }
                SongFormat.PDF -> {
                    StepperLinha(
                        label = "Zoom",
                        valor = "${zoom}%",
                        valorAtivo = zoom != SCALE_DEFAULT,
                        podeMenos = zoom > SCALE_MIN,
                        podeMais = zoom < SCALE_MAX,
                        onMenos = onDiminuirZoom,
                        onMais = onAumentarZoom,
                    )
                    Spacer(Modifier.height(4.dp))
                    StepperLinha(
                        label = "Velocidade",
                        valor = if (velocidade == 0) "off" else "$velocidade",
                        valorAtivo = velocidade > 0,
                        podeMenos = velocidade > 0,
                        podeMais = velocidade < SPEED_MAX,
                        onMenos = onDiminuirVel,
                        onMais = onAumentarVel,
                    )
                }
                SongFormat.IMAGE -> {
                    StepperLinha(
                        label = "Zoom",
                        valor = "${zoom}%",
                        valorAtivo = zoom != SCALE_DEFAULT,
                        podeMenos = zoom > SCALE_MIN,
                        podeMais = zoom < SCALE_MAX,
                        onMenos = onDiminuirZoom,
                        onMais = onAumentarZoom,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperLinha(
    label: String,
    valor: String,
    valorAtivo: Boolean,
    podeMenos: Boolean,
    podeMais: Boolean,
    onMenos: () -> Unit,
    onMais: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            label, fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onMenos, enabled = podeMenos, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = "Diminuir $label",
                modifier = Modifier.size(18.dp))
        }
        Box(
            modifier = Modifier.padding(horizontal = 4.dp).heightIn(min = 28.dp).widthIn(min = 52.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                valor,
                fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = if (valorAtivo) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
        IconButton(onClick = onMais, enabled = podeMais, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar $label",
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TomLinha(
    keyOriginal: String,
    semis: Int,
    podeMenos: Boolean,
    podeMais: Boolean,
    onMenos: () -> Unit,
    onMais: () -> Unit,
    onReset: () -> Unit,
) {
    val prefereFlat = remember(keyOriginal) { Theory.keyPrefersFlat(keyOriginal) }
    val tomExibicao = remember(keyOriginal, semis, prefereFlat) {
        Theory.transposeKey(keyOriginal, semis, prefereFlat)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            "Tom", fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onMenos, enabled = podeMenos, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = "Tom abaixo",
                modifier = Modifier.size(18.dp))
        }
        Box(
            modifier = Modifier.padding(horizontal = 4.dp).heightIn(min = 28.dp).widthIn(min = 44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                tomExibicao,
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (semis != 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
        IconButton(onClick = onMais, enabled = podeMais, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Tom acima",
                modifier = Modifier.size(18.dp))
        }
        // Slot fixo pra reset — só visível quando há transposição. Mantém
        // o layout estável quando o usuário ajusta o tom.
        Box(
            modifier = Modifier.size(width = 32.dp, height = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (semis != 0) {
                IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Voltar ao tom original",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

private fun iconeDoFormato(format: SongFormat): ImageVector = when (format) {
    SongFormat.TEXT -> Icons.Filled.Description
    SongFormat.IMAGE -> Icons.Filled.Image
    SongFormat.PDF -> Icons.Filled.PictureAsPdf
}
