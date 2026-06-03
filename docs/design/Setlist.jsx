// Setlist.jsx — build the stage sequence: pick songs, reorder, preset scroll speed
// Exports to window: SetlistScreen

function SetlistScreen({ C, songs, setlist, setSetlist, speeds, setSpeed, onBack, onStart }) {
  const inList = new Set(setlist);
  const selectedSongs = setlist.map((id) => songs.find((s) => s.id === id)).filter(Boolean);
  const available = songs.filter((s) => !inList.has(s.id));

  const add = (id) => setSetlist([...setlist, id]);
  const remove = (id) => setSetlist(setlist.filter((x) => x !== id));
  const move = (i, d) => {
    const j = i + d; if (j < 0 || j >= setlist.length) return;
    const next = setlist.slice(); [next[i], next[j]] = [next[j], next[i]]; setSetlist(next);
  };

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: C.surface, minHeight: 0 }}>
      <TopBar C={C} onBack={onBack} title="Modo palco" subtitle="Monte a sequência do ensaio" />

      <div style={{ flex: 1, overflow: 'auto', minHeight: 0, paddingBottom: 110 }}>
        {/* sequence */}
        <div style={{ padding: '4px 24px 6px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ font: `700 12px/1 ${C.ui}`, letterSpacing: 1, textTransform: 'uppercase', color: C.primary }}>Na sequência</span>
          <span style={{ font: `400 13px/1 ${C.ui}`, color: C.onSurfaceVariant }}>{setlist.length} músicas</span>
        </div>

        {selectedSongs.length === 0 && (
          <div style={{ margin: '6px 16px 14px', padding: '22px 18px', borderRadius: 16, border: `1px dashed ${C.outlineVariant}`, textAlign: 'center', color: C.onSurfaceVariant, font: `400 14px/1.4 ${C.ui}` }}>
            Adicione cifras abaixo para montar seu setlist. No palco, toque para avançar e toque duas vezes para voltar.
          </div>
        )}

        {selectedSongs.map((s, i) => (
          <div key={s.id} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '8px 12px 8px 16px' }}>
            <MIcon name="drag" size={20} color={C.outline} />
            <div style={{ width: 26, height: 26, borderRadius: 13, background: C.primary, color: C.onPrimary, font: `700 13px/26px ${C.mono}`, textAlign: 'center', flexShrink: 0 }}>{i + 1}</div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ font: `400 15px/1.25 ${C.ui}`, color: C.onSurface, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{s.title}</div>
              {/* scroll speed preset */}
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 3 }}>
                <MIcon name="speed" size={14} color={C.onSurfaceVariant} />
                <span style={{ font: `400 12px/1 ${C.ui}`, color: C.onSurfaceVariant }}>rolagem</span>
                <button onClick={() => setSpeed(s.id, Math.max(0, (speeds[s.id] ?? 3) - 1))} style={miniBtn(C)}>–</button>
                <span style={{ minWidth: 14, textAlign: 'center', font: `700 12px/1 ${C.mono}`, color: speeds[s.id] === 0 ? C.onSurfaceVariant : C.primary }}>{speeds[s.id] === 0 ? 'off' : (speeds[s.id] ?? 3)}</span>
                <button onClick={() => setSpeed(s.id, Math.min(6, (speeds[s.id] ?? 3) + 1))} style={miniBtn(C)}>+</button>
              </div>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <button onClick={() => move(i, -1)} style={arrowBtn(C, i === 0)}>▲</button>
              <button onClick={() => move(i, 1)} style={arrowBtn(C, i === selectedSongs.length - 1)}>▼</button>
            </div>
            <IconBtn name="remove" C={C} size={20} onClick={() => remove(s.id)} />
          </div>
        ))}

        <div style={{ height: 8 }} />
        <Divider C={C} inset={24} />

        {/* available */}
        <div style={{ padding: '14px 24px 6px', font: `700 12px/1 ${C.ui}`, letterSpacing: 1, textTransform: 'uppercase', color: C.onSurfaceVariant }}>Adicionar cifras</div>
        {available.map((s) => (
          <div key={s.id} onClick={() => add(s.id)} style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '10px 16px 10px 24px', cursor: 'pointer' }}
            onPointerDown={(e) => (e.currentTarget.style.background = C.onSurface + '0d')}
            onPointerUp={(e) => (e.currentTarget.style.background = 'transparent')}
            onPointerLeave={(e) => (e.currentTarget.style.background = 'transparent')}>
            <MIcon name="add" size={22} color={C.primary} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ font: `400 15px/1.25 ${C.ui}`, color: C.onSurface, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{s.title}</div>
              <div style={{ font: `400 12.5px/1.3 ${C.ui}`, color: C.onSurfaceVariant }}>{s.artist} · tom {s.key}</div>
            </div>
          </div>
        ))}
      </div>

      {/* start bar */}
      <div style={{ position: 'absolute', left: 0, right: 0, bottom: 0, padding: '12px 16px 28px', background: `linear-gradient(transparent, ${C.surface} 28%)` }}>
        <M3Button kind="filled" icon="play" size="lg" full C={C} disabled={setlist.length === 0} onClick={onStart}>
          Iniciar palco{setlist.length ? ` · ${setlist.length}` : ''}
        </M3Button>
      </div>
    </div>
  );
}

function miniBtn(C) {
  return { width: 22, height: 22, borderRadius: 11, border: `1px solid ${C.outlineVariant}`, background: 'transparent', color: C.onSurfaceVariant, font: `700 13px/1 sans-serif`, cursor: 'pointer', padding: 0 };
}
function arrowBtn(C, disabled) {
  return { width: 24, height: 22, border: 'none', background: 'transparent', color: disabled ? C.outlineVariant : C.onSurfaceVariant, fontSize: 11, cursor: disabled ? 'default' : 'pointer', padding: 0, opacity: disabled ? 0.4 : 1 };
}

Object.assign(window, { SetlistScreen });
