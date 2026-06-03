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

### ~~PR 1 — SAF picker + DataStore para a URI da pasta~~ ✅ (substituído)
Originalmente o app guardava **uma URI de pasta** (`OpenDocumentTree`) e indexava
todos os `.txt` dela. Substituído em 2026-06-03 pelo modelo **biblioteca interna**
(ver PR C "multimodal"): o usuário adiciona arquivos individualmente via Photo
Picker e `OpenMultipleDocuments`, cada URI é persistida via
`takePersistableUriPermission` em uma lista no DataStore.

### PR 2 — Parser `.txt` → `Song` ✅
Implementado em `94ccfb0`. Continua válido após o refactor multimodal — o
`CifraTextParser` roda quando `Song.format == TEXT`.

### PR 3 — Favoritos + Recentes persistentes ✅
Entregue. `UserPreferences` ganhou `favorites: Flow<Set<String>>` e
`recents: Flow<List<String>>` (codec dedicado em `RecentsCodec` com regra MRU
testada). `AppState` consome diretamente; toggle favorito e markRecent vão
pro DataStore. `removeEntry` e `pruneOrphans` limpam favoritos/recentes
órfãos automaticamente quando a biblioteca muda.

### PR 4 — Transposição funcional no Detail ✅
Entregue. `domain/Theory.kt` é o port completo de `theory.jsx` (puro, sem
dependência de Android). `transposeChordLine` preserva colunas com a mesma
regra do JSX (ancora cada token, empurra com espaço só em colisão).
`keyPrefersFlat` escolhe `#` ou `b` conforme convenção do tom original.
Steppers do Detail aplicam de verdade. Transposição reseta ao trocar de
música (não persiste — conforme decisão default).

### PR 5 — Settings funcional ✅
Entregue. Cinco seções (Biblioteca, Aparência, Privacidade, Sobre), todas
persistidas em DataStore via `UserPreferences.themeMode` / `dynamicColor`.
Tema aplica imediato (StateFlow lido no `MainActivity`). Material You é
opt-in com Switch desabilitado em pré-Android 12. Remoção individual fica
no menu da Search (não duplicada aqui); o botão "Limpar biblioteca"
existe com dialog de confirmação. `BuildConfig.VERSION_NAME` exposto via
`buildFeatures.buildConfig = true`.

### PR 6 — Posição de scroll por música ✅
Entregue. `ScrollCodec` persiste `Map<songId, offsetPx>` em DataStore com
mesma convenção de codec dos outros (separador U+0001). `DetailScreen`
restaura ao montar (re-aplica se `maxValue` cresce — necessário para o
PdfContent que mede tudo depois). Salva com `snapshotFlow + debounce(500ms)`.
`pruneOrphans` é chamado ao mudar a biblioteca; `removeEntry` também
limpa o offset da cifra excluída.

Ao fim do Marco 2: subir `versionName` para **0.3.0**. O app já é
usável de verdade — leitura de cifras locais, com favoritos, configs e busca.

## Marco 3 — Stage Mode (feature-assinatura)
O recurso que justifica o nome "Tap Cifras". Sozinho em um marco porque é
complexo e crítico.

### PR 7 — Setlist editor ✅
Entregue. `SetlistCodec` (puro, com `moveUp`/`moveDown`/`add`/`remove`/
`pruneOrphans`). DataStore: `setlist_v1` e `speeds_v1` — speeds reusa o
`ScrollCodec` (mesma estrutura Map<URI,Int>). Tela com lista ordenada,
setas ▲▼, botão remover, steppers de velocidade (−/valor/+, range
0–120 px/s em passos de 10, 0=off), botão "Adicionar cifras" abrindo
dialog com candidatas, FAB "Iniciar palco · N" desabilitado quando vazio,
ícone de lixeira na TopAppBar para limpar tudo com confirmação.

### PR 8 — Stage Mode (UI) ✅
Entregue. `StageScreen` substitui o placeholder com fullscreen
(`WindowInsetsControllerCompat.hide(systemBars)`), `keepScreenOn=true`,
paleta fixa (preto/F2F2F2/1ED760 — independente do tema do app), e
renderiza TEXT/IMAGE/PDF via os mesmos helpers do Detail. Fonte mono
28sp ajustável 18–44sp em passos de 2. Helper `Context.findActivity()`
em `ui/common/ContextExt.kt` desce os ContextWrappers até a Activity.

### PR 9 — Stage Mode (interação) ✅
Entregue na mesma iteração do PR 8. `detectTapGestures` no Box do
conteúdo (não bloqueia drag → scroll vertical continua) para tap=próxima
e double-tap=anterior. Auto-scroll: `LaunchedEffect(song.id, velocidade,
maxValue)` rola `animateScrollBy(velocidade px, tween(1000ms, linear))`
enquanto não chega ao fim — relê velocidade do setlist (PR 7). Chrome
(TopBar + BottomBar + dots) com `AnimatedVisibility(fadeIn/fadeOut)`
+ `delay(3200ms)` pra esconder. Reaparece a cada troca de música ou
ao apertar A−/A+. Dots de progresso (ativo grande verde, vistos cinza,
futuros translúcidos). Overlay de dica inicial centralizado que some
após 5s ou ao toque.

`versionName` subiu para **1.0.0** (versionCode 2).

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
- ~~Suporte a `.pdf` via `PdfRenderer` nativo~~ ✅ entregue no multimodal
- Auto-detecção de tom a partir do conteúdo (não só primeira linha)
- Compartilhar cifra como imagem (mas sem analytics no compartilhamento!)
- Exportar setlist para PDF
- Sincronização **opcional** com pen drive USB OTG (ainda sem rede)
- Login/senha + backup da biblioteca (quando houver) — habilita migração
  para celular novo; até lá, troca de aparelho requer re-selecionar arquivos
- Zoom (pinch) nas imagens/PDF dentro do Detail
- Crop/rotate de imagens recém-adicionadas para enquadrar melhor a cifra
