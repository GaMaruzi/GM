package com.gamaruzi.cifras.ui.pesquisar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamaruzi.cifras.ui.theme.CifrasTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesquisarScreen(
    viewModel: PesquisarViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cifras") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = estado.consulta,
                onValueChange = viewModel::onConsultaChange,
                label = { Text("Pesquisar música") },
                modifier = Modifier.fillMaxWidth()
            )

            if (estado.resultados.isEmpty()) {
                Text("Nenhuma cifra encontrada. (Importação local entra no Marco 1.)")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(estado.resultados) { resultado ->
                        Text(resultado)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PesquisarScreenPreview() {
    CifrasTheme { PesquisarScreen() }
}
