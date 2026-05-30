# Arquitetura — Cifras App

## Stack

| Camada | Escolha | Motivo |
|--------|---------|--------|
| Linguagem | Kotlin 2.1 | Padrão atual do Android |
| UI | Jetpack Compose + Material 3 | Recomendação Google, menos código |
| Build | Gradle Kotlin DSL + version catalog | Tipado, refatorável |
| minSdk | 28 (Android 9) | Cobre 95%+ dos devices em uso |
| targetSdk | 35 (Android 15) | Compatível com HyperOS do Xiaomi 14 |
| Navegação | Navigation Compose | Único grafo, sem multi-módulo cedo |
| Persistência | (a definir — começar com DataStore) | Sem Room até precisar de queries |
| DI | Nenhuma | `viewModel()` do Compose chega para o MVP |
| Testes | JUnit 4 + Compose UI Test | Default do template Android |

Decisões de "não usar" são tão importantes quanto as de "usar".
Veja `seguranca-e-custos.md` e `proximos-passos.md`.

## Organização do código

Pacotes (não módulos) sob `com.gamaruzi.cifras`:

```
com.gamaruzi.cifras/
├── MainActivity.kt          # entry point, monta o tema e o NavHost
├── CifrasApplication.kt     # Application — sem inits de SDKs externos
├── ui/
│   ├── theme/               # Cores, tipografia, Theme.kt
│   ├── navigation/          # AppNavHost.kt, rotas
│   └── pesquisar/           # Tela "Pesquisar" + ViewModel
├── domain/                  # modelos de negócio (vazio agora)
└── data/                    # repositórios, leitura de arquivos (vazio agora)
```

Mantenha **uma feature por subpacote em `ui/`**. Cada subpacote contém a tela
Compose e o ViewModel dela. Quando o app crescer, repensamos.

## Fluxo de dados planejado

```
┌──────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ PesquisarScreen  │──▶ │ PesquisarVM      │──▶ │ CifrasRepository│
│ (Compose)        │    │ (StateFlow)      │    │ (data/)         │
└──────────────────┘    └──────────────────┘    └────────┬────────┘
                                                         │
                                                         ▼
                                                 ┌───────────────┐
                                                 │ SAF / arquivos│
                                                 │ locais .txt   │
                                                 └───────────────┘
```

O repositório lê arquivos da pasta escolhida pelo usuário via Storage Access
Framework. A `URI` da pasta é persistida em DataStore (não em SharedPreferences
de String solta — `URI` precisa de `takePersistableUriPermission`).

## Decisões em aberto

- **Quando migrar de DataStore para Room?** Quando passarmos de ~500 cifras
  indexadas ou precisarmos de busca full-text.
- **Suporte a PDF.** `PdfRenderer` nativo do Android (zero deps externas).
  Avaliar depois do MVP em texto.
- **Transposição de tom.** Feature futura, exige parser de acordes. Depois do MVP.
