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
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import com.gamaruzi.cifras.R
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
    cifraZoom: Map<String, Int> = emptyMap(),
    repertoireDefaults: StageDefaults? = null,
    onBack: () -> Unit,
    onPersistRepertoireDefaults: (repId: String, textZoom: Int?, imageZoom: Float?, scrollSpeed: Int?) -> Unit = { _, _, _, _ -> },
    onSpeedChange: (songId: String, pxPerSec: Int) -> Unit = { _, _ -> },
    onSemisChange: (songId: String, semis: Int) -> Unit = { _, _ -> },
    onZoomChange: (songId: String, valor: Int) -> Unit = { _, _ -> },
    onShowEnded: () -> Unit = onBack,
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
                cifraZoom = cifraZoom,
                repertoireDefaults = repertoireDefaults,
                onBack = onBack,
                onPersistRepertoireDefaults = onPersistRepertoireDefaults,
                onSpeedChange = onSpeedChange,
                onSemisChange = onSemisChange,
                onZoomChange = onZoomChange,
                onShowEnded = onShowEnded,
            )
        }
    }
}

@Composable
private fun StagePalco(
    musicas: List<Song>,
    speeds: Map<String, Int>,
    cifraSemis: Map<String, Int>,
    cifraZoom: Map<String, Int>,
    repertoireDefaults: StageDefaults?,
    onBack: () -> Unit,
    onPersistRepertoireDefaults: (repId: String, textZoom: Int?, imageZoom: Float?, scrollSpeed: Int?) -> Unit,
    onSpeedChange: (songId: String, pxPerSec: Int) -> Unit,
    onSemisChange: (songId: String, semis: Int) -> Unit,
    onZoomChange: (songId: String, valor: Int) -> Unit,
    onShowEnded: () -> Unit,
) {
    var indice by remember { mutableIntStateOf(0) }
    // fontSize/imageScale agora recalculados sempre que muda a música:
    // a fallback chain é cifraZoom[songId] → repertoireDefaults → FONT_DEFAULT.
    // Usar uma key composta no remember() força a re-leitura do mapa.
    val songIdParaInit = musicas[indice.coerceIn(0, musicas.size - 1)].id
    var fontSize by remember(songIdParaInit) {
        mutableIntStateOf(
            cifraZoom[songIdParaInit]
                ?: repertoireDefaults?.textZoom
                ?: FONT_DEFAULT
        )
    }
    var imageScale by remember(songIdParaInit) {
        mutableFloatStateOf(
            (cifraZoom[songIdParaInit]?.toFloat()
                ?: ((repertoireDefaults?.imageZoom ?: 1f) * 100f))
                / 100f
        )
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

    var chromeVisivel by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    LaunchedEffect(songAtual.id) {
        scrollState.scrollTo(0)
        // Não mostramos mais chrome ao trocar de música — o usuário pediu
        // fluidez. Chrome só aparece com double-tap deliberado.
        chromeVisivel = false
    }

    // Auto-hide do chrome.
    LaunchedEffect(chromeVisivel, songAtual.id) {
        if (!chromeVisivel) return@LaunchedEffect
        delay(CHROME_AUTO_HIDE_MS)
        chromeVisivel = false
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
    // detectors capturam o estado. O `by rememberUpdatedState` mantém os
    // pointerInputs com o valor mais recente sem invalidar o pointerInput.
    val indiceLatest by rememberUpdatedState(indice)
    val musicasSizeLatest by rememberUpdatedState(musicas.size)
    val chromeVisivelLatest by rememberUpdatedState(chromeVisivel)

    var showAcabou by remember { mutableStateOf(false) }
    val ehRepertorio = repertoireDefaults != null

    fun avancar() {
        if (indiceLatest < musicasSizeLatest - 1) {
            indice = indiceLatest + 1
        } else if (ehRepertorio) {
            // Última música de um repertório → tela "Show acabou!".
            // Em cifra única, mantém comportamento anterior (no-op).
            showAcabou = true
        }
    }

    fun voltar() {
        if (indiceLatest > 0) indice = indiceLatest - 1
    }

    // Detecta scroll vertical no conteúdo (TXT/PDF que tem verticalScroll
    // habilitado) e mostra o chrome em conjunto — sem consumir o scroll,
    // então a página segue rolando enquanto o menu aparece.
    val nestedScrollConn = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y != 0f) chromeVisivelSetter(true)
                return Offset.Zero
            }
            // setter populado abaixo (chromeVisivel não está em escopo aqui).
            var chromeVisivelSetter: (Boolean) -> Unit = {}
        }
    }
    // Recria a referência do setter a cada recomposição pra capturar
    // o `chromeVisivel` corrente.
    nestedScrollConn.chromeVisivelSetter = { v -> chromeVisivel = v }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConn)) {

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

        // Camada de gestos:
        // - Tap simples: se chrome aberto, fecha sem trocar música; senão,
        //   próxima música (ou tela "Show acabou" na última de repertório).
        // - Swipe horizontal pra direita = música anterior.
        // - Swipe vertical em IMG (que não tem verticalScroll): mostra chrome.
        //   Em TXT/PDF, o nestedScroll do conteúdo já avisa o estado quando
        //   o usuário arrasta verticalmente.
        // - Pinch (IMG/PDF) = zoom, via detectTransformGestures no conteúdo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        if (chromeVisivelLatest) chromeVisivel = false
                        else avancar()
                    })
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
                .pointerInput(songAtual.format) {
                    // Drag vertical apenas pra IMG (que tem scroll desativado
                    // no conteúdo): mostra chrome se passou do threshold.
                    if (songAtual.format != SongFormat.IMAGE) return@pointerInput
                    var totalY = 0f
                    detectVerticalDragGestures(
                        onDragStart = { totalY = 0f },
                        onDragEnd = {
                            if (abs(totalY) > SWIPE_THRESHOLD_PX) chromeVisivel = true
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
                    mostrarTom = ehTexto,
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
                    formato = songAtual.format,
                    velocidade = velocidade,
                    podeFontMinus = fontSize > FONT_MIN,
                    podeFontPlus = fontSize < FONT_MAX,
                    podeZoomMinus = imageScale > 1f,
                    podeZoomPlus = imageScale < 4f,
                    onFontMinus = {
                        chromeVisivel = true
                        val novo = (fontSize - 2).coerceAtLeast(FONT_MIN)
                        fontSize = novo
                        // Fonte/zoom persistem POR CIFRA (cifra_zoom_v1), não
                        // como default do repertório — cada cifra mantém o
                        // próprio ajuste.
                        onZoomChange(songAtual.id, novo)
                    },
                    onFontPlus = {
                        chromeVisivel = true
                        val novo = (fontSize + 2).coerceAtMost(FONT_MAX)
                        fontSize = novo
                        onZoomChange(songAtual.id, novo)
                    },
                    onZoomMinus = {
                        chromeVisivel = true
                        val novo = (imageScale - 0.5f).coerceAtLeast(1f)
                        imageScale = novo
                        // Scale × 100 = valor inteiro pro storage.
                        onZoomChange(songAtual.id, (novo * 100f).toInt())
                    },
                    onZoomPlus = {
                        chromeVisivel = true
                        val novo = (imageScale + 0.5f).coerceAtMost(4f)
                        imageScale = novo
                        onZoomChange(songAtual.id, (novo * 100f).toInt())
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

        // Overlay verde de "Show acabou" — entra com fade. Tap em qualquer
        // lugar OU no X chama onShowEnded; back do sistema também funciona
        // porque o palco continua "vivo" embaixo.
        AnimatedVisibility(
            visible = showAcabou,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            ShowAcabouOverlay(
                onClose = onShowEnded,
                onVoltarUltima = { showAcabou = false },
            )
        }
    }
}

@Composable
private fun ShowAcabouOverlay(
    onClose: () -> Unit,
    onVoltarUltima: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.splash_background))
            // Tap fecha pra Repertórios; swipe horizontal pra direita volta
            // pra última música (desfaz o "fim do show"). Mesmo padrão dos
            // gestos do palco (linhas ~310-326). pointerInput separado pra
            // tap e drag — combiná-los num só consome eventos errado.
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClose() })
            }
            .pointerInput(Unit) {
                var totalX = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalX = 0f },
                    onDragEnd = {
                        if (totalX > SWIPE_THRESHOLD_PX) onVoltarUltima()
                        totalX = 0f
                    },
                    onDragCancel = { totalX = 0f },
                    onHorizontalDrag = { _, dx -> totalX += dx },
                )
            },
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 40.dp)
                .size(44.dp),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Sair do palco", tint = Color.White)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_festival),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "O show acabou!",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Toque pra voltar pra Repertórios",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
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
    mostrarTom: Boolean,
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
        // Tom só pra cifras de texto — IMG/PDF não tem acordes detectáveis,
        // o controle só polui a tela.
        if (mostrarTom) {
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
}

@Composable
private fun BoxScope.StageBottomBar(
    indice: Int,
    total: Int,
    formato: SongFormat,
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
    val mostrarVelocidade = formato != SongFormat.IMAGE
    val ehTexto = formato == SongFormat.TEXT
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
        if (mostrarVelocidade) {
            // TEXT/PDF: zoom à esquerda, velocidade à direita.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
        } else {
            // IMAGE: só zoom, centralizado.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StageActionButton(label = "−", enabled = podeZoomMinus, onClick = onZoomMinus)
                Spacer(Modifier.size(16.dp))
                StageActionButton(label = "+", enabled = podeZoomPlus, onClick = onZoomPlus)
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
private fun StageText(
    song: Song,
    fontSize: TextUnit,
    semis: Int,
    prefereFlat: Boolean,
    scrollState: ScrollState,
) {
    // verticalScroll com gesto liberado: o usuário pode arrastar pra
    // adiantar/voltar a rolagem; o auto-scroll continua a partir do offset
    // atual (animateScrollBy escreve em scrollState.value que já está pós-drag).
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
                .verticalScroll(scrollState)
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
