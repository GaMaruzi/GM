package com.gamaruzi.cifras.ui.stage

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.gamaruzi.cifras.data.Line
import com.gamaruzi.cifras.data.PdfPageRenderer
import com.gamaruzi.cifras.data.Song
import com.gamaruzi.cifras.data.SongFormat
import com.gamaruzi.cifras.ui.common.findActivity
import kotlinx.coroutines.delay

// Paleta fixa do palco (não depende do tema do app — preto OLED + verde Spotify).
// Spec: docs/design/v2-verde-icone-paleta/README.md
private val StageBg = Color(0xFF000000)
private val StageFg = Color(0xFFF2F2F2)
private val StageFgDim = Color(0xFF8A8A8A)
private val StageAccent = Color(0xFF1ED760)

// Stepper de fonte segue spec: 28sp padrão, ajustável 18-44sp.
private const val FONT_MIN = 18
private const val FONT_MAX = 44
private const val FONT_DEFAULT = 28

// Quanto tempo o chrome (TopBar + BottomBar + dots) fica visível antes de
// desaparecer automaticamente. Spec do roadmap.
private const val CHROME_AUTO_HIDE_MS = 3200L

// Overlay com "1× próxima · 2× anterior". Some sozinho um pouco depois.
private const val DICA_INICIAL_MS = 5000L

@Composable
fun StageScreen(
    musicas: List<Song>,
    speeds: Map<String, Int>,
    onBack: () -> Unit,
) {
    SetupStageWindow()

    Box(modifier = Modifier.fillMaxSize().background(StageBg)) {
        if (musicas.isEmpty()) {
            StageVazio(onBack = onBack)
        } else {
            StagePalco(musicas = musicas, speeds = speeds, onBack = onBack)
        }
    }
}

@Composable
private fun StagePalco(
    musicas: List<Song>,
    speeds: Map<String, Int>,
    onBack: () -> Unit,
) {
    var indice by remember { mutableIntStateOf(0) }
    var fontSize by remember { mutableIntStateOf(FONT_DEFAULT) }
    var chromeVisivel by remember { mutableStateOf(true) }
    var dicaVisivel by remember { mutableStateOf(true) }

    val songAtual = musicas[indice.coerceIn(0, musicas.size - 1)]
    val velocidade = speeds[songAtual.id] ?: 0

    // Reseta scroll ao trocar de música. ScrollState é compartilhado entre
    // os 3 renderizadores para o auto-scroll funcionar uniformemente.
    val scrollState = rememberScrollState()
    LaunchedEffect(songAtual.id) {
        scrollState.scrollTo(0)
        // Cada nova música re-mostra o chrome por 3.2s — dá chance pro
        // usuário ajustar fonte ou sair sem precisar de um gesto especial.
        chromeVisivel = true
    }

    // Auto-hide do chrome: depois de CHROME_AUTO_HIDE_MS sem mudança, esconde.
    LaunchedEffect(chromeVisivel, songAtual.id) {
        if (!chromeVisivel) return@LaunchedEffect
        delay(CHROME_AUTO_HIDE_MS)
        chromeVisivel = false
    }

    // Auto-hide da dica inicial.
    LaunchedEffect(Unit) {
        delay(DICA_INICIAL_MS)
        dicaVisivel = false
    }

    // Auto-scroll: rola `velocidade` pixels por segundo, linear, até o fim.
    // Cancela e reinicia quando trocar de música ou velocidade.
    LaunchedEffect(songAtual.id, velocidade, scrollState.maxValue) {
        if (velocidade <= 0) return@LaunchedEffect
        // Aguarda o conteúdo medir (maxValue > 0) — PDF e imagem podem
        // levar alguns frames pra carregar.
        if (scrollState.maxValue <= 0) return@LaunchedEffect
        while (scrollState.value < scrollState.maxValue) {
            scrollState.animateScrollBy(
                value = velocidade.toFloat(),
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // detectTapGestures não consome eventos de drag → o scroll vertical
            // continua funcionando dentro do conteúdo. Janela de double-tap
            // ~280ms é o padrão do gesture detector.
            .pointerInput(musicas.size) {
                detectTapGestures(
                    onTap = {
                        chromeVisivel = true
                        if (indice < musicas.size - 1) {
                            indice++
                        }
                    },
                    onDoubleTap = {
                        chromeVisivel = true
                        if (indice > 0) {
                            indice--
                        }
                    },
                )
            },
    ) {
        // Conteúdo principal
        when (songAtual.format) {
            SongFormat.TEXT -> StageText(songAtual, fontSize.sp, scrollState)
            SongFormat.IMAGE -> StageImage(songAtual, scrollState)
            SongFormat.PDF -> StagePdf(songAtual, scrollState)
        }

        // Chrome (TopBar + BottomBar + dots) com fade
        AnimatedVisibility(visible = chromeVisivel, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize()) {
                StageTopBar(
                    song = songAtual,
                    indice = indice + 1,
                    total = musicas.size,
                    velocidade = velocidade,
                    onBack = onBack,
                )
                StageBottomBar(
                    indice = indice,
                    total = musicas.size,
                    fontPodeReduzir = fontSize > FONT_MIN,
                    fontPodeAumentar = fontSize < FONT_MAX,
                    onFontMinus = {
                        fontSize = (fontSize - 2).coerceAtLeast(FONT_MIN)
                        chromeVisivel = true   // dá mais 3.2s pra outro ajuste
                    },
                    onFontPlus = {
                        fontSize = (fontSize + 2).coerceAtMost(FONT_MAX)
                        chromeVisivel = true
                    },
                )
            }
        }

        // Overlay de dica inicial
        AnimatedVisibility(
            visible = dicaVisivel,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            DicaInicial(onDismiss = { dicaVisivel = false })
        }
    }
}

@Composable
private fun StageVazio(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Sem músicas pra tocar",
                color = StageFg,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Volte e adicione cifras antes de tocar.",
                color = StageFgDim,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(24.dp))
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(StageAccent),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Sair do palco", tint = StageBg)
            }
        }
    }
}

