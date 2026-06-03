package com.gamaruzi.cifras.data

// Um arquivo de cifra adicionado pelo usuário à biblioteca do app.
// A URI vem persistida do SAF (Photo Picker ou OpenDocument) com
// takePersistableUriPermission — sobrevive a reboots, mas não a uninstall.
data class LibraryEntry(
    val uri: String,
    val displayName: String,
    val format: SongFormat,
    val sizeBytes: Long,
)

// Codec linha-única para DataStore. Separador U+0001 (Start of Heading) é
// caractere de controle ausente de qualquer nome de arquivo válido em Android,
// então é seguro como delimitador sem precisar de escape.
internal object LibraryEntryCodec {
    private const val SEP = ""

    fun encode(entry: LibraryEntry): String =
        listOf(
            entry.uri,
            entry.displayName,
            entry.format.name,
            entry.sizeBytes.toString(),
        ).joinToString(SEP)

    fun decode(raw: String): LibraryEntry? {
        val parts = raw.split(SEP)
        if (parts.size != 4) return null
        val format = runCatching { SongFormat.valueOf(parts[2]) }.getOrNull() ?: return null
        val size = parts[3].toLongOrNull() ?: return null
        return LibraryEntry(
            uri = parts[0],
            displayName = parts[1],
            format = format,
            sizeBytes = size,
        )
    }
}

// Limites de tamanho aceitos para evitar OOM/lentidão na renderização.
// TEXT é livre — arquivos .txt de cifras nunca passam de uns poucos KB.
object SizeLimits {
    const val IMAGE_BYTES: Long = 3L * 1024 * 1024   // 3 MB
    const val PDF_BYTES: Long = 8L * 1024 * 1024     // 8 MB
    const val PDF_MAX_PAGES: Int = 20                // limite aplicado ao renderizar

    fun withinLimit(format: SongFormat, sizeBytes: Long): Boolean = when (format) {
        SongFormat.TEXT -> true
        SongFormat.IMAGE -> sizeBytes in 1..IMAGE_BYTES
        SongFormat.PDF -> sizeBytes in 1..PDF_BYTES
    }
}

// Mapeia mime type / extensão para o formato interno. null = arquivo não suportado.
object SongFormatDetector {
    fun fromMimeType(mime: String?): SongFormat? = when (mime?.lowercase()) {
        "text/plain" -> SongFormat.TEXT
        "application/pdf" -> SongFormat.PDF
        "image/jpeg", "image/jpg", "image/png", "image/webp" -> SongFormat.IMAGE
        else -> null
    }

    fun fromFileName(name: String?): SongFormat? {
        val ext = name?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase()
        return when (ext) {
            "txt" -> SongFormat.TEXT
            "pdf" -> SongFormat.PDF
            "jpg", "jpeg", "png", "webp" -> SongFormat.IMAGE
            else -> null
        }
    }

    // Combina ambos: mime tem prioridade (mais confiável), extensão é fallback.
    fun detect(mime: String?, fileName: String?): SongFormat? =
        fromMimeType(mime) ?: fromFileName(fileName)
}
