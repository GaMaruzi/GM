package com.gamaruzi.cifras.ui.pesquisar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PesquisarEstado(
    val consulta: String = "",
    val resultados: List<String> = emptyList()
)

class PesquisarViewModel : ViewModel() {

    private val _estado = MutableStateFlow(PesquisarEstado())
    val estado: StateFlow<PesquisarEstado> = _estado.asStateFlow()

    fun onConsultaChange(novaConsulta: String) {
        _estado.update { it.copy(consulta = novaConsulta) }
        // TODO Marco 1: filtrar arquivos indexados pela consulta.
    }
}
