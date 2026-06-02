# TODO — Primeira versão do Cifras no celular

Checklist tático para sair daqui (esqueleto pronto) até o app rodando no seu
Xiaomi 14. Marque cada item conforme for fazendo.

> Roadmap mais amplo (marcos 2, 3, futuro) fica em [`docs/proximos-passos.md`](./docs/proximos-passos.md).

---

## 1. Pré-requisitos no Mac ✅ FEITO em 2026-06-01

- [x] Android Studio instalado via `brew install --cask android-studio`
- [x] OpenJDK 17 via `brew install openjdk@17`
- [x] `~/.zshrc` configurado com `JAVA_HOME`, `ANDROID_HOME`, `PATH`
- [x] Licenças do SDK aceitas no wizard inicial do Android Studio
- [x] Android SDK Platform 35 + Build-Tools (Platform 35 foi auto-baixado pelo AGP no primeiro build)

## 2. Gerar o `gradle-wrapper.jar` ✅ FEITO

- [x] `gradle wrapper --gradle-version 8.10.2` (via `brew install gradle`)
- [x] `gradlew`, `gradlew.bat`, `gradle-wrapper.jar` commitados (commit `77f9bcc`)

## 3. Confirmar que builda no terminal ✅ FEITO

- [x] `./gradlew assembleDebug` → **BUILD SUCCESSFUL** (1m6s no primeiro build)
- [x] APK em `android/app/build/outputs/apk/debug/app-debug.apk` (24MB)

## 4. Preparar o Xiaomi 14 ✅ FEITO

- [x] Modo desenvolvedor ativado
- [x] Depuração USB ativada + autorizada o Mac
- [x] `adb devices` reconhece como `e26fa033  device` (Xiaomi 14 "houji_global")

## 5. Instalar no Xiaomi ✅ FEITO

> ⚠️ **MIUI/HyperOS bloqueia `./gradlew installDebug`** com `INSTALL_FAILED_USER_RESTRICTED`
> mesmo com todas as flags ativas. **Use `adb install` direto** — funciona perfeitamente.

Comando padrão para instalar nova versão daqui em diante:

```bash
cd /Users/gabrielmaruzi/GIT_GM/GM/android
./gradlew assembleDebug
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
# (opcional) abrir o app remotamente:
~/Library/Android/sdk/platform-tools/adb shell monkey -p com.gamaruzi.cifras.debug -c android.intent.category.LAUNCHER 1
```

- [x] App `com.gamaruzi.cifras.debug` instalado no Xiaomi 14
- [x] Versão 0.1.0 rodando: TopBar "Cifras" + campo de pesquisa + mensagem vazia

🎉 **Versão 0.1.0 do Cifras está no celular.**

---

## 6. Próximas features que tornam o app realmente útil (Marco 1)

Aqui o app deixa de ser esqueleto e vira algo que você usa de verdade.
Cada item daria um PR pequeno:

- [ ] Adicionar botão "Escolher pasta de cifras" usando o Storage Access Framework
      (`ActivityResultContracts.OpenDocumentTree`)
- [ ] Persistir a `URI` da pasta escolhida em DataStore (lembrar do
      `takePersistableUriPermission`, senão a URI vira inválida depois de reiniciar)
- [ ] Listar arquivos `.txt` da pasta usando `DocumentFile.fromTreeUri()`
- [ ] Tela de detalhe que mostra o conteúdo do arquivo em fonte monospace
- [ ] Filtrar a lista da tela "Pesquisar" pelo nome do arquivo conforme você digita
- [ ] Criar pasta `Cifras/` no celular, jogar 3-4 arquivos `.txt` de teste lá,
      apontar o app para ela e validar que aparece tudo

Quando isso estiver pronto, sobe a `versionName` para `0.2.0` em
`android/app/build.gradle.kts` e a gente celebra.

---

## 7. Distribuição (futuro, **só depois** do app valer a pena)

- [ ] Gerar keystore de release (`keytool -genkey -v ...`) — guardar **fora** do repo
- [ ] Configurar assinatura de release no `app/build.gradle.kts` com
      `keystore.properties` (gitignorado)
- [ ] Decidir: APK por sideload (grátis) ou Play Console ($25 USD único)?
      Atual recomendação: sideload até a 1.0, Play só se for compartilhar com outros.

## O que NÃO faz parte da primeira versão

Para não escapular escopo, **fora** do MVP:
- Sincronização em nuvem
- Suporte a PDF (entra no Marco 2)
- Transposição de tom
- Modo palco (auto-scroll, fonte gigante)
- Qualquer coisa que precise de internet
