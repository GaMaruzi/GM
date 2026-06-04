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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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
import com.gamaruzi.cifras.domain.Theory
import com.gamaruzi.cifras.ui.common.findActivity
import kotlin.math.abs
import kotlinx.coroutines.delay

// Paleta fixa do palco (não depende do tema do app — preto OLED + verde Spotify).
private val StageBg = Color(0xFF000000)
private val StageFg = Color(0xFFF2F2F2)
private val StageFgDim = Color(0xFF8A8A8A)
private val StageAccent = Color(0xFF1ED760)

// Stepper de fonte: range 18–44sp; default vem do repertório (ou 18sp).
private const val FONT_MIN = 18
private const val FONT_MAX = 44
private const val FONT_DEFAULT = 18

// Stepper de velocidade (px/s): mesmas constantes do RepertoireEditorScreen.
private const val SPEED_MIN = 0
private const val SPEED_MAX = 120
private const val SPEED_STEP = 10

private const val CHROME_AUTO_HIDE_MS = 3200L

// Dica de gestos. Posição no terço inferior da tela (não no rodapé puro)
// e tempo confortável de leitura — antes 2.2s era curto demais.
private const val DICA_INICIAL_MS = 4500L

// Distância em pixels que o usuário precisa arrastar antes do gesto contar
// como swipe deliberado (não confundir com micro-movimento de um tap).
private const val SWIPE_THRESHOLD_PX = 80f

// Defaults herdados de um repertório quando o palco é aberto a partir dele.
// Quando null (cifra única), nenhum default é persistido depois.
data class StageDefaults(
    val repId: String,
    val textZoom: Int,
    val imageZoom: Float,
    val scrollSpeed: Int,
)

@Composable
fun StageScreen(
    musicas: List<Song>,
    speeds: Map<String, Int>,
    cifraSemis: Map<String, Int> = emptyMap(),
    repertoireDefaults: StageDefaults? = null,
    onBack: () -> Unit,
    onPersistRepertoireDefaults: (repId: String, textZoom: Int?, imageZoom: Float?, scrollSpeed: Int?) -> Unit = { _, _, _, _ -> },
    onSpeedChange: (songId: String, pxPerSec: Int) -> Unit = { _, _ -> },
    onSemisChange: (songId: String, semis: Int) -> Unit = { _, _ -> },
) {
    SetupStageWindow()

    Box(modifier = Modifier.fillMaxSize().background(StageBg)) {
        if (musicas.isEmpty()) {
            StageVazio(onBack = onBack)
        } else {
            StagePalco(
                musicas = musicas,
                speeds = speeds,
                cifraSemis = cifraSemis,
                repertoireDefaults = repertoireDefaults,
                onBack = onBack,
                onPersistRepertoireDefaults = onPersistRepertoireDefaults,
                onSpeedChange = onSpeedChange,
                onSemisChange = onSemisChange,
            )
        }
    }
}

