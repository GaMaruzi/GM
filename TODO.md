# TODO — Primeira versão do Cifras no celular

Checklist tático para sair daqui (esqueleto pronto) até o app rodando no seu
Xiaomi 14. Marque cada item conforme for fazendo.

> Roadmap mais amplo (marcos 2, 3, futuro) fica em [`docs/proximos-passos.md`](./docs/proximos-passos.md).

---

## 1. Pré-requisitos no Mac (uma vez só)

- [ ] **Instalar o Android Studio** (versão Hedgehog ou mais recente)
  - Baixe em https://developer.android.com/studio
  - Durante o setup, deixe ele instalar o **Android SDK Platform 35** e o
    **Android SDK Build-Tools** mais recente.
- [ ] **Instalar o JDK 17** (o Android Studio costuma trazer um embutido — basta
      apontar `JAVA_HOME` para ele se for usar `./gradlew` no terminal)
- [ ] **Aceitar as licenças do SDK** quando o Android Studio pedir

## 2. Gerar o `gradle-wrapper.jar` (uma vez só)

O wrapper jar é binário e não foi commitado ainda. Sem ele, `./gradlew` não roda.

- [ ] Abrir o Android Studio
- [ ] **File → Open** → escolher a pasta `/Users/gabrielmaruzi/GIT_GM/GM/android`
- [ ] Aguardar o "Gradle sync" terminar (a primeira vez baixa ~500 MB de deps).
      O wrapper jar é gerado automaticamente em `android/gradle/wrapper/gradle-wrapper.jar`.
- [ ] Commitar o jar gerado:
  ```bash
  cd /Users/gabrielmaruzi/GIT_GM/GM
  git add android/gradle/wrapper/gradle-wrapper.jar android/gradlew android/gradlew.bat
  git commit -m "chore(android): adicionar gradle wrapper binário"
  ```

## 3. Confirmar que builda no terminal

- [ ] No terminal, na raiz do repo:
  ```bash
  cd android
  ./gradlew assembleDebug
  ```
- [ ] Saída esperada: `BUILD SUCCESSFUL`. O APK fica em
      `android/app/build/outputs/apk/debug/app-debug.apk`.
- [ ] Rodar os testes unitários:
  ```bash
  ./gradlew testDebugUnitTest
  ```

## 4. Preparar o Xiaomi 14 (uma vez só)

- [ ] **Ativar o modo desenvolvedor**:
  - Ajustes → Sobre o celular → tocar em "Versão MIUI/HyperOS" **7 vezes**
- [ ] **Ativar Depuração USB**:
  - Ajustes → Mais ajustes → Opções do desenvolvedor → **Depuração USB** ✅
  - Logo abaixo: **Instalar via USB** ✅ (Xiaomi exige isto além da depuração)
- [ ] **Conectar o celular ao Mac via USB**
- [ ] No celular, autorizar o computador quando aparecer o popup "Permitir depuração USB?"
  (marcar "Sempre permitir deste computador")
- [ ] Confirmar que o Mac enxerga o device:
  ```bash
  # Caminho do adb se você instalou via Android Studio:
  ~/Library/Android/sdk/platform-tools/adb devices
  ```
  Deve listar algo tipo `XXXXXXXX  device`. Se aparecer `unauthorized`, conferir o popup no celular.

## 5. Instalar e rodar no Xiaomi

- [ ] No terminal:
  ```bash
  cd /Users/gabrielmaruzi/GIT_GM/GM/android
  ./gradlew installDebug
  ```
- [ ] Ou no Android Studio: dropdown do device no topo → escolher o Xiaomi 14 →
      botão verde **Run** (▶).
- [ ] Abrir o app no celular: ícone "Cifras" no launcher (vai aparecer com sufixo
      "(debug)" no nome do pacote — normal).
- [ ] **Validar**:
  - [ ] App abre sem crash
  - [ ] Tela mostra "Cifras" no topo e o campo "Pesquisar música"
  - [ ] Digitar algo no campo não dá erro
  - [ ] Aparece a mensagem "Nenhuma cifra encontrada. (Importação local entra no Marco 1.)"

✅ **Se chegou até aqui, a versão 0.1.0 está rodando no seu celular.**

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
