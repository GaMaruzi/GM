package com.gamaruzi.cifras.data

// Pastas para organizar a biblioteca. id é UUID; name é o rótulo visível.
// Cifras se associam a uma pasta via LibraryEntry.folderId; null = raiz.
data class Folder(
    val id: String,
    val name: String,
)

internal object FolderCodec {
    private const val SEP = ""

    fun encode(folder: Folder): String =
        listOf(folder.id, folder.name).joinToString(SEP)

    fun decode(raw: String): Folder? {
        val parts = raw.split(SEP)
        if (parts.size != 2) return null
        if (parts[0].isBlank() || parts[1].isBlank()) return null
        return Folder(id = parts[0], name = parts[1])
    }
}
