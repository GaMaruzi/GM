package com.gamaruzi.cifras.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CifrasRepository(private val context: Context) {

    // Carrega Songs para cada entry da biblioteca. Para TEXT, parseia o
    // conteúdo (acordes/seções); para IMAGE/PDF, devolve Song sem sections
    // — a renderização é tratada pelo DetailScreen via Coil/PdfRenderer.
    // Roda em IO; mesmo com centenas de entries continua tranquilo porque
    // só TEXT realmente lê bytes do arquivo (img/pdf só metadados).
    suspend fun loadLibrary(library: List<LibraryEntry>): List<Song> =
        withContext(Dispatchers.IO) {
            library.mapNotNull { entry -> toSong(entry) }
        }

    private fun toSong(entry: LibraryEntry): Song? {
        val (title, artist) = splitTitleArtist(entry.displayName)
        val ext = entry.displayName.substringAfterLast('.', "")
            .ifEmpty { defaultExt(entry.format) }

        return when (entry.format) {
            SongFormat.TEXT -> {
                val raw = readTextContent(Uri.parse(entry.uri)) ?: return null
                val parsed = CifraTextParser.parse(raw)
                Song(
                    id = entry.uri,
                    file = entry.displayName,
                    title = title,
                    artist = artist,
                    key = parsed.key,
                    capo = parsed.capo,
                    genre = "",
                    ext = ext,
                    format = SongFormat.TEXT,
                    sizeBytes = entry.sizeBytes,
                    sections = parsed.sections,
                )
            }
            SongFormat.IMAGE, SongFormat.PDF -> Song(
                id = entry.uri,
                file = entry.displayName,
                title = title,
                artist = artist,
                key = "—",
                capo = 0,
                genre = "",
                ext = ext,
                format = entry.format,
                sizeBytes = entry.sizeBytes,
                sections = emptyList(),
            )
        }
    }

    private fun defaultExt(format: SongFormat): String = when (format) {
        SongFormat.TEXT -> "txt"
        SongFormat.PDF -> "pdf"
        SongFormat.IMAGE -> "img"
    }

    private fun readTextContent(fileUri: Uri): String? =
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
