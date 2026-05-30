# Repositório GM — Instruções para Claude Code

Repo pessoal do Gabriel. Contém o app **Cifras** (Android) em `/android` e
arquivos pessoais avulsos na raiz (ex: `bolo_de_cenoura.txt`).

## Comunicação
- Responda em **português brasileiro**.
- Termos técnicos podem ficar em inglês (branch, commit, ViewModel, Compose, etc.).

## Regras de ouro (NÃO violar sem perguntar)

1. **Custo zero.** Nada de Firebase, Crashlytics, APIs pagas, bibliotecas que
   exigem chave de API, analytics. Detalhes em `docs/seguranca-e-custos.md`.
2. **App offline.** Não declare permissão `INTERNET` no `AndroidManifest.xml`
   sem aprovação explícita do Gabriel.
3. **Começar simples.** Não introduza Hilt, Room, multi-módulo, KMP, Coroutines
   avançados, ou DSLs próprias até o Gabriel pedir. Veja `docs/proximos-passos.md`
   para o roadmap real.
4. **Sem segredos no repo.** `*.keystore`, `*.jks`, `keystore.properties`,
   `local.properties`, tokens — tudo gitignorado. Antes de commitar, dê uma olhada.

## Estrutura do repo

```
GM/
├── android/                    # projeto Android Studio (Kotlin + Compose)
│   ├── app/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle/libs.versions.toml
├── docs/
│   ├── arquitetura.md
│   ├── seguranca-e-custos.md
│   └── proximos-passos.md
├── .github/
│   ├── workflows/android-ci.yml
│   └── PULL_REQUEST_TEMPLATE.md
├── .claude/                    # configs locais do Claude Code (gitignorada)
├── CLAUDE.md                   # este arquivo
├── README.md
├── .gitignore
└── bolo_de_cenoura.txt         # receita pessoal — não mexer
```

## Comandos comuns

| Tarefa | Comando |
|--------|---------|
| Build debug | `cd android && ./gradlew assembleDebug` |
| Lint | `cd android && ./gradlew lint` |
| Testes unitários | `cd android && ./gradlew test` |
| Instalar no celular USB | `cd android && ./gradlew installDebug` |
| Limpar | `cd android && ./gradlew clean` |

## Antes de adicionar dependência nova
1. É mantida ativamente? (último release < 12 meses)
2. Envia telemetria? Se sim, **veta**.
3. Tem custo direto ou indireto? Se sim, **pergunta o Gabriel**.
4. Aumenta o APK em quanto? Cita o número no PR.

## Antes de mexer no `AndroidManifest.xml`
Permissões e features novas precisam justificativa. O app é offline e quer
permanecer assim. Storage Access Framework cobre leitura de arquivos sem
permissão.

## Quando você ficar em dúvida
Pergunte. Gabriel prefere uma pergunta a um PR desfeito.
