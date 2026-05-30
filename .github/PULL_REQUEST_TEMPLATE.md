## O que muda?
<!-- 1-2 linhas descrevendo a mudança -->

## Por quê?
<!-- contexto / marco do roadmap (docs/proximos-passos.md) -->

## Checklist
- [ ] App ainda builda: `cd android && ./gradlew assembleDebug`
- [ ] Lint passa: `cd android && ./gradlew lintDebug`
- [ ] Sem novas permissões no `AndroidManifest.xml` (ou justificadas no PR)
- [ ] Sem novas dependências (ou listadas + verificadas contra `docs/seguranca-e-custos.md`)
- [ ] Sem segredos (`*.keystore`, `*.jks`, tokens) no diff
- [ ] Sem chamadas de rede adicionadas
