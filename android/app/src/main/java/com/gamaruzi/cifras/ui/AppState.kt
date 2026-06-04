package com.gamaruzi.cifras.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamaruzi.cifras.data.CifrasRepository
import com.gamaruzi.cifras.data.Folder
import com.gamaruzi.cifras.data.LibraryEntry
import com.gamaruzi.cifras.data.Repertoire
import com.gamaruzi.cifras.data.SizeLimits
import com.gamaruzi.cifras.data.Song
import com.gamaruzi.cifras.data.SongFormat
import com.gamaruzi.cifras.data.SongFormatDetector
import com.gamaruzi.cifras.data.UserPreferences
import com.gamaruzi.cifras.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Resultado de uma operação de adição em lote, para feedback no UI.
data class AddResult(
    val adicionadas: Int,
    val ignoradasPorFormato: Int,
    val ignoradasPorTamanho: Int,
)

class AppState(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs = UserPreferences(context)
    private val repo = CifrasRepository(context)

    val library: StateFlow<List<LibraryEntry>> = prefs.library
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val favorites: StateFlow<Set<String>> = prefs.favorites
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val recents: StateFlow<List<String>> = prefs.recents
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val themeMode: StateFlow<ThemeMode> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SISTEMA)

    val dynamicColor: StateFlow<Boolean> = prefs.dynamicColor
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val scrollOffsets: StateFlow<Map<String, Int>> = prefs.scrollOffsets
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val speeds: StateFlow<Map<String, Int>> = prefs.speeds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val folders: StateFlow<List<Folder>> = prefs.folders
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val repertoires: StateFlow<List<Repertoire>> = prefs.repertoires
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _lastAddResult = MutableStateFlow<AddResult?>(null)
    val lastAddResult: StateFlow<AddResult?> = _lastAddResult.asStateFlow()

    init {
        // Migração 1x: setlist_v1 vira "Repertório padrão" no novo modelo.
        viewModelScope.launch { prefs.ensureSetlistMigrated() }

        viewModelScope.launch {
            prefs.library.collect { entries ->
                _loading.value = true
                _songs.value = repo.loadLibrary(entries)
                _loading.value = false
                // Cada mudança na biblioteca dispara uma limpeza de
                // favoritos/recentes órfãos. removeEntry já limpa imediato,
                // mas isto cobre cenários de migração ou reset parcial.
                prefs.pruneOrphans(entries.map { it.uri }.toSet())
            }
        }
    }

    // Adiciona URIs vindas dos pickers (Photo Picker ou OpenDocument).
    // - takePersistableUriPermission para sobreviver a reboot
    // - consulta nome/tamanho via ContentResolver
    // - detecta formato e valida limite de tamanho
    // - entries inválidas são reportadas em AddResult, sem travar o fluxo
    fun addUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            var ignoradasFormato = 0
            var ignoradasTamanho = 0
            val novas = mutableListOf<LibraryEntry>()

            uris.forEach { uri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                val mime = context.contentResolver.getType(uri)
                val (displayName, sizeBytes) = queryMetadata(uri)
                val format = SongFormatDetector.detect(mime, displayName)
                if (format == null) {
                    ignoradasFormato++
                    return@forEach
                }
                if (!SizeLimits.withinLimit(format, sizeBytes)) {
                    ignoradasTamanho++
                    return@forEach
                }
                novas += LibraryEntry(
                    uri = uri.toString(),
                    displayName = displayName,
                    format = format,
                    sizeBytes = sizeBytes,
                )
            }

            prefs.addEntries(novas)
            _lastAddResult.value = AddResult(novas.size, ignoradasFormato, ignoradasTamanho)
        }
    }

    fun removeFromLibrary(uri: String) {
        viewModelScope.launch {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    Uri.parse(uri),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            prefs.removeEntry(uri)
        }
    }

    // Salva um nome customizado para a entry. Vazio ou igual ao displayName
    // desfaz a renomeação (volta a usar o nome original do arquivo).
    fun renameEntry(uri: String, novoNome: String) {
        viewModelScope.launch {
            val atual = library.value.firstOrNull { it.uri == uri } ?: return@launch
            val custom = novoNome.trim().takeUnless { it.isEmpty() || it == atual.displayName }
            prefs.replaceEntry(atual.copy(customName = custom))
        }
    }

    fun clearLibrary() {
        viewModelScope.launch {
            library.value.forEach { entry ->
                runCatching {
                    context.contentResolver.releasePersistableUriPermission(
                        Uri.parse(entry.uri),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
            }
            prefs.clearAll()
        }
    }

    fun consumeAddResult() { _lastAddResult.value = null }

    fun toggleFavorite(id: String) {
        viewModelScope.launch { prefs.toggleFavorite(id) }
    }

    fun markRecent(id: String) {
        viewModelScope.launch { prefs.markRecent(id) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { prefs.setDynamicColor(enabled) }
    }

    fun saveScrollOffset(songId: String, offset: Int) {
        viewModelScope.launch { prefs.saveScrollOffset(songId, offset) }
    }

    fun createRepertoire(name: String, onCreated: (String) -> Unit = {}) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = prefs.addRepertoire(name)
            onCreated(id)
        }
    }

    fun renameRepertoire(id: String, novoNome: String) {
        viewModelScope.launch { prefs.renameRepertoire(id, novoNome) }
    }

    fun deleteRepertoire(id: String) {
        viewModelScope.launch { prefs.deleteRepertoire(id) }
    }

    fun addSongToRepertoire(repertoireId: String, songId: String) {
        viewModelScope.launch { prefs.addSongToRepertoire(repertoireId, songId) }
    }

    fun addSongsToRepertoire(repertoireId: String, songIds: List<String>) {
        viewModelScope.launch { prefs.addSongsToRepertoire(repertoireId, songIds) }
    }

    fun removeSongFromRepertoire(repertoireId: String, songId: String) {
        viewModelScope.launch { prefs.removeSongFromRepertoire(repertoireId, songId) }
    }

    fun moveRepertoireSongUp(repertoireId: String, index: Int) {
        viewModelScope.launch { prefs.moveRepertoireSongUp(repertoireId, index) }
    }

    fun moveRepertoireSongDown(repertoireId: String, index: Int) {
        viewModelScope.launch { prefs.moveRepertoireSongDown(repertoireId, index) }
    }

    fun repertoire(id: String): Repertoire? = repertoires.value.firstOrNull { it.id == id }

    fun setSpeed(songId: String, pxPerSecond: Int) {
        viewModelScope.launch { prefs.setSpeed(songId, pxPerSecond) }
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { prefs.addFolder(name) }
    }

    fun renameFolder(id: String, novoNome: String) {
        viewModelScope.launch { prefs.renameFolder(id, novoNome) }
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch { prefs.deleteFolder(id) }
    }

    fun moveToFolder(uri: String, folderId: String?) {
        viewModelScope.launch { prefs.moveToFolder(uri, folderId) }
    }

    fun song(id: String): Song? = _songs.value.find { it.id == id }

    private fun queryMetadata(uri: Uri): Pair<String, Long> {
        var name = "arquivo"
        var size = 0L
        runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0 && !cursor.isNull(nameIdx)) name = cursor.getString(nameIdx)
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
                }
            }
        }
        // Fallback pro último segmento do path quando o provider não devolve nome.
        if (name == "arquivo") name = uri.lastPathSegment?.substringAfterLast('/') ?: name
        return name to size
    }
}
