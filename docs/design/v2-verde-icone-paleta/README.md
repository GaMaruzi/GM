# Tap Cifras — Ícone & Paleta de cores

Pacote para o **Claude Code** aplicar (1) o **ícone do app** e (2) a **paleta de cores** Material 3 no projeto Android (Kotlin + Jetpack Compose + Material 3).

> Abra `preview.html` no navegador para ver o ícone (nas máscaras do launcher) e todas as cores com swatches.

---

## 1) Ícone do app — "Toque" (Conceito A)

Marca minimalista: uma **mão/dedo tocando** (traço branco) sobre fundo **verde Spotify**. O nome *Tap* guia o símbolo. Adaptive icon com **foreground + background separados** e zona segura central de 66dp.

**Arquivos (vetor, prontos pra usar):**
- `ic_launcher_background.svg` — fundo verde (gradiente `#25E06A → #1DB954 → #12953F`).
- `ic_launcher_foreground.svg` — mão branca, já posicionada na zona segura.
- `ic_launcher.svg` — ícone composto (512px) para Play Store / preview.

**Como aplicar no Android:**
1. Converta cada SVG em **Vector Drawable** (Android Studio → *File ▸ New ▸ Vector Asset ▸ Local file*), gerando `res/drawable/ic_launcher_background.xml` e `res/drawable/ic_launcher_foreground.xml`.
2. Em `res/mipmap-anydpi-v26/ic_launcher.xml` e `ic_launcher_round.xml`:
   ```xml
   <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
       <background android:drawable="@drawable/ic_launcher_background"/>
       <foreground android:drawable="@drawable/ic_launcher_foreground"/>
       <monochrome android:drawable="@drawable/ic_launcher_foreground"/>
   </adaptive-icon>
   ```
   (`monochrome` habilita o ícone temático do Android 13+.)
3. Opcional: gerar PNGs por densidade (mdpi→xxxhdpi) a partir do `ic_launcher.svg` se precisar de fallback legado.

**Notas de desenho:** traço 100% branco `#FFFFFF`; manter o símbolo dentro do círculo de 66dp; não adicionar sombra. Em tamanhos muito pequenos (≤24dp) a mão continua legível por ser um único traço cheio.

---

## 2) Paleta de cores — Material 3 (semente verde)

Tema **padrão do app: verde, modo claro**. Há também a variante escura. O app suporta **Dynamic Color** (Android 12+) — quando ligado, a paleta vem do wallpaper; quando desligado, usa estas cores como base.

### Light (padrão)
| Role | Hex |
|---|---|
| primary | `#107A39` |
| onPrimary | `#FFFFFF` |
| primaryContainer | `#1ED760` |
| onPrimaryContainer | `#00210E` |
| secondary | `#3E6B4B` |
| onSecondary | `#FFFFFF` |
| secondaryContainer | `#C2F0CD` |
| onSecondaryContainer | `#00210E` |
| tertiary | `#1C6B4A` |
| onTertiary | `#FFFFFF` |
| tertiaryContainer | `#A6F2C6` |
| onTertiaryContainer | `#002112` |
| background | `#FBFDF7` |
| onBackground | `#191C19` |
| surface | `#FBFDF7` |
| onSurface | `#191C19` |
| surfaceContainerLowest | `#FFFFFF` |
| surfaceContainerLow | `#F2F6EF` |
| surfaceContainer | `#ECF1E9` |
| surfaceContainerHigh | `#E6EBE3` |
| surfaceContainerHighest | `#E0E6DD` |
| onSurfaceVariant | `#414941` |
| outline | `#717971` |
| outlineVariant | `#C0C9BF` |
| error | `#BA1A1A` |
| onError | `#FFFFFF` |
| errorContainer | `#FFDAD6` |
| onErrorContainer | `#410002` |

