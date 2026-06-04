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
    private val keyFolders = stringSetPreferencesKey("folders_v1")
    private val keyRepertoires = stringSetPreferencesKey("repertoires_v1")
    private val keySetlistMigrated = booleanPreferencesKey("setlist_v1_migrated")
    // Ordenação por aba (Todas/Favoritas/Recentes). Codec: "TAB=MODE;TAB=MODE".
    private val keySortModes = stringPreferencesKey("sort_modes_v1")
    // Transposição (em semitons) salva por cifra. Reusa ScrollCodec porque
    // a estrutura é Map<URI, Int>. Valores negativos descem o tom; 0 ou
    // ausente = tom original.
    private val keyCifraSemis = stringPreferencesKey("cifra_semis_v1")
    // Zoom por cifra. TXT = tamanho da fonte em sp (16..44); IMG/PDF =
    // scale × 100 (100 = 1.0x, 200 = 2.0x). Reusa ScrollCodec — a
    // interpretação fica no consumidor por formato.
    private val keyCifraZoom = stringPreferencesKey("cifra_zoom_v1")

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

    // Velocidade de auto-scroll por música no Stage Mode, em pixels/segundo.
    // 0 (ou ausente) = sem auto-scroll. Reusa ScrollCodec porque a estrutura
    // é idêntica (Map<URI, Int>).
    val speeds: Flow<Map<String, Int>> = context.dataStore.data.map { prefs ->
        ScrollCodec.decode(prefs[keySpeeds].orEmpty())
    }

    // Transposição por cifra. Valores em semitons (-11..+11 em uso típico).
    val cifraSemis: Flow<Map<String, Int>> = context.dataStore.data.map { prefs ->
        ScrollCodec.decode(prefs[keyCifraSemis].orEmpty())
    }

    // Zoom por cifra (ver keyCifraZoom).
    val cifraZoom: Flow<Map<String, Int>> = context.dataStore.data.map { prefs ->
        ScrollCodec.decode(prefs[keyCifraZoom].orEmpty())
    }

    // Pastas criadas pelo usuário. Ordenadas pelo nome em lowercase para a UI
    // mostrar tudo no mesmo critério da biblioteca.
    val folders: Flow<List<Folder>> = context.dataStore.data.map { prefs ->
        prefs[keyFolders]
            .orEmpty()
            .mapNotNull(FolderCodec::decode)
            .sortedBy { it.name.lowercase() }
    }

    // Repertórios criados pelo usuário. Ordenados pelo nome em lowercase.
    // A migração do antigo setlist_v1 acontece sob demanda (ensureSetlistMigrated).
    val repertoires: Flow<List<Repertoire>> = context.dataStore.data.map { prefs ->
        prefs[keyRepertoires]
            .orEmpty()
            .mapNotNull(RepertoireCodec::decode)
            .sortedBy { it.name.lowercase() }
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
            prefs.remove(keyFolders)
            prefs.remove(keyRepertoires)
            prefs.remove(keySetlistMigrated)
            prefs.remove(keySortModes)
            prefs.remove(keyCifraSemis)
            prefs.remove(keyCifraZoom)
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

    // --- Repertórios ---

    // Roda 1x: se ainda não houver repertoires_v1 e existir um setlist_v1 com
    // conteúdo, cria "Repertório padrão" com os mesmos songIds. Marca a flag
    // setlist_v1_migrated pra não rodar de novo. O setlist antigo é apagado.
    suspend fun ensureSetlistMigrated() {
        context.dataStore.edit { prefs ->
            if (prefs[keySetlistMigrated] == true) return@edit
            val antigaLista = SetlistCodec.decode(prefs[keySetlist].orEmpty())
            if (antigaLista.isNotEmpty()) {
                val rep = Repertoire(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Repertório padrão",
                    songIds = antigaLista,
                )
                val atuais = prefs[keyRepertoires].orEmpty()
                prefs[keyRepertoires] = atuais + RepertoireCodec.encode(rep)
            }
            prefs.remove(keySetlist)
            prefs[keySetlistMigrated] = true
        }
    }

    suspend fun addRepertoire(
        name: String,
        color: String = "green",
        defaultTextZoom: Int = 18,
        defaultImageZoom: Float = 1.0f,
        defaultScrollSpeed: Int = 0,
    ): String {
        val id = java.util.UUID.randomUUID().toString()
        val rep = Repertoire(
            id = id,
            name = name.trim(),
            songIds = emptyList(),
            color = color,
            defaultTextZoom = defaultTextZoom,
            defaultImageZoom = defaultImageZoom,
            defaultScrollSpeed = defaultScrollSpeed,
        )
        context.dataStore.edit { prefs ->
            val atuais = prefs[keyRepertoires].orEmpty()
            prefs[keyRepertoires] = atuais + RepertoireCodec.encode(rep)
        }
        return id
    }

    // Renomeia, troca cor, ou ambos. Parâmetros null mantêm o valor atual.
    suspend fun renameRepertoire(id: String, novoNome: String, color: String? = null) {
        val nome = novoNome.trim()
        if (nome.isBlank()) return
        atualizaRepertoire(id) { it.copy(name = nome, color = color ?: it.color) }
    }

    suspend fun setRepertoireDefaults(
        id: String,
        textZoom: Int? = null,
        imageZoom: Float? = null,
        scrollSpeed: Int? = null,
    ) {
        atualizaRepertoire(id) {
            it.copy(
                defaultTextZoom = textZoom ?: it.defaultTextZoom,
                defaultImageZoom = imageZoom ?: it.defaultImageZoom,
                defaultScrollSpeed = scrollSpeed ?: it.defaultScrollSpeed,
            )
        }
    }

    suspend fun deleteRepertoire(id: String) {
        context.dataStore.edit { prefs ->
            prefs[keyRepertoires] = prefs[keyRepertoires].orEmpty()
                .mapNotNull(RepertoireCodec::decode)
                .filter { it.id != id }
                .map(RepertoireCodec::encode)
                .toSet()
        }
    }

    suspend fun addSongToRepertoire(repertoireId: String, songId: String) {
        atualizaRepertoire(repertoireId) { RepertoireCodec.addSong(it, songId) }
    }

    suspend fun addSongsToRepertoire(repertoireId: String, songIds: List<String>) {
        if (songIds.isEmpty()) return
        atualizaRepertoire(repertoireId) { RepertoireCodec.addSongs(it, songIds) }
    }

    suspend fun removeSongFromRepertoire(repertoireId: String, songId: String) {
        atualizaRepertoire(repertoireId) { RepertoireCodec.removeSong(it, songId) }
    }

    suspend fun moveRepertoireSongUp(repertoireId: String, index: Int) {
        atualizaRepertoire(repertoireId) { RepertoireCodec.moveUp(it, index) }
    }

    suspend fun moveRepertoireSongDown(repertoireId: String, index: Int) {
        atualizaRepertoire(repertoireId) { RepertoireCodec.moveDown(it, index) }
    }

    private suspend fun atualizaRepertoire(id: String, fn: (Repertoire) -> Repertoire) {
        context.dataStore.edit { prefs ->
            val atuais = prefs[keyRepertoires].orEmpty().mapNotNull(RepertoireCodec::decode)
            val achou = atuais.any { it.id == id }
            if (!achou) return@edit
            prefs[keyRepertoires] = atuais
                .map { if (it.id == id) fn(it) else it }
                .map(RepertoireCodec::encode)
                .toSet()
        }
    }

    // --- Pastas ---

    suspend fun addFolder(name: String, color: String = "green"): String {
        val id = java.util.UUID.randomUUID().toString()
        context.dataStore.edit { prefs ->
            val atual = prefs[keyFolders].orEmpty()
            prefs[keyFolders] = atual + FolderCodec.encode(Folder(id, name.trim(), color))
        }
        return id
    }

    // Renomeia e/ou troca a cor numa única edição. Passe `color = null`
    // para manter a cor atual.
    suspend fun renameFolder(id: String, novoNome: String, color: String? = null) {
        val nome = novoNome.trim()
        if (nome.isBlank()) return
        context.dataStore.edit { prefs ->
            val atualizado = prefs[keyFolders].orEmpty()
                .mapNotNull(FolderCodec::decode)
                .map {
                    if (it.id == id) it.copy(name = nome, color = color ?: it.color) else it
                }
                .map(FolderCodec::encode)
                .toSet()
            prefs[keyFolders] = atualizado
        }
    }

    // Remove a pasta e devolve à raiz qualquer cifra que estivesse nela.
    suspend fun deleteFolder(id: String) {
        context.dataStore.edit { prefs ->
            val pastasNovas = prefs[keyFolders].orEmpty()
                .mapNotNull(FolderCodec::decode)
                .filter { it.id != id }
                .map(FolderCodec::encode)
                .toSet()
            prefs[keyFolders] = pastasNovas

            val libNova = prefs[keyLibrary].orEmpty()
                .mapNotNull(LibraryEntryCodec::decode)
                .map { if (it.folderId == id) it.copy(folderId = null) else it }
                .map(LibraryEntryCodec::encode)
                .toSet()
            prefs[keyLibrary] = libNova
        }
    }

    // folderId null = mover para a raiz.
    suspend fun moveToFolder(uri: String, folderId: String?) {
        context.dataStore.edit { prefs ->
            val atual = prefs[keyLibrary].orEmpty().mapNotNull(LibraryEntryCodec::decode)
            val achou = atual.any { it.uri == uri }
            if (!achou) return@edit
            prefs[keyLibrary] = atual
                .map { if (it.uri == uri) it.copy(folderId = folderId) else it }
                .map(LibraryEntryCodec::encode)
                .toSet()
        }
    }

    // --- Ordenação ---

    val sortModesRaw: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[keySortModes].orEmpty()
    }

    suspend fun setSortModesRaw(raw: String) {
        context.dataStore.edit { prefs -> prefs[keySortModes] = raw }
    }

    // --- ---

    suspend fun setSpeed(songId: String, pxPerSecond: Int) {
        context.dataStore.edit { prefs ->
            val mapa = ScrollCodec.decode(prefs[keySpeeds].orEmpty()).toMutableMap()
            if (pxPerSecond <= 0) mapa.remove(songId) else mapa[songId] = pxPerSecond
            prefs[keySpeeds] = ScrollCodec.encode(mapa)
        }
    }

    suspend fun setCifraSemis(songId: String, semis: Int) {
        context.dataStore.edit { prefs ->
            val mapa = ScrollCodec.decode(prefs[keyCifraSemis].orEmpty()).toMutableMap()
            if (semis == 0) mapa.remove(songId) else mapa[songId] = semis
            prefs[keyCifraSemis] = ScrollCodec.encode(mapa)
        }
    }

    // Persiste zoom por cifra. Valor 0 ou padrão (TXT=18, IMG/PDF=100)
    // remove a entrada — o consumidor cai no default do repertório.
    suspend fun setCifraZoom(songId: String, valor: Int) {
        context.dataStore.edit { prefs ->
            val mapa = ScrollCodec.decode(prefs[keyCifraZoom].orEmpty()).toMutableMap()
            if (valor <= 0) mapa.remove(songId) else mapa[songId] = valor
            prefs[keyCifraZoom] = ScrollCodec.encode(mapa)
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

            val reps = prefs[keyRepertoires].orEmpty().mapNotNull(RepertoireCodec::decode)
            val repsLimpos = reps.map { RepertoireCodec.pruneOrphans(it, urisValidas) }
            if (repsLimpos != reps) {
                prefs[keyRepertoires] = repsLimpos.map(RepertoireCodec::encode).toSet()
            }

            val sp = ScrollCodec.decode(prefs[keySpeeds].orEmpty())
            val spLimpos = ScrollCodec.pruneOrphans(sp, urisValidas)
            if (spLimpos.size != sp.size) prefs[keySpeeds] = ScrollCodec.encode(spLimpos)
        }
    }
}
