package com.gamaruzi.cifras.data

// Formato físico do arquivo da cifra. Determina como Detail e Stage renderizam.
// - TEXT: parsing por seções/linhas (CifraTextParser); transposição e fonte ativas
// - IMAGE: renderiza bitmap via Coil; sem transposição/fonte
// - PDF:   renderiza páginas via PdfRenderer nativo; sem transposição/fonte
enum class SongFormat { TEXT, IMAGE, PDF }

data class Song(
    val id: String,
    val file: String,
    val title: String,
    val artist: String,
    val key: String,
    val capo: Int,
    val genre: String,
    val ext: String,
    val format: SongFormat,
    val sizeBytes: Long,
    val sections: List<Section>,
    val folderId: String? = null,
)

data class Section(
    val tag: String,
    val lines: List<Line>
)

// Uma linha pode ter só acordes (`chords` preenchido, `lyrics` vazio),
// só letra, ou ambos alinhados por coluna (acordes em cima, letra embaixo).
data class Line(
    val chords: String,
    val lyrics: String
)
