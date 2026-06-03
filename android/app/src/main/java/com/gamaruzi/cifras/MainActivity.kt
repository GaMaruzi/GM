package com.gamaruzi.cifras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamaruzi.cifras.ui.AppState
import com.gamaruzi.cifras.ui.navigation.AppNavHost
import com.gamaruzi.cifras.ui.theme.CifrasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState: AppState = viewModel()
            val themeMode by appState.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by appState.dynamicColor.collectAsStateWithLifecycle()
            CifrasTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                AppNavHost(appState = appState)
            }
        }
    }
}
