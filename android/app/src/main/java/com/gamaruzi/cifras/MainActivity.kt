package com.gamaruzi.cifras

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamaruzi.cifras.ui.AppState
import com.gamaruzi.cifras.ui.navigation.AppNavHost
import com.gamaruzi.cifras.ui.theme.CifrasTheme
import kotlinx.coroutines.delay

// Tempo total mínimo da splash. A janela nativa exibe o logo + fundo verde
// desde o primeiro frame; o overlay Compose monta por baixo com logo+slogan
// e a nativa some revelando-o. OVERLAY_MIN_VISIBLE_MS garante tempo mínimo
// de slogan visível mesmo em cold start lento onde elapsed > SPLASH_TOTAL_MS
// quando o Compose finalmente roda — sem ele, o slogan sairia imediatamente.
private const val SPLASH_TOTAL_MS = 1600L
private const val OVERLAY_MIN_VISIBLE_MS = 900L
private const val OVERLAY_FADE_IN_MS = 180
private const val OVERLAY_FADE_OUT_MS = 400

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() precisa ser chamado antes de super.onCreate().
        // A splash nativa cobre a janela com fundo verde + logo enquanto o
        // Compose monta. O overlay Compose abaixo (SplashOverlay) garante que
        // o slogan "Feito por músicos para músicos" fique visível por
        // SPLASH_TOTAL_MS desde o início do processo.
        val splashScreen = installSplashScreen()
        val processStart = SystemClock.uptimeMillis()
        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appState: AppState = viewModel()
            val themeMode by appState.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by appState.dynamicColor.collectAsStateWithLifecycle()

            // Mostra overlay Compose enquanto a duração total ainda não passou.
            // Inicia true para evitar 1 frame de UI exposto antes do overlay
            // se a splash nativa for descartada cedo (Android 12+ pode liberar
            // a janela antes do nosso timer).
            var showOverlay by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                val elapsed = SystemClock.uptimeMillis() - processStart
                // coerceAtLeast(OVERLAY_MIN_VISIBLE_MS): mesmo em cold start
                // onde elapsed já passou de SPLASH_TOTAL_MS, mantemos o
                // overlay por pelo menos OVERLAY_MIN_VISIBLE_MS pra garantir
                // logo+slogan visíveis juntos.
                val restante = (SPLASH_TOTAL_MS - elapsed).coerceAtLeast(OVERLAY_MIN_VISIBLE_MS)
                delay(restante)
                keepSplash = false
                showOverlay = false
            }

            CifrasTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(appState = appState)
                    AnimatedVisibility(
                        visible = showOverlay,
                        // fadeIn curto (180ms): como o logo da nativa e o do
                        // overlay estão na mesma posição/cor, o fade só faz
                        // o slogan surgir suavemente junto do logo. Sem isso,
                        // o slogan aparece "saltando" no momento em que a
                        // splash nativa sai.
                        enter = fadeIn(animationSpec = tween(durationMillis = OVERLAY_FADE_IN_MS)),
                        exit = fadeOut(animationSpec = tween(durationMillis = OVERLAY_FADE_OUT_MS)),
                    ) {
                        SplashOverlay()
                    }
                }
            }
        }
    }
}

// Overlay com fundo verde idêntico ao da splash nativa, logo grande no
// centro e slogan logo abaixo. Não consome cliques (não há interatividade).
@Composable
private fun SplashOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.splash_background)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.splash_logo),
                contentDescription = null,
                tint = Color.White,
                // 132dp dá folga visual; antes 180dp encostava no slogan
                // em telas menores.
                modifier = Modifier.size(132.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Feito por músicos",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "para músicos",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
