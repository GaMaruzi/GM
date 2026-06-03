// Search.jsx — home: search bar, filter chips, file list, overflow menu, Palco FAB
// Exports to window: SearchScreen

function SearchScreen({ C, songs, favorites, recents, folderName, onOpen, onToggleFav, onOpenMenu, onStartPalco }) {
  const { useState, useMemo } = React;
  const [q, setQ] = useState('');
  const [tab, setTab] = useState('todas'); // todas | favoritas | recentes

  const list = useMemo(() => {
    const t = q.trim().toLowerCase();
    let base = songs;
    if (tab === 'favoritas') base = songs.filter((s) => favorites.has(s.id));
    if (tab === 'recentes') base = recents.map((id) => songs.find((s) => s.id === id)).filter(Boolean);
    if (!t) return base;
    return base.filter((s) => (s.file + ' ' + s.genre).toLowerCase().includes(t));
  }, [songs, q, tab, favorites, recents]);

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: C.surface, minHeight: 0 }}>
      {/* status bar inset */}
      <div style={{ height: 44, flexShrink: 0 }} />

      {/* M3 docked search bar */}
      <div style={{ padding: '6px 16px 4px', flexShrink: 0 }}>
        <div style={{
          height: 56, borderRadius: 28, background: C.surfaceContainerHigh,
          display: 'flex', alignItems: 'center', gap: 6, padding: '0 6px 0 6px',
        }}>
          <IconBtn name="search" C={C} onClick={() => {}} />
          <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Buscar nas suas cifras"
            style={{ flex: 1, border: 'none', outline: 'none', background: 'transparent', font: `400 16px/1 ${C.ui}`, color: C.onSurface }} />
          {q ? (
            <IconBtn name="close" C={C} onClick={() => setQ('')} />
          ) : (
            <IconBtn name="more" C={C} onClick={onOpenMenu} />
          )}
        </div>
      </div>

      {/* folder status */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 7, padding: '6px 24px 8px', flexShrink: 0 }}>
        <MIcon name="folder" size={16} color={C.onSurfaceVariant} />
        <span style={{ font: `400 13px/1.3 ${C.ui}`, color: C.onSurfaceVariant }}>
          {folderName} · {songs.length} cifras indexadas
        </span>
      </div>

      {/* filter chips */}
      <div style={{ display: 'flex', gap: 8, padding: '0 16px 10px', flexShrink: 0, overflowX: 'auto' }}>
        <Chip label="Todas" selected={tab === 'todas'} onClick={() => setTab('todas')} C={C} />
        <Chip label="Favoritas" selected={tab === 'favoritas'} onClick={() => setTab('favoritas')} C={C} icon="star" />
        <Chip label="Recentes" selected={tab === 'recentes'} onClick={() => setTab('recentes')} C={C} icon="history" />
      </div>

      {/* list */}
      <div style={{ flex: 1, overflow: 'auto', minHeight: 0, paddingBottom: 96 }}>
        {list.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '56px 40px', color: C.onSurfaceVariant }}>
            <MIcon name="search" size={44} color={C.outline} style={{ margin: '0 auto 14px' }} />
            <div style={{ font: `400 16px/1.4 ${C.ui}` }}>
              {q ? `Nada encontrado para "${q}".` : tab === 'favoritas' ? 'Nenhuma favorita ainda. Toque na ★ de uma cifra.' : 'Nada por aqui ainda.'}
            </div>
          </div>
        ) : list.map((s) => {
          const fav = favorites.has(s.id);
          return (
            <div key={s.id} onClick={() => onOpen(s.id)}
              style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '10px 16px 10px 24px', cursor: 'pointer' }}
              onPointerDown={(e) => (e.currentTarget.style.background = C.onSurface + '0d')}
              onPointerUp={(e) => (e.currentTarget.style.background = 'transparent')}
              onPointerLeave={(e) => (e.currentTarget.style.background = 'transparent')}>
              <div style={{ width: 40, height: 40, borderRadius: 12, flexShrink: 0, background: C.surfaceContainerHighest, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <MIcon name="description" size={22} color={C.onSurfaceVariant} />
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ font: `400 16px/1.3 ${C.ui}`, color: C.onSurface, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{s.title}</div>
                <div style={{ font: `400 13px/1.3 ${C.ui}`, color: C.onSurfaceVariant, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {s.artist} · {s.ext.toUpperCase()} · tom {s.key}
                </div>
              </div>
              <button onClick={(e) => { e.stopPropagation(); onToggleFav(s.id); }}
                style={{ width: 44, height: 44, border: 'none', background: 'transparent', borderRadius: 22, display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', flexShrink: 0 }}>
                <MIcon name={fav ? 'star' : 'starOff'} size={22} color={fav ? C.primary : C.onSurfaceVariant} />
              </button>
            </div>
          );
        })}
      </div>

      {/* extended FAB — Modo Palco */}
      <div style={{ position: 'absolute', right: 16, bottom: 24, zIndex: 20 }}>
        <M3Fab icon="fullscreen" label="Modo palco" extended C={C} onClick={onStartPalco} />
      </div>
    </div>
  );
}

Object.assign(window, { SearchScreen });
