// m3.jsx — Material 3 color schemes + edge-to-edge phone frame
// Exports to window: M3_SEEDS, buildScheme, PhoneFrame, MONO, ROBOTO

const ROBOTO = "Roboto, 'Roboto Flex', system-ui, sans-serif";
const MONO = "'JetBrains Mono', 'Roboto Mono', ui-monospace, monospace";

// Dynamic Color seeds — each is a full tonal mapping for light & dark.
// "violeta" is the Material baseline (fallback when Dynamic Color is off).
const M3_SEEDS = {
  violeta: {
    label: 'Violeta (fallback)',
    light: {
      primary: '#6750A4', onPrimary: '#FFFFFF', primaryContainer: '#EADDFF', onPrimaryContainer: '#21005D',
      secondary: '#625B71', secondaryContainer: '#E8DEF8', onSecondaryContainer: '#1D192B',
      tertiary: '#7D5260', tertiaryContainer: '#FFD8E4', onTertiaryContainer: '#31111D',
      background: '#FEF7FF', onBackground: '#1D1B20',
      surface: '#FEF7FF', surfaceDim: '#DED8E1', surfaceBright: '#FEF7FF',
      surfaceContainerLowest: '#FFFFFF', surfaceContainerLow: '#F7F2FA', surfaceContainer: '#F3EDF7',
      surfaceContainerHigh: '#ECE6F0', surfaceContainerHighest: '#E6E0E9',
      onSurface: '#1D1B20', onSurfaceVariant: '#49454F',
      outline: '#79747E', outlineVariant: '#CAC4D0',
      error: '#B3261E', onErrorContainer: '#410E0B', errorContainer: '#F9DEDC',
    },
    dark: {
      primary: '#D0BCFF', onPrimary: '#381E72', primaryContainer: '#4F378B', onPrimaryContainer: '#EADDFF',
      secondary: '#CCC2DC', secondaryContainer: '#4A4458', onSecondaryContainer: '#E8DEF8',
      tertiary: '#EFB8C8', tertiaryContainer: '#633B48', onTertiaryContainer: '#FFD8E4',
      background: '#141218', onBackground: '#E6E0E9',
      surface: '#141218', surfaceDim: '#141218', surfaceBright: '#3B383E',
      surfaceContainerLowest: '#0F0D13', surfaceContainerLow: '#1D1B20', surfaceContainer: '#211F26',
      surfaceContainerHigh: '#2B2930', surfaceContainerHighest: '#36343B',
      onSurface: '#E6E0E9', onSurfaceVariant: '#CAC4D0',
      outline: '#938F99', outlineVariant: '#49454F',
      error: '#F2B8B5', onErrorContainer: '#F9DEDC', errorContainer: '#8C1D18',
    },
  },
  // Wallpaper-derived examples (Dynamic Color ON)
  verde: {
    label: 'Verde-musgo',
    light: {
      primary: '#4C662B', onPrimary: '#FFFFFF', primaryContainer: '#CDEDA3', onPrimaryContainer: '#102000',
      secondary: '#586249', secondaryContainer: '#DCE7C8', onSecondaryContainer: '#151E0B',
      tertiary: '#386663', tertiaryContainer: '#BCECE7', onTertiaryContainer: '#00201E',
      background: '#F9FAEF', onBackground: '#1A1C16',
      surface: '#F9FAEF', surfaceDim: '#DADBD0', surfaceBright: '#F9FAEF',
      surfaceContainerLowest: '#FFFFFF', surfaceContainerLow: '#F3F4E9', surfaceContainer: '#EEEFE3',
      surfaceContainerHigh: '#E8E9DE', surfaceContainerHighest: '#E2E3D8',
      onSurface: '#1A1C16', onSurfaceVariant: '#44483D',
      outline: '#75796C', outlineVariant: '#C5C8BA',
      error: '#BA1A1A', onErrorContainer: '#410002', errorContainer: '#FFDAD6',
    },
    dark: {
      primary: '#B1D18A', onPrimary: '#1F3701', primaryContainer: '#354E16', onPrimaryContainer: '#CDEDA3',
      secondary: '#BFCBAD', secondaryContainer: '#404A33', onSecondaryContainer: '#DCE7C8',
      tertiary: '#A0D0CB', tertiaryContainer: '#1E4E4B', onTertiaryContainer: '#BCECE7',
      background: '#12140E', onBackground: '#E2E3D8',
      surface: '#12140E', surfaceDim: '#12140E', surfaceBright: '#383A32',
      surfaceContainerLowest: '#0C0F09', surfaceContainerLow: '#1A1C16', surfaceContainer: '#1E201A',
      surfaceContainerHigh: '#282B24', surfaceContainerHighest: '#33362E',
      onSurface: '#E2E3D8', onSurfaceVariant: '#C5C8BA',
      outline: '#8F9285', outlineVariant: '#44483D',
      error: '#FFB4AB', onErrorContainer: '#FFDAD6', errorContainer: '#93000A',
    },
  },
  ambar: {
    label: 'Âmbar',
    light: {
      primary: '#8F4C38', onPrimary: '#FFFFFF', primaryContainer: '#FFDBD1', onPrimaryContainer: '#3A0B01',
      secondary: '#77574E', secondaryContainer: '#FFDBD1', onSecondaryContainer: '#2C150F',
      tertiary: '#6C5D2F', tertiaryContainer: '#F5E1A7', onTertiaryContainer: '#231B00',
      background: '#FFF8F6', onBackground: '#231917',
      surface: '#FFF8F6', surfaceDim: '#E8D6D2', surfaceBright: '#FFF8F6',
      surfaceContainerLowest: '#FFFFFF', surfaceContainerLow: '#FFF1ED', surfaceContainer: '#FCEAE5',
      surfaceContainerHigh: '#F7E4DF', surfaceContainerHighest: '#F1DED9',
      onSurface: '#231917', onSurfaceVariant: '#53433F',
      outline: '#85736E', outlineVariant: '#D8C2BC',
      error: '#BA1A1A', onErrorContainer: '#410002', errorContainer: '#FFDAD6',
    },
    dark: {
      primary: '#FFB5A0', onPrimary: '#561F0F', primaryContainer: '#723523', onPrimaryContainer: '#FFDBD1',
      secondary: '#E7BDB2', secondaryContainer: '#5D4037', onSecondaryContainer: '#FFDBD1',
      tertiary: '#D8C58D', tertiaryContainer: '#534619', onTertiaryContainer: '#F5E1A7',
      background: '#1A110F', onBackground: '#F1DED9',
      surface: '#1A110F', surfaceDim: '#1A110F', surfaceBright: '#423734',
      surfaceContainerLowest: '#140C0A', surfaceContainerLow: '#231917', surfaceContainer: '#271D1B',
      surfaceContainerHigh: '#322825', surfaceContainerHighest: '#3D322F',
      onSurface: '#F1DED9', onSurfaceVariant: '#D8C2BC',
      outline: '#A08C87', outlineVariant: '#53433F',
      error: '#FFB4AB', onErrorContainer: '#FFDAD6', errorContainer: '#93000A',
    },
  },
};

