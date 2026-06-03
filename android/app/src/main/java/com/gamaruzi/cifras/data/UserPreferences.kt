package com.gamaruzi.cifras.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension top-level — DataStore singleton por processo.
private val Context.dataStore by preferencesDataStore(name = "tap_cifras_prefs")

data class SavedFolder(val uri: Uri, val displayName: String)

class UserPreferences(private val context: Context) {

    private val keyFolderUri = stringPreferencesKey("folder_uri")
    private val keyFolderName = stringPreferencesKey("folder_name")

    val folder: Flow<SavedFolder?> = context.dataStore.data.map { prefs ->
        val uri = prefs[keyFolderUri]?.let(Uri::parse)
        val name = prefs[keyFolderName]
        if (uri != null && name != null) SavedFolder(uri, name) else null
    }

    suspend fun saveFolder(uri: Uri, displayName: String) {
        context.dataStore.edit { prefs ->
            prefs[keyFolderUri] = uri.toString()
            prefs[keyFolderName] = displayName
        }
    }

    suspend fun clearFolder() {
        context.dataStore.edit { prefs ->
            prefs.remove(keyFolderUri)
            prefs.remove(keyFolderName)
        }
    }
}