@Composable
private fun StagePalco(
    musicas: List<Song>,
    speeds: Map<String, Int>,
    cifraSemis: Map<String, Int>,
    repertoireDefaults: StageDefaults?,
    onBack: () -> Unit,
    onPersistRepertoireDefaults: (repId: String, textZoom: Int?, imageZoom: Float?, scrollSpeed: Int?) -> Unit,
    onSpeedChange: (songId: String, pxPerSec: Int) -> Unit,
    onSemisChange: (songId: String, semis: Int) -> Unit,
) {
    var indice by remember { mutableIntStateOf(0) }
    // Inicia com o default do repertório (ou FONT_DEFAULT pra cifra única).
    // O usuário pode ajustar no palco e a alteração é persistida.
    var fontSize by remember {
        mutableIntStateOf(repertoireDefaults?.textZoom ?: FONT_DEFAULT)
    }
    var imageScale by remember(repertoireDefaults?.repId) {
        mutableFloatStateOf(repertoireDefaults?.imageZoom ?: 1f)
    }
    // Velocidade atual aplicada à música corrente. Quando vem do repertório
    // e a cifra não tem speed próprio gravado, usa o default do repertório.
    val songAtual = musicas[indice.coerceIn(0, musicas.size - 1)]
    val velocidade = speeds[songAtual.id] ?: repertoireDefaults?.scrollSpeed ?: 0
    val ehTexto = songAtual.format == SongFormat.TEXT
    // Tom transposto: vem persistido por cifra (pré-definido na home).
    // Mudanças no palco persistem.
    val semis = cifraSemis[songAtual.id] ?: 0
    val prefereFlat = remember(songAtual.id) { Theory.keyPrefersFlat(songAtual.key) }
    val tomExibicao = remember(songAtual.key, semis, prefereFlat) {
        Theory.transposeKey(songAtual.key, semis, prefereFlat)
    }

    var chromeVisivel by remember { mutableStateOf(true) }
    var dicaVisivel by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()
    LaunchedEffect(songAtual.id) {
        scrollState.scrollTo(0)
        // Não mostramos mais chrome ao trocar de música — o usuário pediu
        // fluidez. Chrome só aparece com swipe vertical deliberado.
        chromeVisivel = false
    }

    // Auto-hide do chrome.
    LaunchedEffect(chromeVisivel, songAtual.id) {
        if (!chromeVisivel) return@LaunchedEffect
        delay(CHROME_AUTO_HIDE_MS)
        chromeVisivel = false
    }

    LaunchedEffect(Unit) {
        delay(DICA_INICIAL_MS)
        dicaVisivel = false
    }

    // Auto-scroll: rola `velocidade` pixels por segundo. Cancela quando muda
    // música ou velocidade. Continua mesmo durante chrome visível.
    LaunchedEffect(songAtual.id, velocidade, scrollState.maxValue) {
        if (velocidade <= 0 || scrollState.maxValue <= 0) return@LaunchedEffect
        while (scrollState.value < scrollState.maxValue) {
            scrollState.animateScrollBy(
                value = velocidade.toFloat(),
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
            )
        }
    }

    // Para evitar recriar callbacks em cada recomposição quando os gesture
    // detectors capturam o índice. O `by rememberUpdatedState` mantém os
    // pointerInputs com o índice mais recente.
    val indiceLatest by rememberUpdatedState(indice)
    val musicasSizeLatest by rememberUpdatedState(musicas.size)

    fun avancar() {
        if (indiceLatest < musicasSizeLatest - 1) indice = indiceLatest + 1
    }

    fun voltar() {
        if (indiceLatest > 0) indice = indiceLatest - 1
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Conteúdo principal. `userScrollEnabled = false` no verticalScroll
        // libera o drag vertical pro gesto de mostrar chrome — o auto-scroll
        // programático segue funcionando normalmente.
        when (songAtual.format) {
            SongFormat.TEXT -> StageText(songAtual, fontSize.sp, semis, prefereFlat, scrollState)
            SongFormat.IMAGE -> StageImage(songAtual, scrollState, imageScale) { z ->
                imageScale = z
                if (repertoireDefaults != null) {
                    onPersistRepertoireDefaults(repertoireDefaults.repId, null, z, null)
                }
            }
            SongFormat.PDF -> StagePdf(songAtual, scrollState, imageScale) { z ->
                imageScale = z
                if (repertoireDefaults != null) {
                    onPersistRepertoireDefaults(repertoireDefaults.repId, null, z, null)
                }
            }
        }

        // Camada de gestos: tap = próxima; swipe horizontal direita = anterior;
        // swipe vertical = mostrar chrome. Pinch é capturado no conteúdo via
        // detectTransformGestures (IMG/PDF), independente desta camada.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { avancar() })
                }
                .pointerInput(Unit) {
                    var totalX = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalX = 0f },
                        onDragEnd = {
                            if (totalX > SWIPE_THRESHOLD_PX) voltar()
                            totalX = 0f
                        },
                        onDragCancel = { totalX = 0f },
                        onHorizontalDrag = { _, dx -> totalX += dx },
                    )
                }
                .pointerInput(Unit) {
                    var totalY = 0f
                    detectVerticalDragGestures(
                        onDragStart = { totalY = 0f },
                        onDragEnd = {
                            if (abs(totalY) > SWIPE_THRESHOLD_PX) {
                                chromeVisivel = true
                            }
                            totalY = 0f
                        },
                        onDragCancel = { totalY = 0f },
                        onVerticalDrag = { _, dy -> totalY += dy },
                    )
                },
        )

        // Chrome (TopBar + BottomBar + dots).
        AnimatedVisibility(visible = chromeVisivel, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize()) {
                StageTopBar(
                    song = songAtual,
                    indice = indice + 1,
                    total = musicas.size,
                    velocidade = velocidade,
                    tomExibicao = tomExibicao,
                    semis = semis,
                    onBack = onBack,
                    onTomMinus = {
                        chromeVisivel = true
                        onSemisChange(songAtual.id, semis - 1)
                    },
                    onTomPlus = {
                        chromeVisivel = true
                        onSemisChange(songAtual.id, semis + 1)
                    },
                    onTomReset = {
                        chromeVisivel = true
                        onSemisChange(songAtual.id, 0)
                    },
                )
                StageBottomBar(
                    indice = indice,
                    total = musicas.size,
                    ehTexto = ehTexto,
                    velocidade = velocidade,
                    podeFontMinus = fontSize > FONT_MIN,
                    podeFontPlus = fontSize < FONT_MAX,
                    podeZoomMinus = imageScale > 1f,
                    podeZoomPlus = imageScale < 4f,
                    onFontMinus = {
                        chromeVisivel = true
                        val novo = (fontSize - 2).coerceAtLeast(FONT_MIN)
                        fontSize = novo
                        if (repertoireDefaults != null) {
                            onPersistRepertoireDefaults(repertoireDefaults.repId, novo, null, null)
                        }
                    },
                    onFontPlus = {
                        chromeVisivel = true
                        val novo = (fontSize + 2).coerceAtMost(FONT_MAX)
                        fontSize = novo
                        if (repertoireDefaults != null) {
                            onPersistRepertoireDefaults(repertoireDefaults.repId, novo, null, null)
                        }
                    },
                    onZoomMinus = {
                        chromeVisivel = true
                        val novo = (imageScale - 0.5f).coerceAtLeast(1f)
                        imageScale = novo
                        if (repertoireDefaults != null) {
                            onPersistRepertoireDefaults(repertoireDefaults.repId, null, novo, null)
                        }
                    },
                    onZoomPlus = {
                        chromeVisivel = true
                        val novo = (imageScale + 0.5f).coerceAtMost(4f)
                        imageScale = novo
                        if (repertoireDefaults != null) {
                            onPersistRepertoireDefaults(repertoireDefaults.repId, null, novo, null)
                        }
                    },
                    onSpeedMinus = {
                        chromeVisivel = true
                        val novo = (velocidade - SPEED_STEP).coerceAtLeast(SPEED_MIN)
                        onSpeedChange(songAtual.id, novo)
                        if (repertoireDefaults != null) {
                            onPersistRepertoireDefaults(repertoireDefaults.repId, null, null, novo)
                        }
                    },
                    onSpeedPlus = {
                        chromeVisivel = true
                        val novo = (velocidade + SPEED_STEP).coerceAtMost(SPEED_MAX)
                        onSpeedChange(songAtual.id, novo)
                        if (repertoireDefaults != null) {
                            onPersistRepertoireDefaults(repertoireDefaults.repId, null, null, novo)
                        }
                    },
                )
            }
        }

        // Overlay de dica inicial, posicionado no terço inferior da tela
        // (não no rodapé puro): visível sem competir com a primeira música.
        val telaH = LocalConfiguration.current.screenHeightDp.dp
        AnimatedVisibility(
            visible = dicaVisivel,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = telaH * 2f / 3f),
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
    tomExibicao: String,
    semis: Int,
    onBack: () -> Unit,
    onTomMinus: () -> Unit,
    onTomPlus: () -> Unit,
    onTomReset: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StageBg.copy(alpha = 0.85f))
            .padding(start = 8.dp, end = 16.dp, top = 36.dp, bottom = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    if (velocidade > 0) "${song.artist} · ${velocidade}px/s"
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
        // Controle de tom: aparece sempre, com botão de reset visível só
        // quando semis != 0 (slot fixo pra não mexer no layout).
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Tom",
                color = StageFgDim,
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 8.dp),
            )
            StageActionButton(label = "−", enabled = true, onClick = onTomMinus)
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .width(36.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    tomExibicao,
                    color = StageAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
            }
            StageActionButton(label = "+", enabled = true, onClick = onTomPlus)
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (semis != 0) {
                    StageActionButton(label = "↺", enabled = true, onClick = onTomReset)
                }
            }
        }
    }
}

