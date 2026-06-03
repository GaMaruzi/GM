# Handoff: Tap Cifras (MVP — Marco 1)

App Android **offline-first** para ler cifras de uma pasta local do celular, com **Modo Palco** (navegação por toque) como recurso-assinatura.

---

## Sobre os arquivos deste pacote

Os arquivos `.html` / `.jsx` aqui são **referências de design feitas em HTML/React** — protótipos que mostram aparência e comportamento pretendidos. **Não são código de produção para copiar.** A tarefa é **recriar estas telas no codebase-alvo** (Android nativo) usando seus padrões estabelecidos.

> **Stack-alvo (fixo):** Kotlin + Jetpack Compose + Material 3 (`androidx.compose.material3`). minSdk 28, targetSdk 35. Navigation Compose com NavHost único. `enableEdgeToEdge()`. Acesso à pasta via **Storage Access Framework** (`ACTION_OPEN_DOCUMENT_TREE` → `DocumentFile`), sem permissões declaradas.

## Fidelidade

**Hi-fi.** Cores, tipografia, espaçamentos e interações são finais e alinhados ao Material 3. Recrie pixel-a-pixel usando `MaterialTheme` + componentes M3 nativos (`SearchBar`, `TopAppBar`, `FilledButton`, `ExtendedFloatingActionButton`, `ListItem`, `FilterChip`, `DropdownMenu`, `ModalBottomSheet`, `Switch`).

---

## Design Tokens

### Cor — usar `dynamicColorScheme()` quando disponível (Android 12+); senão o fallback abaixo

O protótipo demonstra **Dynamic Color**: a paleta deriva do wallpaper. Em Compose:

```kotlin
val colorScheme = when {
  dynamicEnabled && Build.VERSION.SDK_INT >= 31 ->
    if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
  dark -> DarkColors   // fallback violeta abaixo
  else -> LightColors
}
```

**Fallback "Violeta" (baseline M3) — Light:**
| Role | Hex |
|---|---|
| primary | `#6750A4` |
| onPrimary | `#FFFFFF` |
| primaryContainer | `#EADDFF` |
| onPrimaryContainer | `#21005D` |
| secondary | `#625B71` |
| secondaryContainer | `#E8DEF8` |
| onSecondaryContainer | `#1D192B` |
| tertiary | `#7D5260` |
| tertiaryContainer | `#FFD8E4` |
| surface | `#FEF7FF` |
| surfaceContainerHigh | `#ECE6F0` |
| surfaceContainerHighest | `#E6E0E9` |
| onSurface | `#1D1B20` |
| onSurfaceVariant | `#49454F` |
| outline | `#79747E` |
| outlineVariant | `#CAC4D0` |

**Fallback "Violeta" — Dark:**
| Role | Hex |
|---|---|
| primary | `#D0BCFF` |
| onPrimary | `#381E72` |
| primaryContainer | `#4F378B` |
| onPrimaryContainer | `#EADDFF` |
| secondaryContainer | `#4A4458` |
| surface | `#141218` |
| surfaceContainerHigh | `#2B2930` |
| surfaceContainerHighest | `#36343B` |
| onSurface | `#E6E0E9` |
| onSurfaceVariant | `#CAC4D0` |
| outline | `#938F99` |
| outlineVariant | `#49454F` |

> O arquivo `m3.jsx` contém **três** paletas semente completas (violeta/verde/âmbar, light+dark) usadas para demonstrar Dynamic Color. Em produção basta a baseline + `dynamicColorScheme()`.

**Modo Palco — paleta fixa (independe do tema):** fundo `#000000` (OLED), texto `#F2F2F2`, dim `#8A8A8A`, acordes = `primary` do tema (no dark, `#D0BCFF`).

### Tipografia
- **UI:** Roboto (Material default) → `Typography` padrão do M3.
- **Conteúdo da cifra:** **monospace obrigatório** — JetBrains Mono ou Roboto Mono. Sem isso os acordes desalinham das sílabas.
- Escala usada nos protótipos:
  - Título top bar: 22sp / Roboto Medium (`titleLarge`)
  - Título tela vazia: 28sp `headlineMedium`
  - Item de lista (headline): 16sp `bodyLarge`; suporte: 13sp `bodyMedium` onSurfaceVariant
  - Label de seção: 12sp/700, `letterSpacing 1`, uppercase, cor primary (`labelMedium` custom)
  - **Cifra (Detail):** 16sp mono ajustável 12–28sp (passo 1)
  - **Cifra (Palco):** 28sp mono ajustável 18–44sp (passo 2)
  - Tags de seção (INTRO/VERSO/REFRÃO): 11sp/700, uppercase, `letterSpacing 1.5`, onSurfaceVariant

