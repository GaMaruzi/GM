package com.gamaruzi.cifras.data

data class Song(
    val id: String,
    val file: String,
    val title: String,
    val artist: String,
    val key: String,
    val capo: Int,
    val genre: String,
    val ext: String,
    val sections: List<Section>
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
