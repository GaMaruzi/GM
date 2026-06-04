package com.gamaruzi.cifras.ui.detail

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamaruzi.cifras.data.Folder
import com.gamaruzi.cifras.data.Line
import com.gamaruzi.cifras.data.PdfPageRenderer
import com.gamaruzi.cifras.data.Section
import com.gamaruzi.cifras.data.Song
import com.gamaruzi.cifras.data.SongFormat
import com.gamaruzi.cifras.domain.Theory
import com.gamaruzi.cifras.ui.common.entityColorByKey
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun DetailScreen(
    song: Song,
    isFavorite: Boolean,
    folders: List<Folder>,
    initialScrollOffset: Int,
    initialSemis: Int,
    onScrollPersist: (Int) -> Unit,
    onSemisChange: (Int) -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: (String, String) -> Unit,
    onMove: (String?) -> Unit,
    onDelete: () -> Unit,
    onPlayStage: () -> Unit,
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var dialogRenomear by remember { mutableStateOf(false) }
    var dialogMover by remember { mutableStateOf(false) }
    var dialogExcluir by remember { mutableStateOf(false) }
    var fontSize by remember { mutableIntStateOf(16) }
    // Tom pré-definido vem do estado salvo da cifra. Mudanças aqui são
    // persistidas via onSemisChange (debounce não necessário — stepper só
    // dispara um evento por clique).
    var semis by remember(song.id, initialSemis) { mutableIntStateOf(initialSemis) }
    LaunchedEffect(semis) { onSemisChange(semis) }
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
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Mais opções")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Renomear") },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                onClick = { menuExpanded = false; dialogRenomear = true },
                            )
                            DropdownMenuItem(
                                text = { Text("Mover para pasta") },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null)
                                },
                                onClick = { menuExpanded = false; dialogMover = true },
                            )
                            DropdownMenuItem(
                                text = { Text("Compartilhar") },
                                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    compartilharSong(context, song)
                                },
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
                                onClick = { menuExpanded = false; dialogExcluir = true },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        Column(modifier = Modifier.fillMaxSize()) {

            if (controlesAtivos) {
                // Altura fixa de 48dp pra Row inteira: o botão de reset
                // (IconButton 40dp) é renderizado num slot de tamanho
                // constante. Quando semis == 0 ele fica invisível mas o
                // espaço continua reservado — assim aumentar/diminuir o
                // tom não causa "respiração" no layout.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StepperBox {
                        Stepper(Icons.Filled.Remove, "Tom abaixo") { semis-- }
                        // Largura mínima fixa pro display do tom: acordes
                        // como "F#" ou "Bb" não vão expandir o stepper.
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .width(28.dp),
                            contentAlignment = Alignment.Center,
                        ) {
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
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = 40.dp, height = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (semis != 0) {
                            IconButton(
                                onClick = { semis = 0 },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = "Voltar ao tom original",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
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
                Spacer(Modifier.height(8.dp))
            }

            when (song.format) {
                SongFormat.TEXT -> TextContent(song, fontSize.sp, semis, prefereFlat, scrollState)
                SongFormat.IMAGE -> ImageContent(song, scrollState)
                SongFormat.PDF -> PdfContent(song, scrollState)
            }
        }

        // Botão "Ir ao palco" centralizado, sobreposto ao conteúdo. Mesmo
        // ícone (Mic) usado no RepertoireEditor pra consistência visual.
        FilledTonalButton(
            onClick = onPlayStage,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        ) {
            Icon(
                Icons.Filled.Mic,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text("Ir ao palco", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
        }
    }

    if (dialogRenomear) {
        val artistaInicial = if (song.artist == "—") "" else song.artist
        RenomearSongDialog(
            nomeInicial = song.title,
            artistaInicial = artistaInicial,
            extensao = song.ext,
            onDismiss = { dialogRenomear = false },
            onConfirm = { nome, artista ->
                onRename(nome, artista)
                dialogRenomear = false
            },
        )
    }

    if (dialogMover) {
        MoverSongDialog(
            song = song,
            folders = folders,
            onDismiss = { dialogMover = false },
            onMover = { folderId ->
                onMove(folderId)
                dialogMover = false
            },
        )
    }

    if (dialogExcluir) {
        AlertDialog(
            onDismissRequest = { dialogExcluir = false },
            title = { Text("Excluir da biblioteca?") },
            text = {
                Text(
                    "\"${song.title}\" será removida do Tap Cifras. O arquivo original no celular não é apagado.",
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogExcluir = false
                    onDelete()
                }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogExcluir = false }) { Text("Cancelar") }
            },
        )
    }
}

private fun compartilharSong(context: android.content.Context, song: Song) {
    runCatching {
        val mime = when (song.format) {
            SongFormat.TEXT -> "text/plain"
            SongFormat.PDF -> "application/pdf"
            SongFormat.IMAGE -> "image/*"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, Uri.parse(song.id))
            putExtra(Intent.EXTRA_SUBJECT, song.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar cifra"))
    }
}

@Composable
private fun RenomearSongDialog(
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
            TextButton(onClick = { onConfirm(nomeLimpo, artistaLimpo) }, enabled = mudou) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun MoverSongDialog(
    song: Song,
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onMover: (String?) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mover \"${song.title}\"") },
        text = {
            Column {
                Surface(
                    onClick = { onMover(null) },
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Home, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.size(12.dp))
                        Text("Raiz (biblioteca)", fontSize = 15.sp, modifier = Modifier.weight(1f))
                        if (song.folderId == null) {
                            Icon(Icons.Filled.Check, contentDescription = "Atual",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                folders.forEach { folder ->
                    val cor = entityColorByKey(folder.color)
                    Surface(
                        onClick = { onMover(folder.id) },
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Folder, contentDescription = null,
                                tint = cor.container)
                            Spacer(Modifier.size(12.dp))
                            Text(
                                folder.name, fontSize = 15.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                            if (song.folderId == folder.id) {
                                Icon(Icons.Filled.Check, contentDescription = "Atual",
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                if (folders.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Você ainda não criou nenhuma pasta. Crie pela tela inicial " +
                            "(botão +) pra organizar suas cifras.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
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
    var scale by remember(song.id) { mutableFloatStateOf(1f) }
    // Centralizada vertical+horizontalmente em 1x. Quando amplia, o
    // verticalScroll exterior permite percorrer a imagem.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = Uri.parse(song.id),
                contentDescription = song.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(song.id) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                        }
                    }
                    .graphicsLayer(scaleX = scale, scaleY = scale),
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
        var scale by remember(song.id) { mutableFloatStateOf(1f) }
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
                        .aspectRatio(bmp.width.toFloat() / bmp.height.toFloat())
                        .pointerInput(song.id) {
                            detectTransformGestures { _, _, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                            }
                        }
                        .graphicsLayer(scaleX = scale, scaleY = scale),
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
