package com.gamaruzi.cifras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
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

// Splash em UMA tela só:
//
//   [splash nativa = só fundo verde, sem ícone]──┐
//        (Theme.Cifras.Splash com splash_empty)  │  cold start / boot Compose
//                                                ▼
//   [overlay Compose montado: logo grande +─────┐
//    slogan, ambos visíveis juntos]             │  SPLASH_DISPLAY_MS
//                                                ▼
//   [fadeOut do overlay → Search]───────────────┐
//        showOverlay = false                    │  OVERLAY_FADE_OUT_MS
//                                                ▼
//   [tela inicial]
//
// Como a splash nativa não tem mais ícone, não há "duas telas" (ícone
// nativo separado do overlay). O usuário vê fundo verde por um instante
// (cold start) e o overlay com logo+slogan aparece de uma vez, fica
// visível e some suave revelando a Search.
private const val SPLASH_DISPLAY_MS = 1800L
private const val OVERLAY_FADE_OUT_MS = 500

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() precisa ser chamado antes de super.onCreate().
        // Splash nativa é só fundo verde (windowSplashScreenAnimatedIcon =
        // drawable transparente), então não tem ícone pra manter visível —
        // o overlay Compose é a única tela com logo+slogan.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appState: AppState = viewModel()
            val themeMode by appState.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by appState.dynamicColor.collectAsStateWithLifecycle()

            var showOverlay by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(SPLASH_DISPLAY_MS)
                showOverlay = false
            }

            CifrasTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(appState = appState)
                    AnimatedVisibility(
                        visible = showOverlay,
                        // Sem fadeIn: o overlay já está renderizado desde
                        // o primeiro frame, sobre o fundo verde da janela.
                        // Quando o sistema termina a splash, o usuário já
                        // está vendo logo+slogan estáveis.
                        enter = EnterTransition.None,
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
                modifier = Modifier.size(200.dp),
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Feito por músicos",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "para músicos",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
