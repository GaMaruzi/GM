# GM

Repositório pessoal do Gabriel Maruzi.

## Projetos

### Cifras — App Android
Leitor offline de cifras de músicas armazenadas localmente no celular
(Xiaomi 14 / Android 14+). Sem internet, sem analytics, sem cadastro.

- Código: [`/android`](./android)
- Arquitetura: [`docs/arquitetura.md`](./docs/arquitetura.md)
- Segurança & custos: [`docs/seguranca-e-custos.md`](./docs/seguranca-e-custos.md)
- Roadmap: [`docs/proximos-passos.md`](./docs/proximos-passos.md)

#### Como rodar
1. Instale o [Android Studio](https://developer.android.com/studio) (Hedgehog ou mais recente).
2. Abra a pasta `android/` no Android Studio (`File → Open → escolher pasta android`).
3. Aguarde o sync do Gradle. Na primeira vez, o Android Studio gera o `gradle-wrapper.jar`.
4. Conecte o Xiaomi 14 via USB com depuração ativada e clique em **Run**.

Alternativa via terminal (após primeiro sync no Android Studio):
```bash
cd android
./gradlew installDebug
```

### Outros arquivos pessoais
- [`bolo_de_cenoura.txt`](./bolo_de_cenoura.txt) — receita

## Para colaboradores (Claude Code)
Ver [`CLAUDE.md`](./CLAUDE.md). Resumo: comunicação em PT-BR, custo zero,
app offline-only, começar simples.