### Forma / Espaçamento
- Grid base **8dp** (paddings de tela 16–24dp; itens de lista 56dp de altura).
- Corner radius M3: search bar/botões = `full`; FAB = 16dp; cards/sheets = 16–28dp; chips = 8dp.
- ModalBottomSheet topo: 28dp. Menu suspenso: 12dp.
- Touch targets **≥ 48dp** (no Palco a tela inteira é alvo).
- Edge-to-edge: conteúdo desenha sob a status bar (inset top ~44dp) e barra de gestos (inset bottom). Use `WindowInsets`/`Scaffold` para os paddings reais.

---

## Telas (Marco 1)

### 1. Primeiro uso / vazio — `Empty.jsx`
- **Propósito:** estado quando nenhuma pasta foi escolhida.
- **Layout:** coluna centralizada. Ilustração (folder tonal `primaryContainer` rotacionada -6° + badge nota em `tertiaryContainer`), título 28sp, parágrafo 15sp `onSurfaceVariant` (max ~280dp), botão `FilledButton` grande (56dp) "Escolher pasta de cifras" com ícone folder.
- **Rodapé:** ícone shield + "Sem internet · sem login · sem anúncios".
- **Ação:** botão dispara `ACTION_OPEN_DOCUMENT_TREE`; ao retornar a uri, persistir com `takePersistableUriPermission` e navegar para Search.

### 2. Pesquisar (home) — `Search.jsx`
- **Propósito:** buscar/abrir cifras da pasta indexada.
- **Layout (top→bottom):** inset status bar → **SearchBar M3** (56dp, `surfaceContainerHigh`, leading=search, trailing= `more` quando vazia / `close` quando há texto) → linha de status "📁 {pasta} · {N} cifras indexadas" → **FilterChips**: Todas / Favoritas(★) / Recentes(history) → **lista** de arquivos.
- **Item de lista:** ícone `description` em quadrado `surfaceContainerHighest` 40dp · título (nome do arquivo, 16sp) · subtítulo "{artista} · TXT · tom {key}" · trailing ★ (toggle favorito). Tap no item → Detail.
- **Estado vazio:** ícone + mensagem contextual (busca sem resultado / sem favoritos).
- **FAB estendido** (canto inf. direito): "Modo palco" (ícone fullscreen) → tela Setlist.
- **Overflow menu (`more`):** Configurações · Trocar pasta · Tema claro/escuro.
- **Busca:** filtra conforme digita sobre nome do arquivo + gênero (case-insensitive).

### 3. Detalhe da cifra — `Detail.jsx`
- **Propósito:** ler uma cifra.
- **TopBar:** back · título(música)+subtítulo(artista) · ★ · overflow.
- **Barra de leitura:** stepper **Transpor** (− {tom atual} +, em mono/cor primary) + atalho "↺ original" quando ≠ 0; à direita stepper **Tamanho de fonte** (− `textSize` +).
- **Corpo:** scroll vertical, monospace. Acordes em **`primary` + bold**; letras em `onSurface`. Linha de acordes preserva alinhamento por coluna (whitespace pré-formatado). Chip de capotraste (`tertiaryContainer`) quando `capo > 0`. Rodapé discreto com o nome do arquivo.
- **FAB estendido:** "Tocar no palco" → entra no Modo Palco já nessa música.
- **Persistência:** posição de scroll salva por música (`tc:pos:<id>`).

### 4. Setlist (montar sequência do Palco) — `Setlist.jsx`
- **Propósito:** escolher quais cifras e em que ordem o Modo Palco vai exibir, e **pré-configurar a rolagem de cada uma**.
- **Seção "Na sequência":** itens reordenáveis (handle drag + setas ▲▼), índice numerado em círculo `primary`, e **controle de velocidade de rolagem por música** (− valor + ; 0 = "off"). Botão remover.
- **Seção "Adicionar cifras":** lista das demais músicas; tap adiciona (ícone +).
- **Barra inferior:** `FilledButton` grande "Iniciar palco · {N}" (desabilitado se vazio).
- **Persistência:** `tc:setlist` (array de ids) e `tc:speeds` (mapa id→velocidade).

### 5. Modo Palco — `Stage.jsx`  ⭐ recurso-assinatura
- **Propósito:** performance. Fundo preto, fonte gigante, **tela sempre acesa**, auto-scroll pré-configurado.
- **Gesto (núcleo):** toque em **qualquer lugar** da superfície de leitura → **próxima** música. **Dois toques** → música **anterior**. (Detecção: timer ~280ms; 2º toque dentro da janela cancela o "single" pendente e volta.)
- **Auto-scroll:** ao entrar numa música o scroll vai ao topo e rola na velocidade **pré-configurada daquela música** (do setlist). Ajustável ao vivo (persiste).
- **Chrome (auto-some após ~3,2s):** topo = botão fechar(X) · indicador "{i} / {N}" · controle de velocidade · A−/A+. Inferior = dots de progresso do setlist.
- **Dica inicial:** overlay central "1× próxima / 2× anterior — toque em qualquer lugar", some ao primeiro toque.
- **Compose:** `WakeLock` via `keepScreenOn`/`FLAG_KEEP_SCREEN_ON`; `pointerInput { detectTapGestures(onTap = …, onDoubleTap = …) }`; auto-scroll com `LazyColumn`/`ScrollState.animateScrollBy` num loop `LaunchedEffect`.

