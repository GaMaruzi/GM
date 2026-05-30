# Segurança e Custos

Política do Cifras App. Vale também para qualquer outro código neste repo.

## Custo zero é requisito de produto

O Gabriel exigiu **zero gastos não planejados**. Não é "evitar quando possível",
é hard requirement.

| Categoria | Permitido? | Por quê |
|-----------|-----------|---------|
| Firebase (qualquer produto) | ❌ | Free tier tem limites; pode estourar sem aviso |
| Crashlytics, Sentry, Bugsnag pagos | ❌ | Custo recorrente |
| Google Analytics for Firebase | ❌ | Telemetria + risco de cobrança |
| APIs de terceiros "gratuitas" | ❌ por padrão | Pedir antes — muitos têm tier pago surpresa |
| Bibliotecas com chave de API | ❌ | Sempre tem custo escondido eventual |
| Google Play Console (publicar) | ⚠️ | $25 USD uma vez — pedir antes |
| Deps no Maven Central / Google Maven | ✅ | Mas verificar manutenção ativa |
| GitHub Actions (tier público) | ✅ | Free para repos públicos |

Se você (Claude) estiver tentado a sugerir algo da lista vermelha, **pare e
pergunte primeiro**.

## Permissões no AndroidManifest

| Permissão | Status | Por quê |
|-----------|--------|---------|
| `INTERNET` | ❌ não declarada | O app é offline por design |
| `READ_EXTERNAL_STORAGE` | ❌ não declarada | Usamos Storage Access Framework |
| `WRITE_EXTERNAL_STORAGE` | ❌ não declarada | Não escrevemos no storage do usuário |

Para ler cifras: o usuário escolhe uma pasta via `Intent.ACTION_OPEN_DOCUMENT_TREE`.
SAF dá acesso persistente sem nenhuma permissão declarada.

Se um dia for adicionar permissão nova, justifique no PR e atualize esta tabela.

## Segredos no repositório

Nunca commitar:
- `*.keystore`, `*.jks` — chaves de assinatura do APK
- `keystore.properties` — senha do keystore
- `local.properties` — caminhos absolutos do SDK
- `google-services.json` — caso algum dia o Firebase entre (não deve)
- Tokens, mesmo de serviços gratuitos

O `.gitignore` raiz já bloqueia tudo isso. Antes de cada commit, confira com
`git status` se não tem nada suspeito.

**Em caso de vazamento acidental:**
1. Revogue a chave/token no serviço **primeiro**.
2. Remova do código.
3. (Opcional) limpe do histórico com `git filter-repo`. Mas o passo 1 é o que importa.

## Build de release

Configurado no `app/build.gradle.kts`:
- `isMinifyEnabled = true` — R8 ofusca e reduz o APK
- `isShrinkResources = true` — remove drawables/strings não usados
- `applicationIdSuffix = ".debug"` no debug — instala lado a lado do release

## O que NÃO está coberto aqui (ainda)

- Assinatura de release com keystore — quando for publicar, criamos `keystore.properties`
  local (gitignorado) e documentamos.
- Threat model — o app é local-only, então o modelo de ameaças é basicamente:
  outro app malicioso lendo os arquivos da pasta de cifras. SAF mitiga.
