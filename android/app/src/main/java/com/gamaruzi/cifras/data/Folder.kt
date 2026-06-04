package com.gamaruzi.cifras.data

// Pastas para organizar a biblioteca. id é UUID; name é o rótulo visível;
// color é uma chave da paleta (ver ui/common/EntityColors.kt) — default
// "green" pra entradas antigas (pré-1.2.0) que não tinham campo de cor.
// Cifras se associam a uma pasta via LibraryEntry.folderId; null = raiz.
data class Folder(
    val id: String,
    val name: String,
    val color: String = "green",
)

// Codec linha-única para DataStore. Separador U+0001 idem aos demais codecs.
// v1 = (id | name)              — pasta sem cor escolhida
// v2 = (id | name | color)      — pasta com cor escolhida
// Decoder aceita v1; encoder sempre escreve v2.
internal object FolderCodec {
    private const val SEP = ""

    fun encode(folder: Folder): String =
        listOf(folder.id, folder.name, folder.color).joinToString(SEP)

    fun decode(raw: String): Folder? {
        val parts = raw.split(SEP)
        if (parts.size !in 2..3) return null
        if (parts[0].isBlank() || parts[1].isBlank()) return null
        val color = if (parts.size >= 3 && parts[2].isNotBlank()) parts[2] else "green"
        return Folder(id = parts[0], name = parts[1], color = color)
    }
}
