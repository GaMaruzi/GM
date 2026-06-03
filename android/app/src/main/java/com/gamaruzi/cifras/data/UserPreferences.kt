package com.gamaruzi.cifras.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gamaruzi.cifras.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension top-level — DataStore singleton por processo.
private val Context.dataStore by preferencesDataStore(name = "tap_cifras_prefs")

class UserPreferences(private val context: Context) {

    // Conjunto de entries serializadas (uma string por entry, codec em
    // LibraryEntryCodec). Uso de Set é OK: a ordem visual da biblioteca é
    // determinada pelo nome do arquivo, não pela ordem de inserção.
    private val keyLibrary = stringSetPreferencesKey("library_v1")
    private val keyFavorites = stringSetPreferencesKey("favorites_v1")
    private val keyRecents = stringPreferencesKey("recents_v1")
    private val keyThemeMode = stringPreferencesKey("theme_mode_v1")
    private val keyDynamicColor = booleanPreferencesKey("dynamic_color_v1")
    private val keyScrollOffsets = stringPreferencesKey("scroll_offsets_v1")
    private val keySetlist = stringPreferencesKey("setlist_v1")
    private val keySpeeds = stringPreferencesKey("speeds_v1")

    val library: Flow<List<LibraryEntry>> = context.dataStore.data.map { prefs ->
        prefs[keyLibrary]
            .orEmpty()
            .mapNotNull(LibraryEntryCodec::decode)
            .sortedBy { it.displayName.lowercase() }
    }

