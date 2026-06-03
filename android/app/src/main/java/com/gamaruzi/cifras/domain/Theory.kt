package com.gamaruzi.cifras.domain

// Helpers de transposição. Port direto de docs/design/v1-violeta-completo/theory.jsx,
// adaptado ao idiomatismo Kotlin. Sem estado, tudo puro — fácil de testar.

object Theory {

    val SHARP = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val FLAT  = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    // Índice cromático de cada grafia possível de uma nota raiz.
    // Inclui "esquisitices" como B#=C, Cb=B, E#=F, Fb=E para suportar
    // cifras que aparecem dessa forma.
    private val INDEX: Map<String, Int> = mapOf(
        "C" to 0, "B#" to 0,
        "C#" to 1, "Db" to 1,
        "D" to 2,
        "D#" to 3, "Eb" to 3,
        "E" to 4, "Fb" to 4,
        "F" to 5, "E#" to 5,
        "F#" to 6, "Gb" to 6,
        "G" to 7,
        "G#" to 8, "Ab" to 8,
        "A" to 9,
        "A#" to 10, "Bb" to 10,
        "B" to 11, "Cb" to 11,
    )

    // Tons que convencionalmente usam bemóis. Bibliografia musical clássica.
    private val FLAT_KEYS = setOf(
        "F", "Bb", "Eb", "Ab", "Db", "Gb",
        "Dm", "Gm", "Cm", "Fm", "Bbm",
    )

    fun keyPrefersFlat(key: String): Boolean = key in FLAT_KEYS

    // Devolve o nome da nota no índice cromático (0..11) já normalizado pra
    // índices negativos. Escolhe grafia sustenida ou bemol conforme preferFlat.
    fun noteName(index: Int, preferFlat: Boolean): String {
        val normalizado = ((index % 12) + 12) % 12
        return if (preferFlat) FLAT[normalizado] else SHARP[normalizado]
    }

    // Transpõe um único token de acorde, ex.: "C#m7", "G/B", "Dsus4".
    // Tokens não reconhecíveis (ex.: "x", "//") voltam intactos.
    fun transposeChord(token: String, semis: Int, preferFlat: Boolean): String {
        // Slash chord: bass note separado por /
        val partes = token.split("/")
        return partes.joinToString("/") { transposeTokenSimples(it, semis, preferFlat) }
    }

    private fun transposeTokenSimples(token: String, semis: Int, preferFlat: Boolean): String {
        val match = TOKEN_REGEX.matchEntire(token) ?: return token
        val raiz = match.groupValues[1]
        val resto = match.groupValues[2]
        val idxBase = INDEX[raiz] ?: return token
        val novaNota = noteName(idxBase + semis, preferFlat)
        return novaNota + resto
    }

    // Transpõe a key (tom da música), preservando sufixos como "m" ou "maj".
    fun transposeKey(key: String, semis: Int, preferFlat: Boolean): String {
        if (key.isBlank() || key == "—") return key
        val match = TOKEN_REGEX.matchEntire(key) ?: return key
        val raiz = match.groupValues[1]
        val resto = match.groupValues[2]
        val idxBase = INDEX[raiz] ?: return key
        return noteName(idxBase + semis, preferFlat) + resto
    }

    // Transpõe uma linha inteira preservando alinhamento por colunas.
    // Cada acorde é ancorado na coluna original; só desloca à direita se houver
    // colisão com o acorde anterior. Resultado: render em Monospace mantém a
    // letra logo abaixo alinhada com o acorde correto.
    fun transposeChordLine(line: String, semis: Int, preferFlat: Boolean): String {
        if (line.isEmpty() || semis == 0) return line
        val sb = StringBuilder()
        TOKEN_RUN_REGEX.findAll(line).forEach { match ->
            val coluna = match.range.first
            val transposto = transposeChord(match.value, semis, preferFlat)
            when {
                coluna > sb.length -> repeat(coluna - sb.length) { sb.append(' ') }
                sb.isNotEmpty() -> sb.append(' ')
            }
            sb.append(transposto)
        }
        return sb.toString()
    }

    // ^([A-G][#b]?)(.*)$
    private val TOKEN_REGEX = Regex("^([A-G][#b]?)(.*)$")

    // \S+ — runs contínuos de não-espaço (cada acorde da linha)
    private val TOKEN_RUN_REGEX = Regex("\\S+")
}
