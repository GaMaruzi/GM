// Settings.jsx — folder, theme, privacy, about. No cloud anything.
// Exports to window: SettingsScreen

function SettingsScreen({ C, folderName, count, themeMode, setThemeMode, dynamic, setDynamic, onBack, onChangeFolder }) {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: C.surface, minHeight: 0 }}>
      <TopBar C={C} onBack={onBack} title="Configurações" />
      <div style={{ flex: 1, overflow: 'auto', minHeight: 0, paddingBottom: 30 }}>

        <Section C={C} label="Pasta de cifras" />
        <Row C={C} icon="folder" title={folderName} sub={`${count} arquivos .txt indexados`}
          trailing={<M3Button kind="tonal" C={C} onClick={onChangeFolder}>Trocar</M3Button>} />

        <Section C={C} label="Aparência" />
        <div style={{ padding: '4px 16px 8px' }}>
          <div style={{ display: 'flex', gap: 8 }}>
            {[['sistema', 'Sistema'], ['claro', 'Claro'], ['escuro', 'Escuro']].map(([k, l]) => (
              <button key={k} onClick={() => setThemeMode(k)} style={{
                flex: 1, height: 44, borderRadius: 12, cursor: 'pointer',
                border: themeMode === k ? 'none' : `1px solid ${C.outlineVariant}`,
                background: themeMode === k ? C.secondaryContainer : 'transparent',
                color: themeMode === k ? C.onSecondaryContainer : C.onSurfaceVariant,
                font: `600 14px/1 ${C.ui}`,
              }}>{l}</button>
            ))}
          </div>
        </div>
        <ToggleRow C={C} icon="darkMode" title="Cores dinâmicas" sub="Derivar a paleta do papel de parede (Android 12+)" value={dynamic} onChange={setDynamic} />

        <Section C={C} label="Privacidade" />
        <div style={{ margin: '4px 16px', padding: '16px 18px', borderRadius: 16, background: C.surfaceContainerHigh, display: 'flex', gap: 14 }}>
          <MIcon name="shield" size={24} color={C.primary} style={{ flexShrink: 0, marginTop: 2 }} />
          <div>
            <div style={{ font: `600 15px/1.3 ${C.ui}`, color: C.onSurface, marginBottom: 4 }}>Este app não acessa a internet</div>
            <div style={{ font: `400 13.5px/1.5 ${C.ui}`, color: C.onSurfaceVariant }}>
              Suas cifras não saem do seu celular. Sem login, sem conta, sem anúncios, sem permissões — a leitura da pasta usa o seletor do próprio Android.
            </div>
          </div>
        </div>

        <Section C={C} label="Sobre" />
        <Row C={C} icon="info" title="Tap Cifras" sub="Versão 1.0 (MVP) · feito para tocar offline" />
      </div>
    </div>
  );
}

function Section({ C, label }) {
  return <div style={{ padding: '18px 24px 6px', font: `700 12px/1 ${C.ui}`, letterSpacing: 1, textTransform: 'uppercase', color: C.primary }}>{label}</div>;
}
function Row({ C, icon, title, sub, trailing }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '10px 16px 10px 24px' }}>
      <MIcon name={icon} size={22} color={C.onSurfaceVariant} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ font: `400 16px/1.3 ${C.ui}`, color: C.onSurface, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{title}</div>
        {sub && <div style={{ font: `400 13px/1.3 ${C.ui}`, color: C.onSurfaceVariant }}>{sub}</div>}
      </div>
      {trailing}
    </div>
  );
}
function ToggleRow({ C, icon, title, sub, value, onChange }) {
  return (
    <div onClick={() => onChange(!value)} style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '10px 20px 10px 24px', cursor: 'pointer' }}>
      <MIcon name={icon} size={22} color={C.onSurfaceVariant} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ font: `400 16px/1.3 ${C.ui}`, color: C.onSurface }}>{title}</div>
        {sub && <div style={{ font: `400 13px/1.35 ${C.ui}`, color: C.onSurfaceVariant }}>{sub}</div>}
      </div>
      <Switch C={C} on={value} />
    </div>
  );
}
function Switch({ C, on }) {
  return (
    <div style={{ width: 52, height: 32, borderRadius: 16, flexShrink: 0, position: 'relative',
      background: on ? C.primary : C.surfaceContainerHighest,
      border: on ? 'none' : `2px solid ${C.outline}`, transition: 'background .2s' }}>
      <div style={{ position: 'absolute', top: on ? 4 : 6, left: on ? 24 : 6, width: on ? 24 : 16, height: on ? 24 : 16, borderRadius: 12,
        background: on ? C.onPrimary : C.outline, transition: 'all .2s' }} />
    </div>
  );
}

Object.assign(window, { SettingsScreen });
