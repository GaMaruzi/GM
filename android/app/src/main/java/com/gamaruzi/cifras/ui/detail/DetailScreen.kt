package com.gamaruzi.cifras.ui.detail

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamaruzi.cifras.data.Line
import com.gamaruzi.cifras.data.PdfPageRenderer
import com.gamaruzi.cifras.data.Section
import com.gamaruzi.cifras.data.Song
import com.gamaruzi.cifras.data.SongFormat
import com.gamaruzi.cifras.domain.Theory
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun DetailScreen(
    song: Song,
    isFavorite: Boolean,
    initialScrollOffset: Int,
    onScrollPersist: (Int) -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayStage: () -> Unit,
) {
    var fontSize by remember { mutableIntStateOf(16) }
    // Reseta a transposição quando troca de música — caso contrário um +3
    // grudado da música anterior se aplicaria à nova sem o usuário saber.
    var semis by remember(song.id) { mutableIntStateOf(0) }
    val controlesAtivos = song.format == SongFormat.TEXT

    // Tom de exibição: já transposto. Preferência por bemóis segue a
    // convenção do tom original — "Bb" prefere bemóis após qualquer
    // transposição; "G" prefere sustenidos. Mantém grafia consistente.
    val prefereFlat = Theory.keyPrefersFlat(song.key)
    val tomExibicao = remember(song.key, semis) {
        Theory.transposeKey(song.key, semis, prefereFlat)
    }

    // ScrollState único compartilhado pelos 3 renderizadores (TEXT/IMAGE/PDF).
    // Recriado quando troca de música, restaurado com o offset persistido.
    val scrollState = rememberScrollState(initial = initialScrollOffset)
    val onScrollPersistAtual by rememberUpdatedState(onScrollPersist)

    // Restaura o scroll quando o conteúdo termina de medir.
    // PdfContent só conhece a altura total após renderizar todas as páginas;
    // sem este efeito o scrollTo do rememberScrollState pode ser truncado
    // contra um maxValue=0 e ficar em 0. Re-aplicar quando maxValue cresce
    // resolve sem heurística de timer.
    LaunchedEffect(song.id, scrollState.maxValue) {
        if (initialScrollOffset > 0 && scrollState.value < initialScrollOffset) {
            scrollState.scrollTo(initialScrollOffset.coerceAtMost(scrollState.maxValue))
        }
    }

    // Persiste o offset com debounce de 500ms. Evita gravar a cada pixel.
    LaunchedEffect(song.id) {
        snapshotFlow { scrollState.value }
            .debounce(500)
            .collect { offset -> onScrollPersistAtual(offset) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                title = {
                    Column {
                        Text(song.title, fontSize = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        Text(
                            song.artist,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Mais opções")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPlayStage,
                icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                text = { Text("Tocar no palco") },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (controlesAtivos) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StepperBox {
                        Stepper(Icons.Filled.Remove, "Tom abaixo") { semis-- }
                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text(
                                text = tomExibicao,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        Stepper(Icons.Filled.Add, "Tom acima") { semis++ }
                    }
                    if (semis != 0) {
                        TextButton(onClick = { semis = 0 }) {
                            Text("↺ original", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    StepperBox {
                        Stepper(Icons.Filled.Remove, "Fonte menor") {
                            fontSize = (fontSize - 1).coerceAtLeast(12)
                        }
                        Icon(
                            Icons.Filled.FormatSize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp).padding(horizontal = 2.dp),
                        )
                        Stepper(Icons.Filled.Add, "Fonte maior") {
                            fontSize = (fontSize + 1).coerceAtMost(28)
                        }
                    }
                }
            }

            when (song.format) {
                SongFormat.TEXT -> TextContent(song, fontSize.sp, semis, prefereFlat, scrollState)
                SongFormat.IMAGE -> ImageContent(song, scrollState)
                SongFormat.PDF -> PdfContent(song, scrollState)
            }
        }
    }
}

@Composable
private fun TextContent(
    song: Song,
    fontSize: TextUnit,
    semis: Int,
    prefereFlat: Boolean,
    scrollState: ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 120.dp),
    ) {
        if (song.capo > 0) {
            CapoChip(song.capo)
            Spacer(Modifier.height(16.dp))
        }

        if (song.sections.isNotEmpty()) {
            SongComSections(song, fontSize, semis, prefereFlat)
        } else {
            Text(
                text = "Não consegui ler o conteúdo deste arquivo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = song.file,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ImageContent(song: Song, scrollState: ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        AsyncImage(
            model = Uri.parse(song.id),
            contentDescription = song.title,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = song.file,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 120.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PdfContent(song: Song, scrollState: ScrollState) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val widthDp = LocalConfiguration.current.screenWidthDp.dp
    val widthPx = with(density) { widthDp.toPx().toInt() }

    var paginas by remember(song.id) { mutableStateOf<List<Bitmap>?>(null) }
    LaunchedEffect(song.id, widthPx) {
        paginas = PdfPageRenderer.render(context, Uri.parse(song.id), widthPx = widthPx)
    }

    val rendered = paginas
    if (rendered == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (rendered.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Não consegui renderizar este PDF.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rendered.forEach { bmp ->
                androidx.compose.foundation.Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(bmp.width.toFloat() / bmp.height.toFloat()),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = song.file,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 120.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SongComSections(song: Song, fontSize: TextUnit, semis: Int, prefereFlat: Boolean) {
    song.sections.forEachIndexed { index, section ->
        if (index > 0) Spacer(Modifier.height(20.dp))
        if (section.tag.isNotBlank()) {
            SectionTag(section.tag)
            Spacer(Modifier.height(6.dp))
        }
        section.lines.forEach { line ->
            // Transpõe in-line. Se semis=0 (caso comum), transposeChordLine
            // devolve a string original sem alocar — barato.
            val linhaRender = if (semis == 0 || line.chords.isBlank()) {
                line
            } else {
                line.copy(chords = Theory.transposeChordLine(line.chords, semis, prefereFlat))
            }
            LineRow(linhaRender, fontSize)
        }
    }
}

@Composable
private fun StepperBox(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.height(40.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { content() }
    }
}

@Composable
private fun Stepper(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
        Icon(icon, contentDescription = desc, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun CapoChip(casa: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            "Capotraste na ${casa}ª casa",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Composable
private fun SectionTag(tag: String) {
    Text(
        text = tag.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LineRow(line: Line, fontSize: TextUnit) {
    if (line.chords.isNotBlank()) {
        Text(
            text = line.chords,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    if (line.lyrics.isNotBlank()) {
        Text(
            text = line.lyrics,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
