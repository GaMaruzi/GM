// render.jsx — shared monospace cifra renderer (chords distinct from lyrics)
// Exports to window: CifraBody

function CifraBody({ song, C, fontSize, chordColor, textColor, semis = 0, lineHeight = 1.25, showTags = true, gap = 18 }) {
  const prefFlat = window.keyPrefersFlat ? window.keyPrefersFlat(song.key) : false;
  const chCol = chordColor || C.primary;
  const txtCol = textColor || C.onSurface;
  return (
    <div style={{ fontFamily: C.mono }}>
      {song.sections.map((sec, si) => (
        <div key={si} style={{ marginBottom: gap }}>
          {showTags && (
            <div style={{ font: `700 11px/1 ${C.ui}`, letterSpacing: 1.5, textTransform: 'uppercase', color: C.onSurfaceVariant, marginBottom: 8, opacity: 0.85 }}>{sec.tag}</div>
          )}
          {sec.lines.map((ln, li) => {
            const ch = window.transposeChordLine ? window.transposeChordLine(ln.ch, semis, prefFlat) : ln.ch;
            return (
              <div key={li} style={{ marginBottom: ln.ly ? 4 : 2 }}>
                {ch && <div style={{ whiteSpace: 'pre', fontSize: fontSize, lineHeight, fontWeight: 700, color: chCol }}>{ch}</div>}
                {ln.ly && <div style={{ whiteSpace: 'pre-wrap', fontSize: fontSize, lineHeight, color: txtCol }}>{ln.ly}</div>}
              </div>
            );
          })}
        </div>
      ))}
    </div>
  );
}

Object.assign(window, { CifraBody });
