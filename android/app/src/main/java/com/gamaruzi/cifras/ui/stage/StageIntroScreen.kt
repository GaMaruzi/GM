package com.gamaruzi.cifras.ui.stage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamaruzi.cifras.R

// Tela de instruções antes do palco. Fundo verde igual à splash, lista
// curta de gestos, e botão grande "Iniciar show" que entra direto no
// palco — sem dica sobrepondo a primeira música.
@Composable
fun StageIntroScreen(
    onStart: () -> Unit,
    onBack: () -> Unit,
) {
    val verde = colorResource(id = R.color.splash_background)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(verde),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 40.dp)
                .size(44.dp),
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Voltar",
                tint = Color.White,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Festival,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp),
                )
            }
            Spacer(Modifier.height(28.dp))
            Text(
                "Pronto pro show?",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.widthIn(max = 360.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Instrucao("Toque na tela", "→ próxima música")
                Spacer(Modifier.height(10.dp))
                Instrucao("Arraste pra direita", "→ música anterior")
                Spacer(Modifier.height(10.dp))
                Instrucao("Toque 2 vezes", "→ abrir/fechar controles")
                Spacer(Modifier.height(10.dp))
                Instrucao("Arraste a cifra", "→ adiantar/voltar a rolagem")
            }
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = verde,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp)
                    .height(56.dp),
            ) {
                Text(
                    "Tocar pra começar o show",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun Instrucao(gesto: String, resultado: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            gesto,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.size(6.dp))
        Text(
            resultado,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 16.sp,
        )
    }
}
