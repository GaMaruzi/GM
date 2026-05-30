# Próximos Passos — Cifras App

Roadmap deliberadamente conservador. Faça o marco atual funcionar antes de
sonhar com o próximo.

## Marco 0 — Esqueleto (você está aqui)
- [x] Projeto Android Compose configurado
- [x] Tela "Pesquisar" estática com `OutlinedTextField` e `LazyColumn`
- [x] Tema Material 3 com cores dinâmicas (Android 12+)
- [ ] Primeiro build no Android Studio (`./gradlew assembleDebug`)
- [ ] Primeiro `installDebug` no Xiaomi 14

## Marco 1 — MVP local
- [ ] Botão "Escolher pasta de cifras" usando `ActivityResultContracts.OpenDocumentTree`
- [ ] Persistir `URI` da pasta em DataStore (com `takePersistableUriPermission`)
- [ ] Listar arquivos `.txt` da pasta (usando `DocumentFile.fromTreeUri`)
- [ ] Tela de detalhe que mostra o conteúdo em fonte monospace
- [ ] Pesquisa case-insensitive pelo nome do arquivo

## Marco 2 — Qualidade de vida
- [ ] Suporte a `.pdf` via `PdfRenderer` nativo
- [ ] Histórico de cifras vistas recentemente
- [ ] Favoritos
- [ ] "Modo palco": fonte gigante, fundo preto, tela sempre acesa enquanto a cifra
      está aberta (`window.addFlags(FLAG_KEEP_SCREEN_ON)`)

## Marco 3 — Refatorar quando doer
Só quando a dor for real, não preventivamente:
- [ ] Migrar de DataStore para **Room** quando passar de ~500 cifras ou precisar
      de busca full-text.
- [ ] Adicionar **Hilt** quando tiver 3+ ViewModels com dependências reais.
- [ ] Quebrar em módulos `:data`, `:domain`, `:ui` quando o app passar de
      ~10k linhas de código Kotlin.

## Bloqueado / requer aprovação do Gabriel
- Publicar na Play Store ($25 USD)
- Adicionar qualquer dependência de rede
- Adicionar analytics de qualquer tipo
- Adicionar SDK que faça chamada de rede em background

## Ideias parqueadas (sem prioridade)
- Transposição de tom (parser de acordes)
- Auto-scroll temporizado para tocar sem encostar no celular
- Compartilhar cifra como imagem
- Exportar setlist para PDF