### Dark
| Role | Hex |
|---|---|
| primary | `#1ED760` |
| onPrimary | `#00390F` |
| primaryContainer | `#0A8A3F` |
| onPrimaryContainer | `#C7F7D2` |
| secondary | `#A6D3B2` |
| onSecondary | `#0E3320` |
| secondaryContainer | `#234A33` |
| onSecondaryContainer | `#C2F0CD` |
| tertiary | `#86D6AC` |
| onTertiary | `#00391F` |
| tertiaryContainer | `#004F30` |
| onTertiaryContainer | `#A6F2C6` |
| background | `#101510` |
| onBackground | `#E0E6DD` |
| surface | `#101510` |
| onSurface | `#E0E6DD` |
| surfaceContainerLowest | `#0A0F0A` |
| surfaceContainerLow | `#181D18` |
| surfaceContainer | `#1A211B` |
| surfaceContainerHigh | `#252B25` |
| surfaceContainerHighest | `#303630` |
| onSurfaceVariant | `#C0C9BF` |
| outline | `#8A938A` |
| outlineVariant | `#414941` |
| error | `#FFB4AB` |
| onError | `#690005` |
| errorContainer | `#93000A` |
| onErrorContainer | `#FFDAD6` |

### Modo Palco (paleta fixa, independe do tema)
Fundo preto OLED para performance no palco.
| Uso | Hex |
|---|---|
| background | `#000000` |
| texto | `#F2F2F2` |
| texto secundário (dim) | `#8A8A8A` |
| acordes (destaque) | `#1ED760` |

### Marca / ícone
| Uso | Hex |
|---|---|
| verde claro | `#1ED760` |
| verde Spotify | `#1DB954` |
| verde escuro | `#12953F` |
| traço (foreground) | `#FFFFFF` |

---

## Snippet Compose (ColorScheme)

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme

val LightColors = lightColorScheme(
    primary = Color(0xFF107A39), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1ED760), onPrimaryContainer = Color(0xFF00210E),
    secondary = Color(0xFF3E6B4B), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC2F0CD), onSecondaryContainer = Color(0xFF00210E),
    tertiary = Color(0xFF1C6B4A), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFA6F2C6), onTertiaryContainer = Color(0xFF002112),
    background = Color(0xFFFBFDF7), onBackground = Color(0xFF191C19),
    surface = Color(0xFFFBFDF7), onSurface = Color(0xFF191C19),
    surfaceVariant = Color(0xFFDDE5DA), onSurfaceVariant = Color(0xFF414941),
    outline = Color(0xFF717971), outlineVariant = Color(0xFFC0C9BF),
    error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF1ED760), onPrimary = Color(0xFF00390F),
    primaryContainer = Color(0xFF0A8A3F), onPrimaryContainer = Color(0xFFC7F7D2),
    secondary = Color(0xFFA6D3B2), onSecondary = Color(0xFF0E3320),
    secondaryContainer = Color(0xFF234A33), onSecondaryContainer = Color(0xFFC2F0CD),
    tertiary = Color(0xFF86D6AC), onTertiary = Color(0xFF00391F),
    tertiaryContainer = Color(0xFF004F30), onTertiaryContainer = Color(0xFFA6F2C6),
    background = Color(0xFF101510), onBackground = Color(0xFFE0E6DD),
    surface = Color(0xFF101510), onSurface = Color(0xFFE0E6DD),
    surfaceVariant = Color(0xFF414941), onSurfaceVariant = Color(0xFFC0C9BF),
    outline = Color(0xFF8A938A), outlineVariant = Color(0xFF414941),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun TapCifrasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,                 // padrão do app: Dynamic Color ligado
    content: @Composable () -> Unit,
) {
    val ctx = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, typography = Typography, content = content)
}
```

> **Atenção:** o **Modo Palco** NÃO usa o `MaterialTheme` acima — ele tem paleta fixa (preto + `#1ED760`) para máxima legibilidade no palco, em qualquer tema. As `surfaceContainer*` do M3 expandido podem ser aplicadas via `MaterialTheme` (M3 1.2+); o `surfaceVariant` no snippet é um aproximado para versões anteriores.

## Arquivos deste pacote
- `preview.html` — visual do ícone + paleta completa (swatches).
- `ic_launcher_background.svg`, `ic_launcher_foreground.svg`, `ic_launcher.svg` — ícone adaptativo.
- `README.md` — este documento.