### 6. Configurações — `Settings.jsx`
- Pasta de cifras (nome + contagem + botão "Trocar") · **Tema** (segmented: Sistema/Claro/Escuro) · toggle **Cores dinâmicas** · card de **Privacidade** ("Este app não acessa a internet…") · Sobre (versão).

---

## Interações & Navegação

```
Empty --pick folder--> Search
Search --tap item--> Detail
Search --FAB--> Setlist --Iniciar--> Stage
Search --overflow--> Settings / Empty(trocar pasta) / toggle tema
Detail --FAB "Tocar no palco"--> Stage (insere a música no topo do setlist)
Stage  --X--> volta (Detail ou Setlist)
```

- **Tap-to-advance** (Palco): single = próxima, double = anterior. Único gesto crítico do app.
- Transições: padrão Navigation Compose (shared-axis/fade). Chrome do Palco: fade 0.4s.
- Reduced-motion: sem animações decorativas infinitas; conteúdo sempre legível.

## Estado (persistente — DataStore/Room em produção)
| Chave (protótipo) | Conteúdo |
|---|---|
| `tc:folder` | uri/flag da pasta SAF escolhida |
| `tc:favs` | Set de ids favoritos |
| `tc:recents` | array de ids (máx 12, MRU) |
| `tc:setlist` | array de ids na ordem do palco |
| `tc:speeds` | mapa id → velocidade de rolagem (0–6) |
| `tc:pos:<id>` | posição de scroll por música |
| tema | modo (sistema/claro/escuro) + dynamic on/off |

**Parsing de cifra:** cada arquivo `.txt` vira `{ sections: [{ tag, lines: [{ ch, ly }] }] }`. Heurística: linha é "de acordes" se, após tokenizar por espaços, todos os tokens casam `^[A-G][#b]?(m|maj|min|sus|dim|aug|add|°|/|\d)*$`; a linha seguinte é a letra associada. Tags (INTRO/VERSO/REFRÃO/PONTE) viram `tag` da seção.

**Transposição:** ver `theory.jsx` — `transposeChordLine()` preserva colunas; usa sustenidos/bemóis conforme o tom. Reaproveite a lógica (mapa de 12 semitons + escolha sharp/flat por tom).

## Princípios não-negociáveis
1. **Offline-first visual** — nada de cloud/sync/share online. Status só local.
2. **Sem onboarding longo** — 1 tela + 1 botão.
3. **Privacidade explícita** — card no Settings.
4. **Densidade alta na leitura** — conteúdo > espaçamento decorativo.
5. Copy **100% PT-BR**.

## Fora de escopo (não implementar)
Login/conta · pagamento/premium · push · integração Spotify/YouTube · compartilhamento social · tutorial multi-tela.

## Assets
- **Ícone adaptativo** (`App Icon.html`): foreground (nota + ondas de "tap", branco) + background (gradiente violeta `#5B43A6`→`#3A2E5C`→`#241B3D`). Exportar como `ic_launcher_foreground`/`ic_launcher_background` (vector drawable ou PNG mdpi→xxxhdpi). Zona segura central 66dp.
- Ícones de UI: equivalem a **Material Symbols (Rounded)** — usar a fonte/biblioteca oficial no app.

## Arquivos neste pacote
- `Tap Cifras.html` — app completo navegável (entrada).
- `m3.jsx` — paletas M3 (3 sementes, light+dark) + frame edge-to-edge.
- `icons.jsx` — subset de ícones (mapear p/ Material Symbols).
- `ui.jsx` — componentes M3 (Button, FAB, TopBar, Chip, Menu, Sheet, Switch…).
- `render.jsx` — renderizador da cifra (acordes destacados).
- `theory.jsx` — transposição de acordes/tom.
- `songs.jsx` — dados demo (letras originais fictícias; substituir por parser de arquivos).
- `Empty/Search/Detail/Setlist/Stage/Settings.jsx` — telas.
- `App Icon.html` — especificação do ícone adaptativo.
```
```

---
_Cifras demo são de autoria original (artistas fictícios) só para preencher o protótipo — não há conteúdo de terceiros. A biblioteca real vem dos arquivos do usuário._
