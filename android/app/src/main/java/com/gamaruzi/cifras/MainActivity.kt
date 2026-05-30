package com.gamaruzi.cifras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gamaruzi.cifras.ui.navigation.AppNavHost
import com.gamaruzi.cifras.ui.theme.CifrasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CifrasTheme {
                AppNavHost()
            }
        }
    }
}