    val favorites: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[keyFavorites].orEmpty()
    }

    val recents: Flow<List<String>> = context.dataStore.data.map { prefs ->
        RecentsCodec.decode(prefs[keyRecents].orEmpty())
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.fromKey(prefs[keyThemeMode])
    }

    // Padrão false: identidade verde Spotify (ver Theme.kt). Toggle vira opt-in.
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyDynamicColor] ?: false
    }

    val scrollOffsets: Flow<Map<String, Int>> = context.dataStore.data.map { prefs ->
        ScrollCodec.decode(prefs[keyScrollOffsets].orEmpty())
    }

    val setlist: Flow<List<String>> = context.dataStore.data.map { prefs ->
        SetlistCodec.decode(prefs[keySetlist].orEmpty())
    }

    // Velocidade de auto-scroll por música no Stage Mode, em pixels/segundo.
    // 0 (ou ausente) = sem auto-scroll. Reusa ScrollCodec porque a estrutura
    // é idêntica (Map<URI, Int>).
    val speeds: Flow<Map<String, Int>> = context.dataStore.data.map { prefs ->
        ScrollCodec.decode(prefs[keySpeeds].orEmpty())
    }

    suspend fun addEntries(novas: List<LibraryEntry>) {
        if (novas.isEmpty()) return
        context.dataStore.edit { prefs ->
            val atual = prefs[keyLibrary].orEmpty().toMutableSet()
            // Deduplica por URI: se o usuário re-adiciona o mesmo arquivo,
            // substitui a entry antiga (tamanho/nome podem ter mudado).
            val urisNovas = novas.map { it.uri }.toSet()
            val mantidas = atual.mapNotNull(LibraryEntryCodec::decode)
                .filter { it.uri !in urisNovas }
                .map(LibraryEntryCodec::encode)
            prefs[keyLibrary] = (mantidas + novas.map(LibraryEntryCodec::encode)).toSet()
        }
    }

    // Substitui uma entry existente (mesma URI). Usado pra renomear sem
    // perder posição no Set. Se a URI não existe, é no-op.
    suspend fun replaceEntry(novaEntry: LibraryEntry) {
        context.dataStore.edit { prefs ->
            val atual = prefs[keyLibrary].orEmpty()
                .mapNotNull(LibraryEntryCodec::decode)
            val achou = atual.any { it.uri == novaEntry.uri }
            if (!achou) return@edit
            prefs[keyLibrary] = atual
                .map { if (it.uri == novaEntry.uri) novaEntry else it }
                .map(LibraryEntryCodec::encode)
                .toSet()
        }
    }

    suspend fun removeEntry(uri: String) {
        context.dataStore.edit { prefs ->
            val atualLib = prefs[keyLibrary].orEmpty()
            prefs[keyLibrary] = atualLib
                .mapNotNull(LibraryEntryCodec::decode)
                .filter { it.uri != uri }
                .map(LibraryEntryCodec::encode)
                .toSet()

            // Limpa também dos favoritos/recentes pra não deixar lixo.
            val favs = prefs[keyFavorites].orEmpty()
            if (uri in favs) prefs[keyFavorites] = favs - uri
            val rec = RecentsCodec.decode(prefs[keyRecents].orEmpty())
            if (uri in rec) prefs[keyRecents] = RecentsCodec.encode(rec - uri)
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.remove(keyLibrary)
            prefs.remove(keyFavorites)
            prefs.remove(keyRecents)
            prefs.remove(keyScrollOffsets)
            prefs.remove(keySetlist)
            prefs.remove(keySpeeds)
        }
    }

    suspend fun toggleFavorite(uri: String) {
        context.dataStore.edit { prefs ->
            val atual = prefs[keyFavorites].orEmpty()
            prefs[keyFavorites] = if (uri in atual) atual - uri else atual + uri
        }
    }

    suspend fun markRecent(uri: String) {
        context.dataStore.edit { prefs ->
            val atual = RecentsCodec.decode(prefs[keyRecents].orEmpty())
            prefs[keyRecents] = RecentsCodec.encode(RecentsCodec.applyMRU(atual, uri))
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs -> prefs[keyThemeMode] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[keyDynamicColor] = enabled }
    }

    suspend fun saveScrollOffset(songId: String, offset: Int) {
        context.dataStore.edit { prefs ->
            val mapa = ScrollCodec.decode(prefs[keyScrollOffsets].orEmpty()).toMutableMap()
            if (offset <= 0) mapa.remove(songId) else mapa[songId] = offset
            prefs[keyScrollOffsets] = ScrollCodec.encode(mapa)
        }
    }

    suspend fun addToSetlist(songId: String) {
        context.dataStore.edit { prefs ->
            val atual = SetlistCodec.decode(prefs[keySetlist].orEmpty())
            prefs[keySetlist] = SetlistCodec.encode(SetlistCodec.add(atual, songId))
        }
    }

    suspend fun removeFromSetlist(songId: String) {
        context.dataStore.edit { prefs ->
            val atual = SetlistCodec.decode(prefs[keySetlist].orEmpty())
            prefs[keySetlist] = SetlistCodec.encode(SetlistCodec.remove(atual, songId))
        }
    }

    suspend fun moveSetlistUp(index: Int) {
        context.dataStore.edit { prefs ->
            val atual = SetlistCodec.decode(prefs[keySetlist].orEmpty())
            prefs[keySetlist] = SetlistCodec.encode(SetlistCodec.moveUp(atual, index))
        }
    }

    suspend fun moveSetlistDown(index: Int) {
        context.dataStore.edit { prefs ->
            val atual = SetlistCodec.decode(prefs[keySetlist].orEmpty())
            prefs[keySetlist] = SetlistCodec.encode(SetlistCodec.moveDown(atual, index))
        }
    }

    suspend fun clearSetlist() {
        context.dataStore.edit { prefs ->
            prefs.remove(keySetlist)
            prefs.remove(keySpeeds)
        }
    }

    suspend fun setSpeed(songId: String, pxPerSecond: Int) {
        context.dataStore.edit { prefs ->
            val mapa = ScrollCodec.decode(prefs[keySpeeds].orEmpty()).toMutableMap()
            if (pxPerSecond <= 0) mapa.remove(songId) else mapa[songId] = pxPerSecond
            prefs[keySpeeds] = ScrollCodec.encode(mapa)
        }
    }

    // Sweep periódico — chamado quando a biblioteca muda — pra evitar que
    // favoritos/recentes apontem para URIs já excluídas (cenário raro hoje
    // porque removeEntry já limpa, mas a verificação por library garante
    // o caso de dados corrompidos ou migração futura).
    suspend fun pruneOrphans(urisValidas: Set<String>) {
        context.dataStore.edit { prefs ->
            val favs = prefs[keyFavorites].orEmpty()
            val favsLimpos = favs.filter { it in urisValidas }.toSet()
            if (favsLimpos.size != favs.size) prefs[keyFavorites] = favsLimpos

            val rec = RecentsCodec.decode(prefs[keyRecents].orEmpty())
            val recLimpos = RecentsCodec.pruneOrphans(rec, urisValidas)
            if (recLimpos.size != rec.size) prefs[keyRecents] = RecentsCodec.encode(recLimpos)

            val scrolls = ScrollCodec.decode(prefs[keyScrollOffsets].orEmpty())
            val scrollsLimpos = ScrollCodec.pruneOrphans(scrolls, urisValidas)
            if (scrollsLimpos.size != scrolls.size)
                prefs[keyScrollOffsets] = ScrollCodec.encode(scrollsLimpos)

            val setl = SetlistCodec.decode(prefs[keySetlist].orEmpty())
            val setlLimpos = SetlistCodec.pruneOrphans(setl, urisValidas)
            if (setlLimpos.size != setl.size)
                prefs[keySetlist] = SetlistCodec.encode(setlLimpos)

            val sp = ScrollCodec.decode(prefs[keySpeeds].orEmpty())
            val spLimpos = ScrollCodec.pruneOrphans(sp, urisValidas)
            if (spLimpos.size != sp.size) prefs[keySpeeds] = ScrollCodec.encode(spLimpos)
        }
    }
}
