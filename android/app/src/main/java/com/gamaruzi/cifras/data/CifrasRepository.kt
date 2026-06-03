package com.gamaruzi.cifras.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CifrasRepository(private val context: Context) {

    // Lista + parseia todas as cifras .txt da pasta escolhida.
    // O parsing roda em IO; para um conjunto típico (~dezenas a centenas de
    // arquivos pequenos) é desprezível. Se virar problema, mover pro PR 6 com
    // index incremental em DataStore/Room.
    suspend fun listSongs(treeUri: Uri): List<Song> = withContext(Dispatchers.IO) {
        val raiz = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        raiz.listFiles()
            .asSequence()
            .filter { it.isFile && it.name?.endsWith(".txt", ignoreCase = true) == true }
            .sortedBy { it.name?.lowercase() }
            .mapNotNull { doc -> parseSong(doc) }
            .toList()
    }

    private fun parseSong(doc: DocumentFile): Song? {
        val fileName = doc.name ?: return null
        val raw = readContentInternal(doc.uri) ?: return null
        val parsed = CifraTextParser.parse(raw)
        val (title, artist) = splitTitleArtist(fileName)
        return Song(
            id = doc.uri.toString(),
            file = fileName,
            title = title,
            artist = artist,
            key = parsed.key,
            capo = parsed.capo,
            genre = "",
            ext = fileName.substringAfterLast('.', "txt"),
            sections = parsed.sections,
        )
    }

    private fun readContentInternal(fileUri: Uri): String? =
        runCatching {
            context.contentResolver.openInputStream(fileUri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            }
        }.getOrNull()
}

// "Asa Branca - Luiz Gonzaga.txt" → ("Asa Branca", "Luiz Gonzaga")
// "Wonderwall.txt"                 → ("Wonderwall", "—")
// "A - B - C.txt"                  → ("A - B", "C")   (último " - " divide)
internal fun splitTitleArtist(fileName: String): Pair<String, String> {
    val semExt = fileName.substringBeforeLast('.', fileName)
    val sep = " - "
    val idx = semExt.lastIndexOf(sep)
    return if (idx > 0) {
        semExt.substring(0, idx).trim() to semExt.substring(idx + sep.length).trim()
    } else {
        semExt.trim() to "—"
    }
}
