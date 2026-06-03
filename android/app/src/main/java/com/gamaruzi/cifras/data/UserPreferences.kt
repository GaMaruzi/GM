package com.gamaruzi.cifras.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension top-level — DataStore singleton por processo.
private val Context.dataStore by preferencesDataStore(name = "tap_cifras_prefs")

class UserPreferences(private val context: Context) {

    // Conjunto de entries serializadas (uma string por entry, codec em
    // LibraryEntryCodec). Uso de Set é OK: a ordem visual da biblioteca é
    // determinada pelo nome do arquivo, não pela ordem de inserção.
    private val keyLibrary = stringSetPreferencesKey("library_v1")

    val library: Flow<List<LibraryEntry>> = context.dataStore.data.map { prefs ->
        prefs[keyLibrary]
            .orEmpty()
            .mapNotNull(LibraryEntryCodec::decode)
            .sortedBy { it.displayName.lowercase() }
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
            val atual = prefs[keyLibrary].orEmpty()
            prefs[keyLibrary] = atual
                .mapNotNull(LibraryEntryCodec::decode)
                .filter { it.uri != uri }
                .map(LibraryEntryCodec::encode)
                .toSet()
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs -> prefs.remove(keyLibrary) }
    }
}