@Composable
private fun BoxScope.StageBottomBar(
    indice: Int,
    total: Int,
    ehTexto: Boolean,
    velocidade: Int,
    podeFontMinus: Boolean,
    podeFontPlus: Boolean,
    podeZoomMinus: Boolean,
    podeZoomPlus: Boolean,
    onFontMinus: () -> Unit,
    onFontPlus: () -> Unit,
    onZoomMinus: () -> Unit,
    onZoomPlus: () -> Unit,
    onSpeedMinus: () -> Unit,
    onSpeedPlus: () -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(StageBg.copy(alpha = 0.85f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DotsProgresso(indice = indice, total = total)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Zoom à esquerda (TEXT = fonte; IMG/PDF = scale).
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ehTexto) {
                    StageActionButton(label = "A−", enabled = podeFontMinus, onClick = onFontMinus)
                    Spacer(Modifier.size(8.dp))
                    StageActionButton(label = "A+", enabled = podeFontPlus, onClick = onFontPlus)
                } else {
                    StageActionButton(label = "−", enabled = podeZoomMinus, onClick = onZoomMinus)
                    Spacer(Modifier.size(8.dp))
                    StageActionButton(label = "+", enabled = podeZoomPlus, onClick = onZoomPlus)
                }
            }
            // Velocidade à direita (sempre visível — auto-scroll vale pra
            // TEXT, IMG e PDF). Display do valor entre os botões.
            Row(verticalAlignment = Alignment.CenterVertically) {
                StageActionButton(
                    label = "▼",
                    enabled = velocidade > SPEED_MIN,
                    onClick = onSpeedMinus,
                )
                Box(
                    modifier = Modifier.padding(horizontal = 6.dp).wrapContentSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (velocidade == 0) "off" else "$velocidade",
                        color = if (velocidade == 0) StageFgDim else StageAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                StageActionButton(
                    label = "▲",
                    enabled = velocidade < SPEED_MAX,
                    onClick = onSpeedPlus,
                )
            }
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
            .clip(RoundedCornerShape(16.dp))
            .background(StageBg.copy(alpha = 0.88f))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                "Toque na tela → próxima música",
                color = StageFg,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Arraste pra direita → música anterior",
                color = StageFg,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Arraste pra cima ou baixo → menu",
                color = StageFg,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Toque aqui pra fechar",
                color = StageFgDim,
                fontSize = 11.sp,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun StageText(
    song: Song,
    fontSize: TextUnit,
    semis: Int,
    prefereFlat: Boolean,
    scrollState: ScrollState,
) {
    // userScrollEnabled = false libera o drag vertical pra mostrar chrome.
    // O auto-scroll programático segue funcionando porque escreve direto no
    // scrollState.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState, enabled = false)
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
            section.lines.forEach { line ->
                // Transpõe inline. Quando semis=0 (caso comum), retorna a
                // string original sem alocar — sem custo.
                val linhaRender = if (semis == 0 || line.chords.isBlank()) line
                else line.copy(chords = Theory.transposeChordLine(line.chords, semis, prefereFlat))
                StageLine(linhaRender, fontSize)
            }
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
private fun StageImage(
    song: Song,
    scrollState: ScrollState,
    scale: Float,
    onScaleChange: (Float) -> Unit,
) {
    // Centralizada vertical e horizontalmente: cabe na viewport em 1x. Quando
    // ampliada via pinch, o verticalScroll programático permite percorrer a
    // imagem inteira via auto-scroll/chrome — pan manual é via pinch+drag.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState, enabled = false),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = Uri.parse(song.id),
            contentDescription = song.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(song.id) {
                    detectTransformGestures { _, _, zoom, _ ->
                        onScaleChange((scale * zoom).coerceIn(1f, 4f))
                    }
                }
                .graphicsLayer(scaleX = scale, scaleY = scale),
        )
    }
}

@Composable
private fun StagePdf(
    song: Song,
    scrollState: ScrollState,
    scale: Float,
    onScaleChange: (Float) -> Unit,
) {
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
                .verticalScroll(scrollState, enabled = false)
                .padding(horizontal = 8.dp, vertical = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            rendered.forEach { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(bmp.width.toFloat() / bmp.height.toFloat())
                        .pointerInput(song.id) {
                            detectTransformGestures { _, _, zoom, _ ->
                                onScaleChange((scale * zoom).coerceIn(1f, 4f))
                            }
                        }
                        .graphicsLayer(scaleX = scale, scaleY = scale),
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
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        view.keepScreenOn = true
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            view.keepScreenOn = false
        }
    }
}
