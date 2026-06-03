package com.gamaruzi.cifras.data

// Parser puro de cifras em texto. Sem dependências de Android — testável
// como unit test JVM. Heurística baseada em docs/design/v1-violeta-completo/render.jsx
// e README.md do mesmo handoff.
//
// Regras:
// - Linha "de acordes": tokenizada por espaços, todos os tokens casam o regex
//   `^[A-G][#b]?(m|M|maj|min|sus|dim|aug|add|°)?\d*(/[A-G][#b]?)?$`
// - Linha seguinte de uma linha de acordes é a letra associada (Line(ch, ly))
// - Linha que casa o regex de seção (INTRO/VERSO/REFRÃO/PONTE/SOLO/...)
//   inicia uma nova Section
// - Linhas "Tom:" / "Capo:" / "Capotraste:" são metadados, não conteúdo
// - Linha vazia entre conteúdos é separador (descartada)
object CifraTextParser {

    data class Parsed(
        val key: String,
        val capo: Int,
        val sections: List<Section>,
    )

    private val chordToken = Regex(
        "^[A-G][#b]?(m|M|maj|min|sus|dim|aug|add|°)?\\d*(/[A-G][#b]?)?$"
    )

    // Qualquer linha "[X]" sozinha é seção, mesmo X não-padrão (ex: "[Falado]").
    private val bracketSection = Regex("^\\s*\\[\\s*([^\\]]+?)\\s*\\]\\s*$")

    // Tags sem colchetes só são reconhecidas se forem nomes conhecidos (case-insensitive).
    private val namedSection = Regex(
        "^\\s*(INTRO|VERSO|REFR[ÃA]O|PR[ÉE][\\- ]?REFR[ÃA]O|PONTE|SOLO|FINAL|OUTRO|CODA|BRIDGE|CHORUS|VERSE)(?:\\s+\\S+)?\\s*:?\\s*$",
        RegexOption.IGNORE_CASE
    )

    private fun matchSectionTag(line: String): String? {
        bracketSection.matchEntire(line)?.let { return normalizeTag(it.groupValues[1]) }
        namedSection.matchEntire(line)?.let { return normalizeTag(it.groupValues[1]) }
        return null
    }

    private val tomLine = Regex(
        "^\\s*(?:Tom|Tonalidade)\\s*:\\s*([A-G][#b]?m?)\\b",
        RegexOption.IGNORE_CASE
    )

    private val capoLine = Regex(
        "^\\s*Capo(?:traste)?(?:\\s+na)?\\s*:?\\s*(\\d+)",
        RegexOption.IGNORE_CASE
    )

    fun parse(content: String): Parsed {
        val rawLines = content.split('\n').map { it.trimEnd() }

        var key = ""
        var capo = 0
        val sections = mutableListOf<Section>()
        var currentTag = ""
        var currentLines = mutableListOf<Line>()

        fun flushSection() {
            if (currentLines.isNotEmpty()) {
                sections.add(Section(currentTag, currentLines.toList()))
            }
            currentLines = mutableListOf()
        }

        var i = 0
        while (i < rawLines.size) {
            val line = rawLines[i]

            val tomMatch = tomLine.find(line)
            if (tomMatch != null) {
                if (key.isEmpty()) key = tomMatch.groupValues[1].replaceFirstChar { c -> c.uppercase() }
                i++; continue
            }

            val capoMatch = capoLine.find(line)
            if (capoMatch != null) {
                capo = capoMatch.groupValues[1].toIntOrNull() ?: capo
                i++; continue
            }

            val sectionTag = matchSectionTag(line)
            if (sectionTag != null) {
                flushSection()
                currentTag = sectionTag
                i++; continue
            }

            if (line.isBlank()) {
                i++; continue
            }

            if (isChordLine(line)) {
                val nextLine = rawLines.getOrNull(i + 1) ?: ""
                val nextIsLyric = nextLine.isNotBlank()
                    && !isChordLine(nextLine)
                    && matchSectionTag(nextLine) == null
                    && !tomLine.containsMatchIn(nextLine)
                    && !capoLine.containsMatchIn(nextLine)
                if (nextIsLyric) {
                    currentLines.add(Line(line, nextLine))
                    if (key.isEmpty()) key = firstChordAsKey(line)
                    i += 2
                } else {
                    currentLines.add(Line(line, ""))
                    if (key.isEmpty()) key = firstChordAsKey(line)
                    i++
                }
                continue
            }

            // Letra solta sem acordes acima (vocal, narração)
            currentLines.add(Line("", line))
            i++
        }
        flushSection()

        return Parsed(
            key = key.ifEmpty { "—" },
            capo = capo,
            sections = sections,
        )
    }

    private fun isChordLine(line: String): Boolean {
        val tokens = line.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return false
        if (tokens.size > 12) return false
        return tokens.all { chordToken.matches(it) }
    }

    private fun firstChordAsKey(chordLine: String): String {
        val first = chordLine.trim().split(Regex("\\s+")).firstOrNull() ?: return ""
        // Para o "tom" preservamos "C" e "Cm" mas removemos extensões e baixos:
        // "Cmaj7" -> "C", "Em7" -> "Em", "G/B" -> "G", "C#m" -> "C#m"
        return first
            .replace(Regex("/.+$"), "")
            .replace(Regex("(maj|min|sus|dim|aug|add|°)\\d*"), "")
            .replace(Regex("\\d+$"), "")
    }

    private fun normalizeTag(raw: String): String {
        val upper = raw.uppercase()
            .replace("É", "E")
            .replace("Ã", "A")
            .replace("-", "")
            .replace(" ", "")
        return when (upper) {
            "INTRO" -> "Intro"
            "VERSO", "VERSE" -> "Verso"
            "REFRAO", "CHORUS" -> "Refrão"
            "PREREFRAO" -> "Pré-Refrão"
            "PONTE", "BRIDGE" -> "Ponte"
            "SOLO" -> "Solo"
            "FINAL", "OUTRO" -> "Final"
            "CODA" -> "Coda"
            else -> raw.lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
