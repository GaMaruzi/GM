// Stage.jsx — performance mode. Black bg, giant font, screen awake.
// Tap anywhere = next song · double-tap = previous song · per-song preset auto-scroll.
// Exports to window: StageScreen

function StageScreen({ C, songs, setlist, speeds, onExit, onSpeedChange }) {
  const { useState, useRef, useEffect, useCallback } = React;
  const [idx, setIdx] = useState(0);
  const [showHint, setShowHint] = useState(true);
  const [chrome, setChrome] = useState(true); // top/bottom overlay visible
  const [font, setFont] = useState(28);
  const scrollRef = useRef(null);
  const tapTimer = useRef(null);
  const wakeRef = useRef(null);
  const chromeTimer = useRef(null);

  const seq = setlist.map((id) => songs.find((s) => s.id === id)).filter(Boolean);
  const song = seq[idx];
  const speed = speeds[song?.id] ?? 3;

  // stage palette: true black for OLED, primary-tinted chords for contrast
  const STAGE = {
    bg: '#000000', text: '#F2F2F2', dim: '#8A8A8A',
    chord: C.dark ? C.primary : (C.primaryContainer === '#EADDFF' ? '#D0BCFF' : C.primary),
    ui: C.ui, mono: C.mono,
  };

  const go = useCallback((d) => {
    setIdx((i) => Math.max(0, Math.min(seq.length - 1, i + d)));
    setShowHint(false);
  }, [seq.length]);

  // reset scroll on song change
  useEffect(() => { if (scrollRef.current) scrollRef.current.scrollTop = 0; }, [idx]);

  // auto-scroll engine (uses per-song preset speed)
  useEffect(() => {
    if (!speed || speed === 0) return;
    let raf, acc = 0, last = performance.now();
    const tick = (now) => {
      const dt = now - last; last = now;
      acc += (speed * 11) * dt / 1000;
      const el = scrollRef.current;
      if (el && acc >= 1) { const step = Math.floor(acc); acc -= step; el.scrollTop += step; }
      raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [idx, speed]);

  // keep awake
  useEffect(() => {
    (async () => { try { if ('wakeLock' in navigator) wakeRef.current = await navigator.wakeLock.request('screen'); } catch (e) {} })();
    return () => { try { wakeRef.current && wakeRef.current.release(); } catch (e) {} };
  }, []);

  // auto-hide chrome
  useEffect(() => {
    clearTimeout(chromeTimer.current);
    if (chrome) chromeTimer.current = setTimeout(() => setChrome(false), 3200);
    return () => clearTimeout(chromeTimer.current);
  }, [chrome, idx]);

  // single vs double tap on the reading surface
  const onTap = () => {
    if (tapTimer.current) {            // second tap → previous
      clearTimeout(tapTimer.current); tapTimer.current = null;
      go(-1);
    } else {
      tapTimer.current = setTimeout(() => {  // single tap → next
        tapTimer.current = null;
        if (idx >= seq.length - 1) { setChrome(true); } else go(1);
      }, 280);
    }
  };

  if (!song) return null;

  return (
    <div style={{ position: 'absolute', inset: 0, background: STAGE.bg, overflow: 'hidden', userSelect: 'none' }}>
      {/* tap surface = the scrolling cifra */}
      <div ref={scrollRef} onClick={onTap}
        style={{ position: 'absolute', inset: 0, overflow: 'auto', padding: '92px 22px 120px', WebkitOverflowScrolling: 'touch' }}>
        <div style={{ font: `700 13px/1 ${STAGE.ui}`, letterSpacing: 2, textTransform: 'uppercase', color: STAGE.dim, marginBottom: 4 }}>{song.artist}</div>
        <div style={{ font: `500 24px/1.1 ${STAGE.ui}`, color: STAGE.text, marginBottom: 22 }}>{song.title}</div>
        <CifraBody song={song} C={{ ...C, mono: STAGE.mono, ui: STAGE.ui, onSurfaceVariant: STAGE.dim }}
          fontSize={font} chordColor={STAGE.chord} textColor={STAGE.text} lineHeight={1.32} gap={26} />
        <div style={{ height: 60, textAlign: 'center', color: STAGE.dim, font: `400 13px/1 ${STAGE.ui}`, paddingTop: 24 }}>
          {idx < seq.length - 1 ? 'toque para a próxima ▸' : '✓ última do setlist'}
        </div>
      </div>

      {/* center hint (fades) */}
      {showHint && (
        <div onClick={onTap} style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 30, pointerEvents: 'none' }}>
          <div style={{ background: 'rgba(20,20,20,0.82)', borderRadius: 20, padding: '18px 24px', textAlign: 'center', maxWidth: 260 }}>
            <div style={{ display: 'flex', justifyContent: 'center', gap: 18, marginBottom: 12 }}>
              <Hint STAGE={STAGE} taps="1" label="próxima" />
              <Hint STAGE={STAGE} taps="2" label="anterior" />
            </div>
            <div style={{ font: `400 12.5px/1.4 ${STAGE.ui}`, color: STAGE.dim }}>toque em qualquer lugar da tela</div>
          </div>
        </div>
      )}

      {/* top chrome */}
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, padding: '46px 10px 10px', display: 'flex', alignItems: 'center', gap: 8, zIndex: 40, background: 'linear-gradient(rgba(0,0,0,0.85), transparent)', opacity: chrome ? 1 : 0, transition: 'opacity .4s', pointerEvents: chrome ? 'auto' : 'none' }}>
        <button onClick={(e) => { e.stopPropagation(); onExit(); }} style={stageBtn(STAGE)}>
          <MIcon name="close" size={24} color={STAGE.text} />
        </button>
        <div style={{ flex: 1, textAlign: 'center' }}>
          <div style={{ font: `600 13px/1 ${STAGE.ui}`, color: STAGE.text }}>{idx + 1} / {seq.length}</div>
        </div>
        {/* speed control */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 2, background: 'rgba(40,40,40,0.7)', borderRadius: 18, padding: '0 4px', height: 36 }} onClick={(e) => e.stopPropagation()}>
          <MIcon name="speed" size={16} color={STAGE.dim} style={{ margin: '0 2px' }} />
          <button onClick={() => onSpeedChange(song.id, Math.max(0, speed - 1))} style={stageMini(STAGE)}>–</button>
          <span style={{ minWidth: 16, textAlign: 'center', font: `700 12px/1 ${STAGE.mono}`, color: speed === 0 ? STAGE.dim : STAGE.chord }}>{speed === 0 ? 'off' : speed}</span>
          <button onClick={() => onSpeedChange(song.id, Math.min(6, speed + 1))} style={stageMini(STAGE)}>+</button>
        </div>
        {/* font */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 2, background: 'rgba(40,40,40,0.7)', borderRadius: 18, padding: '0 4px', height: 36 }} onClick={(e) => e.stopPropagation()}>
          <button onClick={() => setFont((f) => Math.max(18, f - 2))} style={stageMini(STAGE)}>A−</button>
          <button onClick={() => setFont((f) => Math.min(44, f + 2))} style={stageMini(STAGE)}>A+</button>
        </div>
      </div>

      {/* bottom progress dots */}
      <div style={{ position: 'absolute', bottom: 18, left: 0, right: 0, display: 'flex', justifyContent: 'center', gap: 6, zIndex: 40, opacity: chrome ? 1 : 0.5, transition: 'opacity .4s', pointerEvents: 'none' }}>
        {seq.map((_, i) => (
          <div key={i} style={{ width: i === idx ? 20 : 6, height: 6, borderRadius: 3, background: i === idx ? STAGE.chord : STAGE.dim, opacity: i === idx ? 1 : 0.5, transition: 'all .3s' }} />
        ))}
      </div>
    </div>
  );
}

function Hint({ STAGE, taps, label }) {
  return (
    <div style={{ textAlign: 'center' }}>
      <div style={{ width: 52, height: 52, borderRadius: 26, border: `2px solid ${STAGE.chord}`, display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 6px', font: `700 20px/1 ${STAGE.mono}`, color: STAGE.chord }}>{taps}×</div>
      <div style={{ font: `500 12px/1 ${STAGE.ui}`, color: STAGE.text }}>{label}</div>
    </div>
  );
}
function stageBtn(STAGE) {
  return { width: 44, height: 44, borderRadius: 22, border: 'none', background: 'rgba(40,40,40,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' };
}
function stageMini(STAGE) {
  return { minWidth: 26, height: 30, borderRadius: 15, border: 'none', background: 'transparent', color: STAGE.text, font: `700 13px/1 ${STAGE.ui}`, cursor: 'pointer', padding: '0 4px' };
}

Object.assign(window, { StageScreen });
