// Empty.jsx — first-run state: no folder chosen yet
// Exports to window: EmptyState

function EmptyState({ C, onPick }) {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', paddingTop: 44, background: C.surface }}>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '0 40px', textAlign: 'center' }}>
        {/* illustration: folder + note, drawn from M3 tonal surfaces */}
        <div style={{ position: 'relative', width: 168, height: 168, marginBottom: 40 }}>
          <div style={{ position: 'absolute', inset: 0, borderRadius: 48, background: C.primaryContainer, transform: 'rotate(-6deg)' }} />
          <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <MIcon name="folderOpen" size={88} color={C.onPrimaryContainer} />
          </div>
          <div style={{ position: 'absolute', right: 18, top: 16, width: 52, height: 52, borderRadius: 26, background: C.tertiaryContainer, display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 10px rgba(0,0,0,0.15)' }}>
            <MIcon name="musicNote" size={28} color={C.onTertiaryContainer} />
          </div>
        </div>

        <h1 style={{ margin: 0, font: `400 28px/1.2 ${C.ui}`, color: C.onSurface }}>Suas cifras, offline</h1>
        <p style={{ margin: '14px 0 0', font: `400 15px/1.5 ${C.ui}`, color: C.onSurfaceVariant, maxWidth: 280 }}>
          Escolha a pasta do seu celular onde ficam suas cifras. O Tap Cifras vai indexar os arquivos <b style={{ color: C.onSurface }}>.txt</b> dela — tudo fica no aparelho.
        </p>

        <div style={{ marginTop: 32 }}>
          <M3Button kind="filled" icon="folder" size="lg" C={C} onClick={onPick}>Escolher pasta de cifras</M3Button>
        </div>
      </div>

      {/* offline reassurance footer */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8, padding: '0 40px 40px' }}>
        <MIcon name="shield" size={18} color={C.onSurfaceVariant} />
        <span style={{ font: `400 13px/1.4 ${C.ui}`, color: C.onSurfaceVariant }}>Sem internet · sem login · sem anúncios</span>
      </div>
    </div>
  );
}

Object.assign(window, { EmptyState });
