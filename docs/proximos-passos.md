# Próximos Passos — Tap Cifras

Roadmap deliberadamente conservador. Faça o marco atual funcionar antes de
sonhar com o próximo.

## Marco 0 — Esqueleto ✅
- [x] Projeto Android Compose configurado e buildando
- [x] Tela "Pesquisar" estática
- [x] Tema Material 3 com cores dinâmicas
- [x] Build no Xiaomi 14 via USB (com workaround `adb install` — ver [`../TODO.md`](../TODO.md))

## Marco 1 — Refresh visual + navegação (Scope A) ✅
Tudo entregue no commit `a0d7389` em 2026-06-02.
- [x] Rebatizado para **Tap Cifras** + ícone adaptive gradient violet
- [x] Paleta M3 violeta baseline (light + dark) + Dynamic Color
- [x] `material-icons-extended` adicionado
- [x] Tela **Empty** com ilustração rotacionada
- [x] Tela **Search**: SearchBar M3, chips Todas/Favoritas/Recentes, lista, FAB
- [x] Tela **Detail**: TopBar, steppers transpor/fonte, corpo monospace
- [x] Stubs **Setlist / Stage / Settings** com placeholder consistente
- [x] AppState central + dados mock para 3 cifras
- [x] Handoff completo de design preservado em `docs/design/`

## Marco 2 — MVP funcional (Scope B)
Quebrado em PRs pequenos. Ordem sugerida segue dependências (cada um habilita o próximo).

### PR 1 — SAF picker + DataStore para a URI da pasta
- [ ] `ActivityResultContracts.OpenDocumentTree` no botão "Escolher pasta"
- [ ] `takePersistableUriPermission` (senão a URI vira inválida depois de reboot)
- [ ] DataStore Preferences guardando a URI + nome amigável da pasta
- [ ] `AppState.pickFolderMock()` vira `pickFolder(uri: Uri)` real
- [ ] Trocar pasta nas Configurações funciona

### PR 2 — Parser `.txt` → `Song`
- [ ] `TxtCifraParser` que lê arquivos via `DocumentFile.fromTreeUri()`
- [ ] Heurística de linha de acordes:
      `^[A-G][#b]?(m|maj|min|sus|dim|aug|add|°|/|\d)*$` (ver `docs/design/render.jsx`)
- [ ] Detecção de tags de seção (INTRO/VERSO/REFRÃO/PONTE)
- [ ] Detecção de tom inicial (primeira linha de acordes) + capotraste se sinalizado
- [ ] Cache: parse 1x por arquivo, invalidar quando `lastModified` muda
- [ ] Lista da Search lê do parser real, não do `SampleSongs`

### PR 3 — Favoritos + Recentes persistentes
- [ ] DataStore para `Set<String>` de favoritos
- [ ] DataStore para `List<String>` de recentes (MRU, máx 12)
- [ ] `AppState` consome flows do DataStore em vez de in-memory

### PR 4 — Transposição funcional no Detail
- [ ] Port de `docs/design/theory.jsx` → `domain/Theory.kt`
- [ ] `transposeChordLine()` preservando colunas (acordes mais longos ocupam mais colunas)
- [ ] `keyPrefersFlat()` para escolher entre `#` e `b` conforme o tom
- [ ] Steppers do Detail aplicam de verdade
- [ ] Persistir transposição por música? **Decisão em aberto** — o design não fala. Default: não persistir.

### PR 5 — Settings funcional
- [ ] Linha "Pasta de cifras" mostra nome + contagem + botão "Trocar"
- [ ] Segmented control de tema: Sistema / Claro / Escuro (persiste)
- [ ] Toggle "Cores dinâmicas" (persiste)
- [ ] Card de privacidade ("Este app não acessa a internet…")
- [ ] Item "Sobre" com versão (lê de `BuildConfig.VERSION_NAME`)

### PR 6 — Posição de scroll por música
- [ ] DataStore mapa `songId → scrollOffset`
- [ ] Detail restaura no resume, salva no scroll (debounce 500ms)
- [ ] Limpar entradas de IDs que não existem mais (quando a pasta muda)

Ao fim do Marco 2: subir `versionName` para **0.3.0**. O app já é
usável de verdade — leitura de cifras locais, com favoritos, configs e busca.

## Marco 3 — Stage Mode (feature-assinatura)
O recurso que justifica o nome "Tap Cifras". Sozinho em um marco porque é
complexo e crítico.

### PR 7 — Setlist editor
- [ ] Tela Setlist real: lista reordenável (drag handle + setas ▲▼)
- [ ] Controle de velocidade por música (− valor +, 0 = off)
- [ ] Botão "Adicionar cifras" — segunda lista de candidatas
- [ ] DataStore `tc:setlist` (List<String>) + `tc:speeds` (Map<String, Int>)
- [ ] Botão "Iniciar palco · {N}" no rodapé, desabilitado se vazio

### PR 8 — Stage Mode (UI)
- [ ] Fullscreen, fundo `#000000`, texto `#F2F2F2`, acordes em `primary`
- [ ] `WindowCompat.getInsetsController(...).hide(systemBars)` no entrar, restaurar no sair
- [ ] `keepScreenOn = true` enquanto a tela está ativa
- [ ] Renderização em fonte mono, escala 28sp ajustável 18-44sp

### PR 9 — Stage Mode (interação)
- [ ] `detectTapGestures(onTap = próxima, onDoubleTap = anterior)`
      com janela de ~280ms para resolver
- [ ] Auto-scroll com `LaunchedEffect` + `ScrollState.animateScrollBy`
      na velocidade pré-configurada da música atual
- [ ] Chrome (X, contador i/N, controles A−/A+) aparece e some em ~3.2s
- [ ] Dots de progresso do setlist no rodapé
- [ ] Overlay de dica inicial "1× próxima · 2× anterior"

Ao fim do Marco 3: subir `versionName` para **1.0.0** e considerar a 1.0
oficialmente pronta.

## Marco 4 — Refatorar quando doer
Só quando a dor for real, não preventivamente:
- [ ] Migrar de DataStore para **Room** quando o índice de cifras passar de
      ~500 arquivos ou precisar de busca full-text
- [ ] Adicionar **Hilt** quando tiver 4+ ViewModels com dependências reais
- [ ] Quebrar em módulos `:data`, `:domain`, `:ui` quando o app passar de
      ~10k LOC Kotlin
- [ ] Renomear package `com.gamaruzi.cifras` → `com.gamaruzi.tapcifras`
      (cuidado: muda applicationId; trata como migração)

## Bloqueado / requer aprovação
- Publicar na Play Store ($25 USD único)
- Adicionar qualquer dependência de rede
- Adicionar analytics de qualquer tipo
- Adicionar SDK que faça chamada de rede em background

## Ideias parqueadas (sem prioridade)
- Suporte a `.pdf` via `PdfRenderer` nativo
- Auto-detecção de tom a partir do conteúdo (não só primeira linha)
- Compartilhar cifra como imagem (mas sem analytics no compartilhamento!)
- Exportar setlist para PDF
- Sincronização **opcional** com pen drive USB OTG (ainda sem rede)
