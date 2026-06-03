package com.gamaruzi.cifras.ui

import androidx.lifecycle.ViewModel
import com.gamaruzi.cifras.data.SampleSongs
import com.gamaruzi.cifras.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Estado compartilhado entre as 6 telas (Scope A: tudo em memória).
// Quando o SAF picker entrar (Scope B), `pickFolder` vai realmente abrir o
// `ACTION_OPEN_DOCUMENT_TREE` e listar os arquivos da pasta escolhida.
class AppState : ViewModel() {

    private val _folderName = MutableStateFlow<String?>(null)
    val folderName: StateFlow<String?> = _folderName.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _recents = MutableStateFlow<List<String>>(emptyList())
    val recents: StateFlow<List<String>> = _recents.asStateFlow()

    fun pickFolderMock() {
        _folderName.value = "Documentos/Cifras"
        _songs.value = SampleSongs.ALL
    }

    fun toggleFavorite(id: String) {
        _favorites.update { atual -> if (id in atual) atual - id else atual + id }
    }

    fun markRecent(id: String) {
        _recents.update { atual -> (listOf(id) + atual.filter { it != id }).take(12) }
    }

    fun song(id: String): Song? = _songs.value.find { it.id == id }
}
