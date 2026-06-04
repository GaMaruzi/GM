package com.gamaruzi.cifras

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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

// Tempo total mínimo da splash (nativa + overlay Compose). Garante leitura
// do slogan mesmo quando a UI carrega rápido. Curto o bastante pra não
// frustrar quem só quer abrir o app.
private const val SPLASH_TOTAL_MS = 1200L

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
                val restante = (SPLASH_TOTAL_MS - elapsed).coerceAtLeast(0L)
                if (restante > 0) delay(restante)
                keepSplash = false
                showOverlay = false
            }

            CifrasTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(appState = appState)
                    AnimatedVisibility(
                        visible = showOverlay,
                        enter = androidx.compose.animation.EnterTransition.None,
                        exit = fadeOut(animationSpec = tween(durationMillis = 220)),
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
                modifier = Modifier.size(180.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Feito por músicos para músicos",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
