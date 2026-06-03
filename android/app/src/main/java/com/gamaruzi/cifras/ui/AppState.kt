package com.gamaruzi.cifras.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamaruzi.cifras.data.CifrasRepository
import com.gamaruzi.cifras.data.Song
import com.gamaruzi.cifras.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// PR 1: AppState passa a usar DataStore para a pasta escolhida (URI persistida)
// e CifrasRepository para listar os arquivos da pasta.
// Favoritos e recentes ainda em memória — entram no PR 3.
class AppState(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs = UserPreferences(context)
    private val repo = CifrasRepository(context)

    val folderName: StateFlow<String?> = prefs.folder
        .map { it?.displayName }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _recents = MutableStateFlow<List<String>>(emptyList())
    val recents: StateFlow<List<String>> = _recents.asStateFlow()

    init {
        // Quando a pasta muda (boot do app ou troca de pasta), recarrega a lista.
        viewModelScope.launch {
            prefs.folder.collect { saved ->
                _songs.value = saved?.let { repo.listSongs(it.uri) } ?: emptyList()
            }
        }
    }

    // Chamado depois do usuário confirmar a pasta no SAF picker.
    fun setFolder(uri: Uri) {
        viewModelScope.launch {
            // Sem isso a URI fica inválida depois de reiniciar o app.
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val displayName = DocumentFile.fromTreeUri(context, uri)?.name ?: "Pasta de cifras"
            prefs.saveFolder(uri, displayName)
        }
    }

    fun clearFolder() {
        viewModelScope.launch { prefs.clearFolder() }
    }

    fun toggleFavorite(id: String) {
        _favorites.update { atual -> if (id in atual) atual - id else atual + id }
    }

    fun markRecent(id: String) {
        _recents.update { atual -> (listOf(id) + atual.filter { it != id }).take(12) }
    }

    fun song(id: String): Song? = _songs.value.find { it.id == id }

    suspend fun readSongContent(id: String): String? {
        val song = song(id) ?: return null
        return repo.readContent(Uri.parse(song.id))
    }
}