function buildScheme(seedKey, dark) {
  const seed = M3_SEEDS[seedKey] || M3_SEEDS.violeta;
  const c = dark ? seed.dark : seed.light;
  return { ...c, dark, ui: ROBOTO, mono: MONO };
}

// Edge-to-edge phone frame (Xiaomi 14 ratio ≈ 19.5:9). Content draws under the
// status bar and gesture pill; we expose inset paddings via render-prop-ish props.
function PhoneFrame({ scheme, statusDark, children, time = '21:08', bare = false }) {
  const C = scheme;
  const barFg = (statusDark != null ? statusDark : C.dark) ? '#FFFFFF' : '#1D1B20';
  return (
    <div style={{
      width: 392, height: 850, borderRadius: 44, position: 'relative',
      background: bare ? '#000' : C.background,
      border: '10px solid #0a0a0c', overflow: 'hidden',
      boxShadow: '0 40px 90px rgba(40,20,60,0.30), 0 6px 18px rgba(0,0,0,0.2)',
      boxSizing: 'border-box',
    }}>
      {/* content fills full frame (edge-to-edge) */}
      <div style={{ position: 'absolute', inset: 0, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
        {children}
      </div>
      {/* status bar overlay (edge-to-edge: floats above content) */}
      <div style={{
        position: 'absolute', top: 0, left: 0, right: 0, height: 44, zIndex: 50,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '0 26px', pointerEvents: 'none',
      }}>
        <span style={{ fontFamily: ROBOTO, fontSize: 14, fontWeight: 600, color: barFg }}>{time}</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Glyph kind="signal" color={barFg} />
          <Glyph kind="wifi" color={barFg} />
          <Glyph kind="battery" color={barFg} />
        </div>
      </div>
      {/* punch-hole camera */}
      <div style={{ position: 'absolute', top: 14, left: '50%', transform: 'translateX(-50%)', width: 11, height: 11, borderRadius: '50%', background: '#000', zIndex: 51 }} />
      {/* gesture nav pill */}
      <div style={{ position: 'absolute', bottom: 7, left: '50%', transform: 'translateX(-50%)', width: 126, height: 5, borderRadius: 3, background: barFg, opacity: 0.5, zIndex: 50, pointerEvents: 'none' }} />
    </div>
  );
}

function Glyph({ kind, color }) {
  const common = { fill: color };
  if (kind === 'signal') return <svg width="17" height="12" viewBox="0 0 17 12"><path {...common} d="M0 9h3v3H0zM5 6h3v6H5zM10 3h3v9h-3zM15 0h2v12h-2z"/></svg>;
  if (kind === 'wifi') return <svg width="16" height="12" viewBox="0 0 16 12"><path {...common} d="M8 11.5L0.8 3.6A10.8 10.8 0 018 0.8a10.8 10.8 0 017.2 2.8z" opacity="0.9"/></svg>;
  if (kind === 'battery') return (
    <svg width="24" height="13" viewBox="0 0 24 13"><rect x="0.5" y="0.5" width="20" height="12" rx="3" fill="none" stroke={color} opacity="0.5"/><rect x="2" y="2" width="15" height="9" rx="1.5" fill={color}/><rect x="21" y="4" width="2" height="5" rx="1" fill={color} opacity="0.5"/></svg>
  );
  return null;
}

Object.assign(window, { M3_SEEDS, buildScheme, PhoneFrame, MONO, ROBOTO });
