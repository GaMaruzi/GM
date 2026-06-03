// theory.jsx — chord parsing + transposition helpers
// Exports to window: NOTES, noteIndex, transposeChord, transposeChordLine, transposeKey

const SHARP = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B'];
const FLAT  = ['C', 'Db', 'D', 'Eb', 'E', 'F', 'Gb', 'G', 'Ab', 'A', 'Bb', 'B'];

const IDX = {
  'C': 0, 'B#': 0, 'C#': 1, 'Db': 1, 'D': 2, 'D#': 3, 'Eb': 3,
  'E': 4, 'Fb': 4, 'F': 5, 'E#': 5, 'F#': 6, 'Gb': 6, 'G': 7,
  'G#': 8, 'Ab': 8, 'A': 9, 'A#': 10, 'Bb': 10, 'B': 11, 'Cb': 11,
};

// keys that conventionally use flats
const FLAT_KEYS = new Set(['F', 'Bb', 'Eb', 'Ab', 'Db', 'Gb', 'Dm', 'Gm', 'Cm', 'Fm', 'Bbm']);

function noteName(idx, preferFlat) {
  idx = ((idx % 12) + 12) % 12;
  return (preferFlat ? FLAT : SHARP)[idx];
}

// Transpose a single chord token, e.g. "C#m7", "G/B", "Dsus4"
function transposeChord(token, semis, preferFlat) {
  // split slash (bass)
  const parts = token.split('/');
  const out = parts.map((p) => {
    const m = p.match(/^([A-G][#b]?)(.*)$/);
    if (!m) return p;
    const root = m[1];
    const rest = m[2];
    if (!(root in IDX)) return p;
    const ni = (IDX[root] + semis) % 12;
    return noteName(ni, preferFlat) + rest;
  });
  return out.join('/');
}

// Transpose a whole chord line while preserving column alignment.
// Anchors each chord at its original start column; nudges right only on collision.
function transposeChordLine(line, semis, preferFlat) {
  if (!line) return line;
  if (semis === 0) return line;
  const re = /\S+/g;
  let m;
  let result = '';
  let cursor = 0;
  while ((m = re.exec(line)) !== null) {
    const start = m.index;
    const tok = transposeChord(m[0], semis, preferFlat);
    // pad spaces up to the anchor column (at least one space gap)
    if (start > result.length) {
      result += ' '.repeat(start - result.length);
    } else if (result.length > 0) {
      result += ' ';
    }
    result += tok;
    cursor = start;
  }
  return result;
}

function transposeKey(key, semis, preferFlat) {
  if (!key) return key;
  const m = key.match(/^([A-G][#b]?)(.*)$/);
  if (!m || !(m[1] in IDX)) return key;
  const ni = (IDX[m[1]] + semis) % 12;
  return noteName(ni, preferFlat) + m[2];
}

function keyPrefersFlat(key) {
  return FLAT_KEYS.has(key);
}

Object.assign(window, {
  SHARP, FLAT, transposeChord, transposeChordLine, transposeKey, keyPrefersFlat,
});