@Composable
private fun StageTopBar(
    song: Song,
    indice: Int,
    total: Int,
    velocidade: Int,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StageBg.copy(alpha = 0.85f))
            .padding(start = 8.dp, end = 16.dp, top = 36.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.Close, contentDescription = "Sair do palco", tint = StageFg)
        }
        Spacer(Modifier.size(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                color = StageFg,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                if (velocidade > 0) "${song.artist} · auto-scroll ${velocidade}px/s"
                else song.artist,
                color = StageFgDim,
                fontSize = 11.sp,
                maxLines = 1,
            )
        }
        Text(
            "$indice / $total",
            color = StageFgDim,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun BoxScope.StageBottomBar(
    indice: Int,
    total: Int,
    fontPodeReduzir: Boolean,
    fontPodeAumentar: Boolean,
    onFontMinus: () -> Unit,
    onFontPlus: () -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(StageBg.copy(alpha = 0.85f))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Dots de progresso do setlist
        DotsProgresso(indice = indice, total = total)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StageActionButton(label = "A−", enabled = fontPodeReduzir, onClick = onFontMinus)
            Spacer(Modifier.size(12.dp))
            StageActionButton(label = "A+", enabled = fontPodeAumentar, onClick = onFontPlus)
        }
    }
}

@Composable
private fun DotsProgresso(indice: Int, total: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val ativo = i == indice
            val visto = i < indice
            Box(
                modifier = Modifier
                    .size(if (ativo) 9.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            ativo -> StageAccent
                            visto -> StageFgDim
                            else -> StageFg.copy(alpha = 0.18f)
                        }
                    ),
            )
        }
    }
}

@Composable
private fun StageActionButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (enabled) StageFg.copy(alpha = 0.18f) else StageFg.copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxSize()) {
            Text(
                label,
                color = if (enabled) StageFg else StageFgDim,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DicaInicial(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(StageBg.copy(alpha = 0.85f))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Toque única vez para a próxima",
                color = StageFg,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Toque duas vezes para voltar",
                color = StageFg,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Toque aqui pra fechar",
                color = StageFgDim,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun StageText(song: Song, fontSize: TextUnit, scrollState: ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 96.dp),
    ) {
        song.sections.forEachIndexed { index, section ->
            if (index > 0) Spacer(Modifier.height(28.dp))
            if (section.tag.isNotBlank()) {
                Text(
                    section.tag.uppercase(),
                    color = StageFgDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(8.dp))
            }
            section.lines.forEach { line -> StageLine(line, fontSize) }
        }
    }
}

@Composable
private fun StageLine(line: Line, fontSize: TextUnit) {
    if (line.chords.isNotBlank()) {
        Text(
            text = line.chords,
            color = StageAccent,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
    }
    if (line.lyrics.isNotBlank()) {
        Text(
            text = line.lyrics,
            color = StageFg,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun StageImage(song: Song, scrollState: ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 80.dp),
    ) {
        AsyncImage(
            model = Uri.parse(song.id),
            contentDescription = song.title,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StagePdf(song: Song, scrollState: ScrollState) {
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
            CircularProgressIndicator(color = StageAccent)
        }
    } else if (rendered.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("PDF vazio.", color = StageFgDim)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rendered.forEach { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(bmp.width.toFloat() / bmp.height.toFloat()),
                )
            }
        }
    }
}

// Configura janela para modo "palco": esconde system bars, mantém a tela
// sempre acesa. Restaura ao sair (onDispose).
@Composable
private fun SetupStageWindow() {
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()
    DisposableEffect(Unit) {
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        // BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE — o usuário pode arrastar do
        // topo/baixo pra ver as bars temporariamente, sem sair do imersivo.
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        view.keepScreenOn = true
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            view.keepScreenOn = false
        }
    }
}

