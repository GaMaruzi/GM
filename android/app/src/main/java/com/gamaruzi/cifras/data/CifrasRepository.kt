package com.gamaruzi.cifras.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// PR 1: lista arquivos .txt da pasta escolhida e expõe conteúdo bruto.
// Parsing inteligente (acordes vs letra, transposição, capotraste) entra no PR 2.
class CifrasRepository(private val context: Context) {

    suspend fun listSongs(treeUri: Uri): List<Song> = withContext(Dispatchers.IO) {
        val raiz = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        raiz.listFiles()
            .asSequence()
            .filter { it.isFile && it.name?.endsWith(".txt", ignoreCase = true) == true }
            .sortedBy { it.name?.lowercase() }
            .map { doc -> toMinimalSong(doc) }
            .toList()
    }

    // Lê o conteúdo bruto do arquivo. Usado pelo Detail enquanto o parser real
    // não chega (PR 2). Retorna null se o arquivo sumiu ou não pode ser lido.
    suspend fun readContent(fileUri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(fileUri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            }
        }.getOrNull()
    }

    private fun toMinimalSong(doc: DocumentFile): Song {
        val nomeArquivo = doc.name ?: "sem-nome.txt"
        val semExt = nomeArquivo.substringBeforeLast('.', nomeArquivo)
        return Song(
            id = doc.uri.toString(),
            file = nomeArquivo,
            title = semExt,
            artist = "—",
            key = "—",
            capo = 0,
            genre = "",
            ext = nomeArquivo.substringAfterLast('.', "txt"),
            // Sections virá do parser real no PR 2. Por enquanto vazio — o
            // Detail carrega o conteúdo bruto via readContent().
            sections = emptyList()
        )
    }
}
