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

class AppState(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs = UserPreferences(context)
    private val repo = CifrasRepository(context)

    val folderName: StateFlow<String?> = prefs.folder
        .map { it?.displayName }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _recents = MutableStateFlow<List<String>>(emptyList())
    val recents: StateFlow<List<String>> = _recents.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.folder.collect { saved ->
                if (saved == null) {
                    _songs.value = emptyList()
                } else {
                    _loading.value = true
                    _songs.value = repo.listSongs(saved.uri)
                    _loading.value = false
                }
            }
        }
    }

    fun setFolder(uri: Uri) {
        viewModelScope.launch {
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
}
