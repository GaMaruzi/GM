// Detail.jsx — single cifra: monospace body, transpose, font size, favorite, capo
// Exports to window: DetailScreen

function DetailScreen({ C, song, favorite, onBack, onToggleFav, onMenu, onStagePlay, scrollPref, onSaveScroll }) {
  const { useState, useRef, useEffect } = React;
  const [font, setFont] = useState(16);
  const [semis, setSemis] = useState(0);
  const prefFlat = window.keyPrefersFlat(song.key);
  const curKey = window.transposeKey(song.key, semis, prefFlat);
  const scrollRef = useRef(null);

  // restore per-song reading position
  useEffect(() => {
    const el = scrollRef.current; if (!el) return;
    el.scrollTop = +(localStorage.getItem('tc:pos:' + song.id) || 0);
    const on = () => localStorage.setItem('tc:pos:' + song.id, String(el.scrollTop));
    el.addEventListener('scroll', on, { passive: true });
    return () => el.removeEventListener('scroll', on);
  }, [song.id]);

  const fontStep = (d) => setFont((f) => Math.max(12, Math.min(28, f + d)));

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: C.surface, minHeight: 0 }}>
      <TopBar C={C} onBack={onBack} title={song.title} subtitle={song.artist}
        actions={
          <React.Fragment>
            <IconBtn name={favorite ? 'star' : 'starOff'} C={C} color={favorite ? C.primary : C.onSurfaceVariant} onClick={() => onToggleFav(song.id)} />
            <IconBtn name="more" C={C} onClick={onMenu} />
          </React.Fragment>
        } />

      {/* reading controls */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '4px 16px 12px', flexShrink: 0 }}>
        {/* transpose */}
        <div style={{ display: 'flex', alignItems: 'center', background: C.surfaceContainerHigh, borderRadius: 20, height: 40, padding: '0 4px' }}>
          <Stepper C={C} onClick={() => setSemis((s) => s - 1)} name="remove" />
          <div style={{ minWidth: 44, textAlign: 'center', font: `700 15px/1 ${C.mono}`, color: C.primary }}>{curKey}</div>
          <Stepper C={C} onClick={() => setSemis((s) => s + 1)} name="add" />
        </div>
        {semis !== 0 && (
          <button onClick={() => setSemis(0)} style={{ border: 'none', background: 'transparent', font: `500 13px/1 ${C.ui}`, color: C.primary, cursor: 'pointer' }}>↺ original</button>
        )}
        <div style={{ flex: 1 }} />
        {/* font size */}
        <div style={{ display: 'flex', alignItems: 'center', background: C.surfaceContainerHigh, borderRadius: 20, height: 40, padding: '0 4px' }}>
          <Stepper C={C} onClick={() => fontStep(-1)} name="remove" />
          <MIcon name="textSize" size={20} color={C.onSurfaceVariant} style={{ margin: '0 2px' }} />
          <Stepper C={C} onClick={() => fontStep(1)} name="add" />
        </div>
      </div>

      {/* body */}
      <div ref={scrollRef} style={{ flex: 1, overflow: 'auto', minHeight: 0, padding: '4px 20px 120px' }}>
        {song.capo > 0 && (
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: 8, marginBottom: 16, padding: '6px 14px', borderRadius: 10, background: C.tertiaryContainer, color: C.onTertiaryContainer, font: `600 13px/1 ${C.ui}` }}>
            <MIcon name="musicNote" size={16} color={C.onTertiaryContainer} /> Capotraste na {song.capo}ª casa
          </div>
        )}
        <CifraBody song={song} C={C} fontSize={font} semis={semis} />
        <div style={{ textAlign: 'center', font: `400 12px/1 ${C.ui}`, color: C.onSurfaceVariant, padding: '8px 0 0', opacity: 0.7 }}>{song.file}</div>
      </div>

      {/* play-in-stage FAB */}
      <div style={{ position: 'absolute', right: 16, bottom: 24, zIndex: 20 }}>
        <M3Fab icon="play" label="Tocar no palco" extended C={C} onClick={() => onStagePlay(song.id)} />
      </div>
    </div>
  );
}

function Stepper({ C, onClick, name }) {
  return (
    <button onClick={onClick} style={{ width: 32, height: 32, borderRadius: 16, border: 'none', background: 'transparent', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}
      onPointerDown={(e) => (e.currentTarget.style.background = C.onSurface + '14')}
      onPointerUp={(e) => (e.currentTarget.style.background = 'transparent')}
      onPointerLeave={(e) => (e.currentTarget.style.background = 'transparent')}>
      <MIcon name={name} size={18} color={C.onSurface} />
    </button>
  );
}

Object.assign(window, { DetailScreen });
