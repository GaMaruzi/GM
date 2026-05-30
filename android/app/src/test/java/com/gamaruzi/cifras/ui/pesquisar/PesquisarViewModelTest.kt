package com.gamaruzi.cifras.ui.pesquisar

import org.junit.Assert.assertEquals
import org.junit.Test

class PesquisarViewModelTest {

    @Test
    fun `consulta inicial vazia`() {
        val vm = PesquisarViewModel()
        assertEquals("", vm.estado.value.consulta)
    }

    @Test
    fun `onConsultaChange atualiza estado`() {
        val vm = PesquisarViewModel()
        vm.onConsultaChange("Wonderwall")
        assertEquals("Wonderwall", vm.estado.value.consulta)
    }
}
