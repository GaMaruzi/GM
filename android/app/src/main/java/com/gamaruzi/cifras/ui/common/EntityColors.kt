package com.gamaruzi.cifras.ui.common

import androidx.compose.ui.graphics.Color

// Paleta de cores compartilhada entre pastas (PR 18) e repertórios (PR 19).
// Cada cor é identificada pela `key` persistida no codec; o ícone do
// container é sempre branco para garantir contraste em ambos os temas.
//
// Mantida em 8 opções para não fragmentar o visual da home. Verde é o
// default da marca quando nada foi escolhido (ou no fallback de codec).
data class EntityColor(
    val key: String,
    val label: String,
    val container: Color,
    val onContainer: Color = Color.White,
)

val EntityColorPalette: List<EntityColor> = listOf(
    EntityColor("green", "Verde", Color(0xFF1ED760)),
    EntityColor("blue", "Azul", Color(0xFF4285F4)),
    EntityColor("purple", "Roxo", Color(0xFF9C27B0)),
    EntityColor("pink", "Rosa", Color(0xFFEC407A)),
    EntityColor("red", "Vermelho", Color(0xFFE53935)),
    EntityColor("orange", "Laranja", Color(0xFFFB8C00)),
    EntityColor("yellow", "Amarelo", Color(0xFFFBC02D)),
    EntityColor("gray", "Cinza", Color(0xFF757575)),
)

const val DEFAULT_ENTITY_COLOR_KEY: String = "green"

// Resolve a chave persistida em EntityColor. Falha-silenciosa pro default
// quando o valor é desconhecido (downgrade ou bug futuro).
fun entityColorByKey(key: String?): EntityColor =
    EntityColorPalette.firstOrNull { it.key == key } ?: EntityColorPalette.first()
